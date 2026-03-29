package net.edwardsonthe.dds;

import net.edwardsonthe.messages.TimeOfDayMessageTypeSupport;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.topic.Topic;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Shared DDS infrastructure configuration for RTI Connext DDS.
 *
 * <p>Creates and manages the lifecycle of the {@link DomainParticipant} and
 * {@link Topic} beans that are common to both producers and consumers.
 *
 * <p>Only active when the {@code dds} Spring profile is enabled. Role-specific
 * beans (DataWriter, DataReader) are created by {@link DdsProducerConfig} and
 * {@link DdsConsumerConfig} respectively.
 */
@Configuration
@Profile("dds")
public class DdsParticipantConfig {

  private static final Logger log = LoggerFactory.getLogger(DdsParticipantConfig.class);

  @Value("${dds.domain-id:0}")
  private int domainId;

  @Value("${dds.topic-name:TimeOfDay}")
  private String topicName;

  private DomainParticipant participant;

  /**
   * Creates the DDS {@link DomainParticipant} with default QoS and registers
   * the {@link TimeOfDayMessageTypeSupport}.
   *
   * @return the configured DomainParticipant
   * @throws RuntimeException if participant creation fails
   */
  @Bean(destroyMethod = "")
  public DomainParticipant domainParticipant() {
    participant = DomainParticipantFactory.get_instance().create_participant(
        domainId,
        DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    if (participant == null) {
      throw new RuntimeException("Failed to create DomainParticipant");
    }

    log.info("DDS DomainParticipant created on domain {}", domainId);

    TimeOfDayMessageTypeSupport.register_type(
        participant, TimeOfDayMessageTypeSupport.get_type_name());

    return participant;
  }

  /**
   * Creates the DDS {@link Topic} for {@code TimeOfDayMessage}.
   *
   * @param participant the DDS DomainParticipant to create the topic on
   * @return the configured Topic
   * @throws RuntimeException if topic creation fails
   */
  @Bean(destroyMethod = "")
  public Topic timeOfDayTopic(DomainParticipant participant) {
    Topic topic = participant.create_topic(
        topicName,
        TimeOfDayMessageTypeSupport.get_type_name(),
        DomainParticipant.TOPIC_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    if (topic == null) {
      throw new RuntimeException("Failed to create Topic '" + topicName + "'");
    }

    return topic;
  }

  /**
   * Tears down all DDS entities in the correct order on Spring context shutdown.
   */
  @PreDestroy
  public void cleanup() {
    log.info("Shutting down DDS");
    if (participant != null) {
      participant.delete_contained_entities();
      DomainParticipantFactory.get_instance().delete_participant(participant);
    }
  }
}
