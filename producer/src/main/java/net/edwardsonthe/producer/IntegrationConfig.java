package net.edwardsonthe.producer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

/**
 * Defines the Spring Integration channels used by the producer.
 *
 * <p>This configuration is profile-independent — the channel exists regardless of
 * which messaging transport (DDS, Kafka, etc.) is active. Business logic sends
 * messages to the channel; the active profile's channel adapter consumes from it
 * and delivers to the transport.
 */
@Configuration
public class IntegrationConfig {

  /**
   * The outbound channel that the producer sends messages to. A transport-specific
   * adapter subscribes to this channel and forwards messages to the underlying
   * messaging system.
   *
   * @return a direct (synchronous) message channel
   */
  @Bean
  public MessageChannel timeOfDayOutboundChannel() {
    return new DirectChannel();
  }
}
