# Consumer Module

A Spring Boot application that receives `TimeOfDayEvent` messages via Spring Integration and logs each one. The consumer has **zero dependency on any messaging transport** — it receives from a channel via `@ServiceActivator`, and the active Spring profile determines the transport (DDS, Kafka, etc.).

## What It Does

The consumer receives `TimeOfDayEvent` messages from the `timeOfDayInboundChannel` and logs all fields (messageId, timestamp, quote). A transport-specific inbound adapter pushes messages into the channel based on the active profile.

## Key Classes

### `ConsumerApplication`

`@SpringBootApplication` entry point. Transport module beans are discovered automatically via Spring Boot's auto-configuration mechanism.

### `IntegrationConfig`

Defines the `timeOfDayInboundChannel` as a `DirectChannel`. This channel is profile-independent — it exists regardless of which transport is active.

### `TimeOfDayConsumer`

`@Component` with `@ServiceActivator(inputChannel = "timeOfDayInboundChannel")`. Processes incoming `TimeOfDayEvent` messages. Tracks received messages via `messages.received` Micrometer counter.

## Actuator Endpoints

Available at `http://localhost:8082/actuator/`.

## Configuration

| Property                 | Default     | Description              |
| ------------------------ | ----------- | ------------------------ |
| `spring.profiles.active` | `dds`       | Active transport profile |
| `server.port`            | `8082`      | HTTP port for actuator   |
| `dds.role`               | `consumer`  | DDS transport role       |
| `dds.domain-id`          | `0`         | DDS domain ID            |
| `dds.topic-name`         | `TimeOfDay` | DDS topic name           |

## Dependencies

- **spring-boot-starter-web** — embedded Tomcat for HTTP endpoints
- **spring-boot-starter-actuator** — actuator framework and Micrometer metrics
- **spring-integration-core** — message channels and messaging framework
- **common** — `TimeOfDayEvent` POJO
- **dds-support** — DDS transport (activated by `dds` profile)
