package net.edwardsonthe.kafka;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Auto-configuration entry point for the Kafka transport module.
 *
 * <p>Enables component scanning of the {@code net.edwardsonthe.kafka} package so that
 * Kafka adapters and health indicators are discovered automatically by any Spring Boot
 * application that includes {@code kafka-support} on its classpath.
 *
 * <p>Only active when the {@code kafka} profile is enabled.
 */
@Configuration
@Profile("kafka")
@ComponentScan(basePackages = "net.edwardsonthe.kafka")
public class KafkaAutoConfiguration {
}
