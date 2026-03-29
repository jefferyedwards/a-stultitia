package net.edwardsonthe.consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

/**
 * Defines the Spring Integration channels used by the consumer.
 *
 * <p>This configuration is profile-independent — the channel exists regardless of
 * which messaging transport (DDS, Kafka, etc.) is active. A transport-specific
 * inbound adapter pushes messages into the channel; business logic receives from it
 * via {@link TimeOfDayConsumer}.
 */
@Configuration
public class IntegrationConfig {

  /**
   * The inbound channel that receives messages from the transport layer. A
   * transport-specific adapter sends messages to this channel, and the
   * {@link TimeOfDayConsumer} processes them via {@code @ServiceActivator}.
   *
   * @return a direct (synchronous) message channel
   */
  @Bean
  public MessageChannel timeOfDayInboundChannel() {
    return new DirectChannel();
  }
}
