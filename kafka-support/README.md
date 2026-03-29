# Kafka Support Module

Apache Kafka transport for the tutorial. Provides Spring Integration channel adapters, embedded broker for development, and health indicator — mirroring the `dds-support` module pattern.

## What It Does

Bridges Spring Integration message channels to Apache Kafka. Business logic sends/receives `TimeOfDayEvent` POJOs via channels; this module handles serialization to/from Kafka and manages the broker connection.

All classes are gated by `@Profile("kafka")` — they are only active when Kafka is the selected transport.

## Key Classes

| Class                         | Role                                                       |
| ----------------------------- | ---------------------------------------------------------- |
| `KafkaAutoConfiguration`      | Auto-configuration entry point, enables component scanning |
| `KafkaBrokerConfig`           | Starts embedded KRaft broker, writes address to file       |
| `KafkaProducerConfig`         | `KafkaTemplate` with JSON serialization                    |
| `KafkaConsumerConfig`         | Listener container with JSON deserialization               |
| `KafkaOutboundChannelAdapter` | `@ServiceActivator` on outbound channel to Kafka topic     |
| `KafkaInboundChannelAdapter`  | `@KafkaListener` to inbound channel                        |
| `KafkaHealthIndicator`        | Actuator health check for Kafka transport                  |

## Configuration

| Property                | Default              | Description                         |
| ----------------------- | -------------------- | ----------------------------------- |
| `kafka.role`            | (required)           | `producer` or `consumer`            |
| `kafka.topic-name`      | `time-of-day`        | Kafka topic name                    |
| `kafka.group-id`        | `timeofday-consumer` | Consumer group ID                   |
| `kafka.broker.embedded` | `false`              | Start embedded KRaft broker if true |

## Embedded Broker

`KafkaBrokerConfig` runs an in-process Kafka broker in KRaft mode (no Zookeeper). The broker address is written to a shared temp file so the producer can discover it. For production, remove this config and set `spring.kafka.bootstrap-servers` directly.

## Dependencies

- **spring-integration-core** — Spring Integration channel and messaging framework
- **spring-boot-starter** — Spring Boot auto-configuration
- **spring-kafka** — Kafka client and listener support
- **spring-kafka-test** — embedded KRaft broker
- **spring-boot-starter-actuator** (optional) — health indicator support
- **jackson-databind** — JSON serialization/deserialization
- **common** — `TimeOfDayEvent` POJO
