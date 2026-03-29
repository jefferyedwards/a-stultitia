package net.edwardsonthe.kafka;

import net.edwardsonthe.common.TimeOfDayEvent;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka producer-specific configuration. Creates the {@link KafkaTemplate} for
 * publishing {@link TimeOfDayEvent} messages to a Kafka topic.
 *
 * <p>Only active when the {@code kafka} profile is enabled and
 * {@code kafka.role=producer}.
 */
@Configuration
@Profile("kafka")
@ConditionalOnProperty(name = "kafka.role", havingValue = "producer")
public class KafkaProducerConfig {

  @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
  private String bootstrapServers;

  /**
   * Creates a {@link KafkaTemplate} configured with JSON serialization for
   * {@link TimeOfDayEvent} values.
   *
   * @return the configured Kafka template
   */
  @Bean
  public KafkaTemplate<String, TimeOfDayEvent> kafkaTemplate() {
    Map<String, Object> props = new HashMap<>();
    props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    ProducerFactory<String, TimeOfDayEvent> factory = new DefaultKafkaProducerFactory<>(props);
    return new KafkaTemplate<>(factory);
  }
}
