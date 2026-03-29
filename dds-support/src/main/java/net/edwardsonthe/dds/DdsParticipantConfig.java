package net.edwardsonthe.dds;

import net.edwardsonthe.messages.TimeOfDayMessageTypeSupport;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.StatusKind;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared Spring configuration for the DDS {@link DomainParticipant}.
 *
 * <p>Creates and manages the lifecycle of the DomainParticipant bean using
 * default QoS settings. The participant is torn down in the correct order
 * via {@link #cleanup()}, which is invoked automatically by Spring on
 * context shutdown.
 */
@Configuration
public class DdsParticipantConfig {

  private static final Logger log = LoggerFactory.getLogger(DdsParticipantConfig.class);

  @Value("${dds.domain-id:0}")
  private int domainId;

  @Value("${dds.topic-name:TimeOfDay}")
  private String topicName;

  private DomainParticipant participant;

  /**
   * Creates the DDS {@link DomainParticipant} with default QoS and registers
   * the {@link TimeOfDayMessageTypeSupport} so the participant can publish
   * or subscribe to {@code TimeOfDayMessage} instances.
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
   * Returns the configured DDS topic name.
   *
   * @return the topic name from properties
   */
  public String getTopicName() {
    return topicName;
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
