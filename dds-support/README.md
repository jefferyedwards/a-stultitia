# DDS Support Module

Shared Spring configuration for the RTI Connext DDS `DomainParticipant` lifecycle. Used by both the producer and consumer modules to avoid duplicating DDS setup code.

## What It Does

Provides a single `@Configuration` class that creates and manages the DDS `DomainParticipant` as a Spring bean using default QoS. Domain ID and topic name are externalized to `application.properties`.

## Key Class

### `DdsParticipantConfig`

`@Configuration` class that:

1. Creates the `DomainParticipant` with default QoS on the configured domain ID
2. Registers the `TimeOfDayMessage` type
3. Tears down all DDS entities on Spring context shutdown via `@PreDestroy`

| Bean                | Description                                                        |
| ------------------- | ------------------------------------------------------------------ |
| `DomainParticipant` | Configured programmatically (transport, discovery) from properties |

The `getTopicName()` method exposes the configured topic name so that module-specific configs (producer/consumer) can create their topic, writer, or reader beans.

## Configuration

These properties are read by `DdsParticipantConfig`:

| Property         | Default     | Description    |
| ---------------- | ----------- | -------------- |
| `dds.domain-id`  | `0`         | DDS domain ID  |
| `dds.topic-name` | `TimeOfDay` | DDS topic name |

Properties are defined in each application's `application.properties` and can be overridden at runtime via command-line arguments.

## Component Scanning

Since this module lives in the `net.edwardsonthe.dds` package, Spring Boot's `@SpringBootApplication` in the producer (`net.edwardsonthe.producer`) and consumer (`net.edwardsonthe.consumer`) will auto-scan it — all packages share the `net.edwardsonthe` base.

## Dependencies

- **spring-boot-starter** — Spring Boot auto-configuration
- **idl** — provides `TimeOfDayMessageTypeSupport` for type registration
- **nddsjava** — RTI Connext DDS Java API
