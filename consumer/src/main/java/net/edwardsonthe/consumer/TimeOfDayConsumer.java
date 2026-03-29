package net.edwardsonthe.consumer;

import net.edwardsonthe.messages.TimeOfDayMessage;
import net.edwardsonthe.messages.TimeOfDayMessageDataReader;
import net.edwardsonthe.messages.TimeOfDayMessageSeq;
import net.edwardsonthe.messages.TimeOfDayMessageTypeSupport;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.topic.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Subscribes to {@link TimeOfDayMessage} instances from a DDS topic and logs them.
 *
 * <p>This is a standalone Java application (no framework) that manually creates all DDS
 * entities, registers a {@link DataReaderAdapter} listener for asynchronous message
 * delivery, and blocks the main thread until a JVM shutdown signal is received.
 *
 * <p>The inner {@link TimeOfDayListener} class handles the DDS {@code on_data_available}
 * callback, which executes on a DDS-managed thread.
 */
public class TimeOfDayConsumer {

  private static final Logger logger = LoggerFactory.getLogger(TimeOfDayConsumer.class);

  private static final int DOMAIN_ID = 0;
  private static final String TOPIC_NAME = "TimeOfDay";

  private DomainParticipant participant;
  private final CountDownLatch shutdownLatch = new CountDownLatch(1);

  /**
   * Initializes all DDS entities: DomainParticipant, type registration, Topic,
   * Subscriber, and DataReader with a listener. Must be called before
   * {@link #awaitShutdown()}.
   *
   * @throws RuntimeException if any DDS entity creation fails
   */
  public void start() {
    participant = DomainParticipantFactory.get_instance().create_participant(
        DOMAIN_ID,
        DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    if (participant == null) {
      throw new RuntimeException("Failed to create DomainParticipant");
    }
    logger.info("DDS DomainParticipant created on domain {}", DOMAIN_ID);

    TimeOfDayMessageTypeSupport.register_type(
        participant, TimeOfDayMessageTypeSupport.get_type_name());

    Topic topic = participant.create_topic(
        TOPIC_NAME,
        TimeOfDayMessageTypeSupport.get_type_name(),
        DomainParticipant.TOPIC_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    if (topic == null) {
      throw new RuntimeException("Failed to create Topic");
    }

    Subscriber subscriber = participant.create_subscriber(
        DomainParticipant.SUBSCRIBER_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    if (subscriber == null) {
      throw new RuntimeException("Failed to create Subscriber");
    }

    DataReader reader = subscriber.create_datareader(
        topic,
        Subscriber.DATAREADER_QOS_DEFAULT,
        new TimeOfDayListener(),
        StatusKind.DATA_AVAILABLE_STATUS);

    if (reader == null) {
      throw new RuntimeException("Failed to create DataReader");
    }

    logger.info("Subscribed to topic '{}'", TOPIC_NAME);
  }

  /**
   * Blocks the calling thread until {@link #shutdown()} is invoked. Used to keep
   * the main thread alive while the DDS listener receives messages asynchronously.
   */
  public void awaitShutdown() {
    try {
      shutdownLatch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Tears down all DDS entities in the correct order and releases the
   * {@link #awaitShutdown()} latch. Registered as a JVM shutdown hook
   * in {@link #main(String[])}.
   */
  public void shutdown() {
    logger.info("Shutting down consumer");
    if (participant != null) {
      participant.delete_contained_entities();
      DomainParticipantFactory.get_instance().delete_participant(participant);
    }
    shutdownLatch.countDown();
  }

  /**
   * DDS listener that processes incoming {@link TimeOfDayMessage} samples.
   * Invoked on a DDS-managed thread whenever new data is available.
   */
  private static class TimeOfDayListener extends DataReaderAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TimeOfDayListener.class);

    /**
     * Called by DDS when one or more samples are available. Takes all available
     * samples and logs each valid message.
     *
     * @param reader the DDS DataReader that has data available
     */
    @Override
    public void on_data_available(DataReader reader) {
      TimeOfDayMessageDataReader messageReader = (TimeOfDayMessageDataReader) reader;
      TimeOfDayMessageSeq dataSeq = new TimeOfDayMessageSeq();
      SampleInfoSeq infoSeq = new SampleInfoSeq();

      try {
        messageReader.take(
            dataSeq,
            infoSeq,
            ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
            SampleStateKind.ANY_SAMPLE_STATE,
            ViewStateKind.ANY_VIEW_STATE,
            InstanceStateKind.ANY_INSTANCE_STATE);

        for (int i = 0; i < dataSeq.size(); i++) {
          SampleInfo info = (SampleInfo) infoSeq.get(i);
          if (info.valid_data) {
            TimeOfDayMessage message = (TimeOfDayMessage) dataSeq.get(i);
            logger.info("Received [{}]: {} — {}", message.messageId, message.timestamp, message.quote);
          }
        }
      } catch (RETCODE_NO_DATA noData) {
        // No data to read — not an error
      } finally {
        messageReader.return_loan(dataSeq, infoSeq);
      }
    }
  }

  /**
   * Entry point. Creates the consumer, registers a shutdown hook, initializes DDS,
   * and blocks until the process is terminated.
   *
   * @param args command-line arguments (unused)
   */
  public static void main(String[] args) {
    TimeOfDayConsumer consumer = new TimeOfDayConsumer();

    Runtime.getRuntime().addShutdownHook(new Thread(consumer::shutdown));

    consumer.start();

    logger.info("Consumer running. Press Ctrl+C to exit.");
    consumer.awaitShutdown();
  }
}
