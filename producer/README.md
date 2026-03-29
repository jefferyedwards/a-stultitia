# Producer Module

A Spring Boot application that publishes `TimeOfDayEvent` messages at a configurable interval via Spring Integration. The producer has **zero dependency on any messaging transport** — it sends to a channel, and the active Spring profile determines the transport (DDS, Kafka, etc.).

## What It Does

The producer enters a publish loop that sends a `TimeOfDayEvent` to the `timeOfDayOutboundChannel` at a configurable interval (default: every 2 seconds). Each event contains:

- **timestamp** — current time in ISO 8601 format
- **messageId** — incrementing counter starting from 1
- **quote** — cycled sequentially from a configurable quotes file

## Key Classes

### `ProducerApplication`

`@SpringBootApplication` entry point with `scanBasePackages = "net.edwardsonthe"` to pick up the transport module beans.

### `IntegrationConfig`

Defines the `timeOfDayOutboundChannel` as a `DirectChannel`. This channel is profile-independent — it exists regardless of which transport is active.

### `TimeOfDayProducer`

`@Component` implementing `CommandLineRunner`. Sends `TimeOfDayEvent` to the outbound channel via `MessageBuilder`. Tracks published messages via `messages.published` Micrometer counter.

## Actuator Endpoints

Available at `http://localhost:8081/actuator/`.

## Configuration

| Property                       | Default                | Description                    |
| ------------------------------ | ---------------------- | ------------------------------ |
| `spring.profiles.active`       | `dds`                  | Active transport profile       |
| `server.port`                  | `8081`                 | HTTP port for actuator         |
| `dds.role`                     | `producer`             | DDS transport role             |
| `dds.domain-id`                | `0`                    | DDS domain ID                  |
| `dds.topic-name`               | `TimeOfDay`            | DDS topic name                 |
| `producer.publish-interval-ms` | `2000`                 | Milliseconds between publishes |
| `producer.quotes-file`         | `classpath:quotes.txt` | Resource path to quotes file   |
| `kafka.role`                   | `producer`             | Kafka transport role           |
| `kafka.topic-name`             | `time-of-day`          | Kafka topic name               |

DDS properties are in `application.properties`; Kafka properties are in `application-kafka.properties` (loaded when `spring.profiles.active=kafka`).

## Dependencies

- **spring-boot-starter-web** — embedded Tomcat for HTTP endpoints
- **spring-boot-starter-actuator** — actuator framework and Micrometer metrics
- **spring-integration-core** — message channels and messaging framework
- **common** — `TimeOfDayEvent` POJO
- **dds-support** — DDS transport (activated by `dds` profile)
- **kafka-support** — Kafka transport (activated by `kafka` profile)
