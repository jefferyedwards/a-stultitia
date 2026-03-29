# Consumer Module

A Spring Boot application that subscribes to `TimeOfDayMessage` instances from a DDS topic and logs each received message.

## What It Does

The consumer initializes DDS entities via Spring-managed `@Configuration` beans and registers an asynchronous listener on the DataReader. When messages arrive, the listener logs all fields (messageId, timestamp, quote). The Spring Boot application stays alive as long as the context is open — no `CountDownLatch` needed.

## Key Classes

### `ConsumerApplication`

`@SpringBootApplication` entry point. Bootstraps the Spring context which initializes DDS and registers the message listener.

### `DdsConfig`

`@Configuration` class that creates DDS entities as Spring beans from externalized properties:

| Bean                         | Description                                                  |
| ---------------------------- | ------------------------------------------------------------ |
| `DomainParticipant`          | Created with default QoS on configured domain                |
| `TimeOfDayMessageDataReader` | Typed reader with `TimeOfDayConsumer` registered as listener |

`@PreDestroy` handles orderly DDS teardown on application shutdown.

### `TimeOfDayConsumer`

`@Component` extending `DataReaderAdapter`. Registered as the DDS data reader listener in `DdsConfig`. The `on_data_available()` callback:

1. Casts the generic `DataReader` to `TimeOfDayMessageDataReader`
2. Calls `take()` to consume all available samples
3. Iterates over the sample sequence, logging each valid message
4. Returns the loan to DDS in a `finally` block

The callback executes on a **DDS-managed thread**, not a Spring-managed thread.

## Configuration

All configuration is externalized in `application.properties`:

| Property              | Default     | Description                     |
| --------------------- | ----------- | ------------------------------- |
| `dds.domain-id`       | `0`         | DDS domain ID                   |
| `dds.topic-name`      | `TimeOfDay` | DDS topic name                  |
| `dds.transport`       | `UDPv4`     | DDS transport                   |
| `dds.discovery.peers` | `127.0.0.1` | Comma-separated discovery peers |

Override at runtime without recompilation:

```bash
java -jar consumer-1.0.0-SNAPSHOT.jar --dds.domain-id=1
```

## Dependencies

- **spring-boot-starter** — Spring Boot auto-configuration, SLF4J/Logback logging
- **idl** — provides generated `TimeOfDayMessage` type support classes
- **nddsjava** — RTI Connext DDS Java API
