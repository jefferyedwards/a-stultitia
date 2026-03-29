package net.edwardsonthe.consumer;

import net.edwardsonthe.dds.DdsParticipantConfig;
import net.edwardsonthe.messages.TimeOfDayMessageDataReader;
import net.edwardsonthe.messages.TimeOfDayMessageTypeSupport;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.topic.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the DDS consumer entities.
 *
 * <p>Creates the topic, subscriber, and data reader for {@code TimeOfDayMessage}.
 * The {@link DomainParticipant} is provided by {@link DdsParticipantConfig}
 * in the {@code dds-support} module. The {@link TimeOfDayConsumer} is registered
 * as the data reader listener.
 */
@Configuration
public class DdsConfig {

  private static final Logger log = LoggerFactory.getLogger(DdsConfig.class);

  /**
   * Creates the DDS topic, subscriber, and data reader for {@code TimeOfDayMessage}.
   *
   * @param participant       the DDS DomainParticipant
   * @param participantConfig the shared config providing the topic name
   * @param consumer          the listener that processes incoming messages
   * @return the configured DataReader for receiving messages
   * @throws RuntimeException if any DDS entity creation fails
   */
  @Bean(destroyMethod = "")
  public TimeOfDayMessageDataReader dataReader(DomainParticipant participant,
                                               DdsParticipantConfig participantConfig,
                                               TimeOfDayConsumer consumer) {
    Topic topic = participant.create_topic(
        participantConfig.getTopicName(),
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

    TimeOfDayMessageDataReader reader = (TimeOfDayMessageDataReader) subscriber.create_datareader(
        topic,
        Subscriber.DATAREADER_QOS_DEFAULT,
        consumer,
        StatusKind.DATA_AVAILABLE_STATUS);

    if (reader == null) {
      throw new RuntimeException("Failed to create DataReader");
    }

    log.info("Subscribed to topic '{}'", participantConfig.getTopicName());
    return reader;
  }
}
