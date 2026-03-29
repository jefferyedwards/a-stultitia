package net.edwardsonthe.dds;

import net.edwardsonthe.messages.TimeOfDayMessageDataWriter;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.topic.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * DDS producer-specific configuration. Creates the DDS {@link Publisher} and
 * {@link TimeOfDayMessageDataWriter} beans.
 *
 * <p>Only active when the {@code dds} profile is enabled and
 * {@code dds.role=producer} is set in application properties.
 */
@Configuration
@Profile("dds")
@ConditionalOnProperty(name = "dds.role", havingValue = "producer")
public class DdsProducerConfig {

  private static final Logger log = LoggerFactory.getLogger(DdsProducerConfig.class);

  /**
   * Creates the DDS publisher and data writer for {@code TimeOfDayMessage}.
   *
   * @param participant the DDS DomainParticipant
   * @param topic       the DDS Topic for TimeOfDayMessage
   * @return the configured DataWriter for publishing messages
   * @throws RuntimeException if any DDS entity creation fails
   */
  @Bean(destroyMethod = "")
  public TimeOfDayMessageDataWriter dataWriter(DomainParticipant participant, Topic topic) {
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

    log.info("DDS DataWriter created for topic '{}'", topic.get_name());
    return writer;
  }
}
