package net.edwardsonthe.kafka;

import net.edwardsonthe.common.TimeOfDayEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer-specific configuration. Creates the listener container factory
 * for receiving {@link TimeOfDayEvent} messages from a Kafka topic.
 *
 * <p>Only active when the {@code kafka} profile is enabled and
 * {@code kafka.role=consumer}.
 */
@Configuration
@Profile("kafka")
@ConditionalOnProperty(name = "kafka.role", havingValue = "consumer")
public class KafkaConsumerConfig {

  @Value("${kafka.group-id:timeofday-consumer}")
  private String groupId;

  /**
   * Creates a Kafka consumer factory configured with JSON deserialization for
   * {@link TimeOfDayEvent} values.
   *
   * @return the configured consumer factory
   */
  @Bean
  public ConsumerFactory<String, TimeOfDayEvent> consumerFactory() {
    String bootstrapServers = System.getProperty(
        "spring.kafka.bootstrap-servers", "localhost:9092");

    Map<String, Object> props = new HashMap<>();
    props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

    JsonDeserializer<TimeOfDayEvent> deserializer = new JsonDeserializer<>(TimeOfDayEvent.class);
    deserializer.addTrustedPackages("net.edwardsonthe.common");
    deserializer.setUseTypeHeaders(false);

    return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
  }

  /**
   * Creates the Kafka listener container factory used by {@link KafkaInboundChannelAdapter}.
   *
   * @return the configured listener container factory
   */
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, TimeOfDayEvent> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, TimeOfDayEvent> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }
}
