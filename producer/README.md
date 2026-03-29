# Producer Module

A Spring Boot application that publishes `TimeOfDayMessage` instances to a DDS topic at a configurable interval. Includes actuator endpoints for health monitoring, metrics, and runtime log management.

## What It Does

The producer initializes DDS entities via Spring-managed `@Configuration` beans, then enters a publish loop that sends a message at a configurable interval (default: every 2 seconds). Each message contains:

- **timestamp** — current time in ISO 8601 format
- **messageId** — incrementing counter starting from 1
- **quote** — cycled sequentially from a configurable quotes file

After the last quote is published, the cycle restarts from the beginning.

## Key Classes

### `ProducerApplication`

`@SpringBootApplication` entry point. Bootstraps the Spring context which initializes DDS and starts the publish loop.

### `DdsConfig`

`@Configuration` class that creates the DDS topic, publisher, and data writer. The `DomainParticipant` is provided by `DdsParticipantConfig` in the `dds-support` module.

### `TimeOfDayProducer`

`@Component` implementing `CommandLineRunner`. Receives the `TimeOfDayMessageDataWriter` and `MeterRegistry` via constructor injection. Publishes messages and increments the `dds.messages.published` Micrometer counter on each publish.

## Actuator Endpoints

Available at `http://localhost:8081/actuator/`:

| Endpoint                                   | Purpose                                |
| ------------------------------------------ | -------------------------------------- |
| `/actuator/health`                         | DDS health indicator (domain ID, role) |
| `/actuator/metrics/dds.messages.published` | Total messages published               |
| `/actuator/loggers/net.edwardsonthe`       | View/change log levels at runtime      |
| `/actuator/info`                           | Application name, description, step    |
| `/actuator/env`                            | Resolved configuration properties      |

## Configuration

All configuration is externalized in `application.properties`:

| Property                       | Default                | Description                    |
| ------------------------------ | ---------------------- | ------------------------------ |
| `server.port`                  | `8081`                 | HTTP port for actuator         |
| `dds.domain-id`                | `0`                    | DDS domain ID                  |
| `dds.topic-name`               | `TimeOfDay`            | DDS topic name                 |
| `producer.publish-interval-ms` | `2000`                 | Milliseconds between publishes |
| `producer.quotes-file`         | `classpath:quotes.txt` | Resource path to quotes file   |

Override at runtime without recompilation:

```bash
java -jar producer-1.0.0-SNAPSHOT.jar --dds.domain-id=1 --producer.publish-interval-ms=5000
```

## Resources

- [src/main/resources/quotes.txt](src/main/resources/quotes.txt) — 10 quotes loaded at startup and cycled through sequentially
- [src/main/resources/application.properties](src/main/resources/application.properties) — default configuration

## Dependencies

- **spring-boot-starter-web** — embedded Tomcat for HTTP endpoints
- **spring-boot-starter-actuator** — actuator framework and Micrometer metrics
- **dds-support** — shared DDS participant config and health indicator
- **nddsjava** — RTI Connext DDS Java API
