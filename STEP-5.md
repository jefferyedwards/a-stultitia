# Step 5: Kafka Transport Swap — Zero Code Changes

## Goal

Prove that the Spring Integration abstraction introduced in Step 4 works — swap the messaging transport from RTI Connext DDS to Apache Kafka **without modifying any business logic code**.

## The Zero Code Changes Proof

These files are **identical between Step 4 and Step 5**:

- `TimeOfDayProducer.java` — still sends `TimeOfDayEvent` to `timeOfDayOutboundChannel`
- `TimeOfDayConsumer.java` — still receives `TimeOfDayEvent` via `@ServiceActivator`
- `IntegrationConfig.java` (both modules) — channels are transport-agnostic
- `TimeOfDayEvent.java` — the POJO doesn't change
- All `dds-support/` files — DDS transport still works with `--spring.profiles.active=dds`

The swap is accomplished entirely by:

1. Adding a new `kafka-support` transport module (following the same pattern as `dds-support`)
2. Adding `application-kafka.properties` to each application module
3. Starting with `--spring.profiles.active=kafka` instead of `dds`

## Architecture

**Profile: `dds`**

```mermaid
flowchart LR
  classDef shade1 fill:#23445D,stroke:#5D7F99,rx:10,color:#FFFFFF
  classDef shade2 fill:#AE4132,stroke:#FAD9D5,rx:10,color:#FFFFFF
  classDef shade3 fill:#777777,stroke:#CCCCCC,rx:10,color:#FFFFFF
  classDef shade4 fill:#2D6A4F,stroke:#95D5B2,rx:10,color:#FFFFFF

  PP["**TimeOfDayProducer**"] --> OC["outboundChannel"]
  OC --> DOA["**DdsOutbound<br/>Adapter**"]
  DOA --> DDS{{"DDS"}}
  DDS --> DIA["**DdsInbound<br/>Adapter**"]
  DIA --> IC["inboundChannel"]
  IC --> CC["**TimeOfDayConsumer**"]

  class PP shade1
  class CC shade2
  class DDS shade3
  class DOA,DIA shade4
  class OC,IC shade3
```

**Profile: `kafka`**

```mermaid
flowchart LR
  classDef shade1 fill:#23445D,stroke:#5D7F99,rx:10,color:#FFFFFF
  classDef shade2 fill:#AE4132,stroke:#FAD9D5,rx:10,color:#FFFFFF
  classDef shade3 fill:#777777,stroke:#CCCCCC,rx:10,color:#FFFFFF
  classDef shade4 fill:#2D6A4F,stroke:#95D5B2,rx:10,color:#FFFFFF

  PP["**TimeOfDayProducer**"] --> OC["outboundChannel"]
  OC --> KOA["**KafkaOutbound<br/>Adapter**"]
  KOA --> KAFKA{{"Kafka"}}
  KAFKA --> KIA["**KafkaInbound<br/>Adapter**"]
  KIA --> IC["inboundChannel"]
  IC --> CC["**TimeOfDayConsumer**"]

  class PP shade1
  class CC shade2
  class KAFKA shade3
  class KOA,KIA shade4
  class OC,IC shade3
```

**Same business logic. Same channels. Different transport. One property change.**

## Why Embedded Kafka?

For this tutorial, we use Spring Kafka's `EmbeddedKafkaKraftBroker` (KRaft mode, no Zookeeper). This runs an in-process Kafka broker alongside the application, requiring no Docker, no external infrastructure, and no installation.

**How it works:** The consumer starts the embedded broker and writes the address to a shared file. The producer reads this file at startup and connects.

**Production note:** Replace embedded Kafka with an external cluster — just remove `KafkaBrokerConfig` and set `spring.kafka.bootstrap-servers` directly.

## What Changed from Step 4

### New Module: `kafka-support/`

Mirrors the `dds-support/` module pattern, including the auto-configuration mechanism introduced in Step 4 — the module is self-registering via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, so no `scanBasePackages` is needed:

| Class                         | Role                                                       |
| ----------------------------- | ---------------------------------------------------------- |
| `KafkaAutoConfiguration`      | Auto-configuration entry point, enables component scanning |
| `KafkaBrokerConfig`           | Starts embedded KRaft broker, writes address to file       |
| `KafkaProducerConfig`         | `KafkaTemplate` with JSON serialization                    |
| `KafkaConsumerConfig`         | Listener container with JSON deserialization               |
| `KafkaOutboundChannelAdapter` | `@ServiceActivator` on outbound channel to Kafka topic     |
| `KafkaInboundChannelAdapter`  | `@KafkaListener` to inbound channel                        |
| `KafkaHealthIndicator`        | Actuator health check for Kafka transport                  |

