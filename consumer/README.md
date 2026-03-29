# Consumer Module

A Spring Boot application that subscribes to `TimeOfDayMessage` instances from a DDS topic and logs each received message. Includes actuator endpoints for health monitoring, metrics, and runtime log management.

## What It Does

The consumer initializes DDS entities via Spring-managed `@Configuration` beans and registers an asynchronous listener on the DataReader. When messages arrive, the listener logs all fields (messageId, timestamp, quote) and increments a Micrometer counter. The embedded Tomcat server keeps the application alive and serves actuator endpoints.

## Key Classes

### `ConsumerApplication`

`@SpringBootApplication` entry point. Bootstraps the Spring context which initializes DDS and registers the message listener.

### `DdsConfig`

`@Configuration` class that creates the DDS topic, subscriber, and data reader with `TimeOfDayConsumer` as the listener. The `DomainParticipant` is provided by `DdsParticipantConfig` in the `dds-support` module.

### `TimeOfDayConsumer`

`@Component` extending `DataReaderAdapter`. Receives `MeterRegistry` via constructor injection and increments the `dds.messages.received` counter on each valid message. The `on_data_available()` callback executes on a **DDS-managed thread**.

## Actuator Endpoints

Available at `http://localhost:8082/actuator/`:

| Endpoint                                  | Purpose                                |
| ----------------------------------------- | -------------------------------------- |
| `/actuator/health`                        | DDS health indicator (domain ID, role) |
| `/actuator/metrics/dds.messages.received` | Total messages received                |
| `/actuator/loggers/net.edwardsonthe`      | View/change log levels at runtime      |
| `/actuator/info`                          | Application name, description, step    |
| `/actuator/env`                           | Resolved configuration properties      |

## Configuration

All configuration is externalized in `application.properties`:

| Property         | Default     | Description            |
| ---------------- | ----------- | ---------------------- |
| `server.port`    | `8082`      | HTTP port for actuator |
| `dds.domain-id`  | `0`         | DDS domain ID          |
| `dds.topic-name` | `TimeOfDay` | DDS topic name         |

Override at runtime without recompilation:

```bash
java -jar consumer-1.0.0-SNAPSHOT.jar --dds.domain-id=1
```

## Dependencies

- **spring-boot-starter-web** — embedded Tomcat for HTTP endpoints
- **spring-boot-starter-actuator** — actuator framework and Micrometer metrics
- **dds-support** — shared DDS participant config and health indicator
- **nddsjava** — RTI Connext DDS Java API
