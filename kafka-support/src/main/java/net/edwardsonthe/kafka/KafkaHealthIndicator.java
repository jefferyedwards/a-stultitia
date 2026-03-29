package net.edwardsonthe.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator that reports Kafka transport status.
 *
 * <p>Appears as {@code "kafka"} in the {@code /actuator/health} response.
 * Reports {@code UP} with the bootstrap servers address and topic name.
 *
 * <p>Only active when the {@code kafka} profile is enabled and the Spring Boot
 * Actuator is on the classpath.
 */
@Component
@Profile("kafka")
@ConditionalOnClass(HealthIndicator.class)
public class KafkaHealthIndicator implements HealthIndicator {

  private final String bootstrapServers;
  private final String topicName;

  public KafkaHealthIndicator(
      @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers,
      @Value("${kafka.topic-name:time-of-day}") String topicName) {
    this.bootstrapServers = bootstrapServers;
    this.topicName = topicName;
  }

  @Override
  public Health health() {
    return Health.up()
        .withDetail("bootstrapServers", bootstrapServers)
        .withDetail("topic", topicName)
        .build();
  }
}
