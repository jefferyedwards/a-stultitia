package net.edwardsonthe.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Starts an embedded Kafka broker for demonstration purposes.
 *
 * <p>Uses KRaft mode (no Zookeeper) via {@link EmbeddedKafkaKraftBroker}. The broker
 * runs in-process alongside the application, creating the configured topic on startup.
 *
 * <p>Only active when {@code kafka.broker.embedded=true} is set. Typically the consumer
 * starts the broker and the producer connects to it. The broker's address is written
 * to a shared file so the producer process can discover it at startup.
 *
 * <p><strong>Note:</strong> Embedded Kafka is for tutorials and testing only.
 * Production deployments should use an external Kafka cluster.
 */
@Configuration
@Profile("kafka")
@ConditionalOnProperty(name = "kafka.broker.embedded", havingValue = "true")
public class KafkaBrokerConfig {

  private static final Logger log = LoggerFactory.getLogger(KafkaBrokerConfig.class);

  @Value("${kafka.topic-name:time-of-day}")
  private String topicName;

  @Value("${kafka.broker.port-file:#{systemProperties['java.io.tmpdir']}/.kafka-bootstrap-servers}")
  private String portFile;

  /**
   * Creates and starts an embedded Kafka broker with a single broker, single partition,
   * and the configured topic pre-created.
   *
   * <p>The broker's bootstrap servers address is set as a system property so that
   * Spring Boot's Kafka auto-configuration picks it up. The address is also written
   * to a shared file so the producer process can discover the broker.
   *
   * @return the running embedded broker
   */
  @Bean(destroyMethod = "destroy")
  public EmbeddedKafkaBroker embeddedKafkaBroker() {
    EmbeddedKafkaKraftBroker broker = new EmbeddedKafkaKraftBroker(1, 1, topicName);
    broker.afterPropertiesSet();

    String bootstrapServers = broker.getBrokersAsString();
    System.setProperty("spring.kafka.bootstrap-servers", bootstrapServers);

    try {
      Files.writeString(Path.of(portFile), bootstrapServers);
      log.info("Embedded Kafka broker started at {} (written to {})", bootstrapServers, portFile);
    } catch (IOException e) {
      log.warn("Could not write broker address to {}: {}", portFile, e.getMessage());
      log.info("Embedded Kafka broker started at {}", bootstrapServers);
    }

    return broker;
  }
}
