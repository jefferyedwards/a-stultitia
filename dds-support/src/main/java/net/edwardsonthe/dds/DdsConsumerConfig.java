package net.edwardsonthe.dds;

import net.edwardsonthe.messages.TimeOfDayMessageDataReader;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.topic.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

/**
 * DDS consumer-specific configuration. Creates the DDS {@link Subscriber} and
 * {@link TimeOfDayMessageDataReader}, wiring the {@link DdsInboundChannelAdapter}
 * as the data reader listener.
 *
 * <p>The DataReader is created in response to {@link ApplicationReadyEvent} rather
 * than during bean initialization. This ensures that the Spring Integration channel
 * subscribers (e.g., {@code @ServiceActivator}) are fully wired before DDS starts
 * delivering messages, avoiding a startup race condition.
 *
 * <p>Only active when the {@code dds} profile is enabled and
 * {@code dds.role=consumer} is set in application properties.
 */
@Configuration
@Profile("dds")
@ConditionalOnProperty(name = "dds.role", havingValue = "consumer")
public class DdsConsumerConfig {

  private static final Logger log = LoggerFactory.getLogger(DdsConsumerConfig.class);

  private final DomainParticipant participant;
  private final Topic topic;
  private final DdsInboundChannelAdapter inboundAdapter;

  public DdsConsumerConfig(DomainParticipant participant,
                           Topic topic,
                           DdsInboundChannelAdapter inboundAdapter) {
    this.participant = participant;
    this.topic = topic;
    this.inboundAdapter = inboundAdapter;
  }

  /**
   * Creates the DDS subscriber and data reader after the application context is
   * fully initialized. This ensures Spring Integration channels have their
   * subscribers registered before DDS begins delivering messages.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void startSubscription() {
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
        inboundAdapter,
        StatusKind.DATA_AVAILABLE_STATUS);

    if (reader == null) {
      throw new RuntimeException("Failed to create DataReader");
    }

    log.info("DDS DataReader created for topic '{}'", topic.get_name());
  }
}
