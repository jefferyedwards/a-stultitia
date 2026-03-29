package net.edwardsonthe.kafka;

import net.edwardsonthe.common.TimeOfDayEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

/**
 * Kafka inbound channel adapter that bridges Apache Kafka to Spring Integration.
 *
 * <p>Listens on a Kafka topic and forwards each received {@link TimeOfDayEvent} to
 * the {@code timeOfDayInboundChannel}. This is the Kafka equivalent of
 * {@code DdsInboundChannelAdapter} — same channel, different transport.
 *
 * <p>Only active when the {@code kafka} profile is enabled and
 * {@code kafka.role=consumer}.
 */
@Component
@Profile("kafka")
@ConditionalOnProperty(name = "kafka.role", havingValue = "consumer")
public class KafkaInboundChannelAdapter {

  private static final Logger log = LoggerFactory.getLogger(KafkaInboundChannelAdapter.class);

  private final MessageChannel inboundChannel;

  public KafkaInboundChannelAdapter(
      @Qualifier("timeOfDayInboundChannel") MessageChannel inboundChannel) {
    this.inboundChannel = inboundChannel;
  }

  /**
   * Receives a {@link TimeOfDayEvent} from the Kafka topic and sends it to the
   * inbound Spring Integration channel.
   *
   * @param event the deserialized event from Kafka
   */
  @KafkaListener(
      topics = "${kafka.topic-name:time-of-day}",
      groupId = "${kafka.group-id:timeofday-consumer}")
  public void onMessage(TimeOfDayEvent event) {
    inboundChannel.send(MessageBuilder.withPayload(event).build());
    log.debug("Forwarded from Kafka: [{}]", event.getMessageId());
  }
}
