package net.edwardsonthe.producer;

import net.edwardsonthe.dds.DdsParticipantConfig;
import net.edwardsonthe.messages.TimeOfDayMessageDataWriter;
import net.edwardsonthe.messages.TimeOfDayMessageTypeSupport;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.topic.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the DDS producer entities.
 *
 * <p>Creates the topic, publisher, and data writer for {@code TimeOfDayMessage}.
 * The {@link DomainParticipant} is provided by {@link DdsParticipantConfig}
 * in the {@code dds-support} module.
 */
@Configuration
public class DdsConfig {

  private static final Logger log = LoggerFactory.getLogger(DdsConfig.class);

  /**
   * Creates the DDS topic, publisher, and data writer for {@code TimeOfDayMessage}.
   *
   * @param participant       the DDS DomainParticipant
   * @param participantConfig the shared config providing the topic name
   * @return the configured DataWriter for publishing messages
   * @throws RuntimeException if any DDS entity creation fails
   */
  @Bean(destroyMethod = "")
  public TimeOfDayMessageDataWriter dataWriter(DomainParticipant participant,
                                               DdsParticipantConfig participantConfig) {
    Topic topic = participant.create_topic(
        participantConfig.getTopicName(),
        TimeOfDayMessageTypeSupport.get_type_name(),
        DomainParticipant.TOPIC_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    if (topic == null) {
      throw new RuntimeException("Failed to create Topic");
    }

    Publisher publisher = participant.create_publisher(
        DomainParticipant.PUBLISHER_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    if (publisher == null) {
      throw new RuntimeException("Failed to create Publisher");
    }

    TimeOfDayMessageDataWriter writer = (TimeOfDayMessageDataWriter) publisher.create_datawriter(
        topic,
        Publisher.DATAWRITER_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    if (writer == null) {
      throw new RuntimeException("Failed to create DataWriter");
    }

    log.info("Publishing on topic '{}'", participantConfig.getTopicName());
    return writer;
  }
}
