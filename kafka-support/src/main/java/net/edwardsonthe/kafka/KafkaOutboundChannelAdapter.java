package net.edwardsonthe.kafka;

import net.edwardsonthe.common.TimeOfDayEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Kafka outbound channel adapter that bridges Spring Integration to Apache Kafka.
 *
 * <p>Consumes {@link TimeOfDayEvent} messages from the {@code timeOfDayOutboundChannel}
 * and publishes them to a Kafka topic. This is the Kafka equivalent of
 * {@code DdsOutboundChannelAdapter} — same channel, different transport.
 *
 * <p>Only active when the {@code kafka} profile is enabled and
 * {@code kafka.role=producer}.
 */
@Component
@Profile("kafka")
@ConditionalOnProperty(name = "kafka.role", havingValue = "producer")
public class KafkaOutboundChannelAdapter {

  private static final Logger log = LoggerFactory.getLogger(KafkaOutboundChannelAdapter.class);

  private final KafkaTemplate<String, TimeOfDayEvent> kafkaTemplate;
  private final String topicName;

  public KafkaOutboundChannelAdapter(KafkaTemplate<String, TimeOfDayEvent> kafkaTemplate,
                                     @Value("${kafka.topic-name:time-of-day}") String topicName) {
    this.kafkaTemplate = kafkaTemplate;
    this.topicName = topicName;
  }

  /**
   * Receives a {@link TimeOfDayEvent} from the outbound channel and publishes it
   * to the configured Kafka topic.
   *
   * @param message the Spring Integration message containing the event payload
   */
  @ServiceActivator(inputChannel = "timeOfDayOutboundChannel")
  public void handleMessage(Message<TimeOfDayEvent> message) {
    TimeOfDayEvent event = message.getPayload();
    kafkaTemplate.send(topicName, event);
    log.debug("Sent to Kafka topic '{}': [{}]", topicName, event.getMessageId());
  }
}
