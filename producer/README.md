# Producer Module

A Spring Boot application that publishes `TimeOfDayMessage` instances to a DDS topic at a configurable interval.

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

`@Configuration` class that creates DDS entities as Spring beans from externalized properties:

| Bean                         | Description                                   |
| ---------------------------- | --------------------------------------------- |
| `DomainParticipant`          | Created with default QoS on configured domain |
| `TimeOfDayMessageDataWriter` | Typed writer for publishing messages          |

`@PreDestroy` handles orderly DDS teardown on application shutdown.

### `TimeOfDayProducer`

`@Component` implementing `CommandLineRunner`. Receives the `TimeOfDayMessageDataWriter` via constructor injection and runs the publish loop after the Spring context is fully initialized.

| Method      | Description                                                         |
| ----------- | ------------------------------------------------------------------- |
| `run()`     | CommandLineRunner entry — starts the publish loop                   |
| `publish()` | Builds and writes a single `TimeOfDayMessage`                       |

## Configuration

All configuration is externalized in `application.properties`:

| Property                       | Default                | Description                    |
| ------------------------------ | ---------------------- | ------------------------------ |
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

- **spring-boot-starter** — Spring Boot auto-configuration, SLF4J/Logback logging
- **idl** — provides generated `TimeOfDayMessage` type support classes
- **nddsjava** — RTI Connext DDS Java API