### New: Profile-Specific Properties

Spring Boot loads `application-kafka.properties` when `spring.profiles.active=kafka`:

**Producer:** `kafka.role=producer`, `kafka.topic-name=time-of-day`

**Consumer:** `kafka.role=consumer`, `kafka.topic-name=time-of-day`, `kafka.broker.embedded=true`

### New: Kafka Run Scripts

Simplified scripts that do **not** require NDDSHOME:

- `demo/bin/run-consumer-kafka.sh` — starts embedded broker
- `demo/bin/run-producer-kafka.sh` — waits for broker address file, then connects

## Project Structure

```text
a-stultitia/
├── pom.xml                                          # MODIFIED — added kafka-support module
├── demo/
│   ├── README.md
│   └── bin/
│       ├── run-producer.sh                          # DDS transport
│       ├── run-consumer.sh                          # DDS transport
│       ├── run-producer-kafka.sh                    # NEW — Kafka transport
│       └── run-consumer-kafka.sh                    # NEW — Kafka transport
├── common/                                          # Unchanged
├── idl/                                             # Unchanged
├── dds-support/                                     # Unchanged
├── kafka-support/                                   # NEW — Kafka transport module
│   ├── pom.xml
│   └── src/main/
│       ├── java/net/edwardsonthe/kafka/
│       │   ├── KafkaAutoConfiguration.java
│       │   ├── KafkaBrokerConfig.java
│       │   ├── KafkaProducerConfig.java
│       │   ├── KafkaConsumerConfig.java
│       │   ├── KafkaOutboundChannelAdapter.java
│       │   ├── KafkaInboundChannelAdapter.java
│       │   └── KafkaHealthIndicator.java
│       └── resources/META-INF/spring/
│           └── ...AutoConfiguration.imports
├── producer/                                        # Business logic UNCHANGED
│   ├── pom.xml                                      # + kafka-support dependency
│   └── src/main/resources/
│       ├── application.properties                   # Default: dds
│       └── application-kafka.properties             # NEW
└── consumer/                                        # Business logic UNCHANGED
    ├── pom.xml                                      # + kafka-support dependency
    └── src/main/resources/
        ├── application.properties                   # Default: dds
        └── application-kafka.properties             # NEW
```

## Build

```bash
export NDDSHOME=/path/to/rti_connext_dds-7.6.0
mvn clean package
```

Note: `NDDSHOME` is needed at build time for the `dds-support` module. It is **not** needed at runtime when running with the Kafka profile.

## Run with Kafka

```bash
# Terminal 1 — start the consumer (starts embedded Kafka broker)
./demo/bin/run-consumer-kafka.sh

# Terminal 2 — start the producer (connects to the consumer's broker)
./demo/bin/run-producer-kafka.sh
```

No `NDDSHOME` required. No RTI installation needed at runtime.

## Run with DDS (Still Works)

```bash
# Terminal 1
./demo/bin/run-consumer.sh

# Terminal 2
./demo/bin/run-producer.sh
```

Both transports coexist in the same build artifact.

## The Modernization Journey — Complete

| Step | What Changed                                          | Key Concept                         |
| ---- | ----------------------------------------------------- | ----------------------------------- |
| 1    | Raw RTI DDS, shell scripts, SLF4J logging             | Baseline legacy application         |
| 2    | Spring Boot, externalized config, fat JARs            | Framework adoption                  |
| 3    | Actuators: health, metrics, loggers                   | Operational visibility              |
| 4    | Spring Integration, transport auto-configuration      | Messaging abstraction               |
| 5    | Kafka transport via profile swap                      | Configuration-driven infrastructure |

The producer and consumer **business logic has not changed since Step 4**. The messaging transport is an infrastructure concern, configured by properties — exactly as it should be.

## Production Considerations

- **Replace embedded Kafka** with an external cluster (or managed service). Remove `KafkaBrokerConfig` and set `spring.kafka.bootstrap-servers` directly.
- **Add Spring Security** to protect actuator endpoints.
- **Add tests** — business logic can be tested against mock channels without any messaging infrastructure.
- **Schema evolution** — consider Avro + Schema Registry for production Kafka serialization instead of JSON.
