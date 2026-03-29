package net.edwardsonthe.consumer;

import net.edwardsonthe.messages.TimeOfDayMessage;
import net.edwardsonthe.messages.TimeOfDayMessageDataReader;
import net.edwardsonthe.messages.TimeOfDayMessageSeq;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.ViewStateKind;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DDS listener that processes incoming {@link TimeOfDayMessage} instances.
 *
 * <p>Extends {@link DataReaderAdapter} and is registered with the DDS DataReader
 * in {@link DdsConfig}. The {@link #on_data_available(DataReader)} callback is
 * invoked on a DDS-managed thread whenever new messages are available.
 *
 * <p>Uses {@code take()} to remove messages from the reader's queue after processing,
 * and returns the loan to release DDS internal buffers.
 */
@Component
public class TimeOfDayConsumer extends DataReaderAdapter {

  private static final Logger log = LoggerFactory.getLogger(TimeOfDayConsumer.class);

  private final Counter receivedCounter;

  /**
   * Constructs the consumer with a Micrometer registry for recording metrics.
   *
   * @param meterRegistry the Micrometer registry for recording received message counts
   */
  public TimeOfDayConsumer(MeterRegistry meterRegistry) {
    this.receivedCounter = Counter.builder("dds.messages.received")
        .description("Total messages received from DDS")
        .register(meterRegistry);
  }

  /**
   * Called by DDS when one or more {@link TimeOfDayMessage} samples are available.
   *
   * <p>Takes all available samples from the reader and logs each valid message.
   * This method executes on a DDS internal thread, not a Spring-managed thread.
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
          receivedCounter.increment();
          log.info("Received [{}]: {} — {}", message.messageId, message.timestamp, message.quote);
        }
      }
    } catch (RETCODE_NO_DATA noData) {
      // No data to read — not an error
    } finally {
      messageReader.return_loan(dataSeq, infoSeq);
    }
  }
}
