# DDS Support Module

RTI Connext DDS support for the tutorial. Provides Spring Integration channel adapters, DDS configuration, health indicator, and shared DDS entity lifecycle management.

## What It Does

Bridges Spring Integration message channels to RTI Connext DDS. Business logic sends/receives `TimeOfDayEvent` POJOs via channels; this module handles conversion to/from DDS `TimeOfDayMessage` and manages the DDS entity lifecycle.

All classes are gated by `@Profile("dds")` — they are only active when DDS is the selected transport.

## Key Classes

| Class                       | Role                                                          |
| --------------------------- | ------------------------------------------------------------- |
| `DdsParticipantConfig`      | Shared: DomainParticipant, Topic, type registration, cleanup  |
| `DdsProducerConfig`         | Role-specific: Publisher + DataWriter (`dds.role=producer`)   |
| `DdsConsumerConfig`         | Role-specific: Subscriber + DataReader (`dds.role=consumer`)  |
| `DdsOutboundChannelAdapter` | Channel to DDS: converts `TimeOfDayEvent` to DDS and writes   |
| `DdsInboundChannelAdapter`  | DDS to Channel: converts DDS to `TimeOfDayEvent` and sends    |
| `DdsHealthIndicator`        | Actuator health check for DomainParticipant status            |

## Configuration

| Property         | Default     | Description                              |
| ---------------- | ----------- | ---------------------------------------- |
| `dds.role`       | (required)  | `producer` or `consumer`                 |
| `dds.domain-id`  | `0`         | DDS domain ID                            |
| `dds.topic-name` | `TimeOfDay` | DDS topic name                           |

The `dds.role` property controls which role-specific beans are created, allowing a single transport module to serve both roles.

## Consumer Startup Ordering

`DdsConsumerConfig` creates the DataReader in response to `ApplicationReadyEvent` rather than during bean initialization. This ensures Spring Integration channel subscribers (`@ServiceActivator`) are fully wired before DDS starts delivering messages.

## Component Scanning

Since this module lives in the `net.edwardsonthe.dds` package, Spring Boot's `@SpringBootApplication` with `scanBasePackages = "net.edwardsonthe"` picks it up automatically.

## Dependencies

- **spring-integration-core** — Spring Integration channel and messaging framework
- **spring-boot-starter** — Spring Boot auto-configuration
- **spring-boot-starter-actuator** (optional) — health indicator support
- **common** — `TimeOfDayEvent` POJO
- **idl** — DDS-generated `TimeOfDayMessage` type support classes
- **nddsjava** — RTI Connext DDS Java API
