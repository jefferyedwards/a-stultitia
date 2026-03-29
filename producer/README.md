# Producer Module

A standalone Java application that publishes `TimeOfDayMessage` instances to a DDS topic at a fixed interval.

## What It Does

The producer creates a DDS DomainParticipant, registers the `TimeOfDayMessage` type, and enters a publish loop that sends a message every 2 seconds. Each message contains:

- **timestamp** — current time in ISO 8601 format
- **messageId** — incrementing counter starting from 1
- **quote** — cycled sequentially from `quotes.txt`

After the last quote is published, the cycle restarts from the beginning.

## Key Class

**`net.edwardsonthe.producer.TimeOfDayProducer`**

| Method       | Description                                                                    |
| ------------ | ------------------------------------------------------------------------------ |
| `start()`    | Creates all DDS entities: DomainParticipant, Topic, Publisher, DataWriter      |
| `publish()`  | Builds and writes a single `TimeOfDayMessage`                                  |
| `shutdown()` | Tears down DDS entities in the correct order                                   |
| `main()`     | Entry point — registers a JVM shutdown hook, starts DDS, runs the publish loop |

## DDS Entity Lifecycle

```text
DomainParticipantFactory
  └─ DomainParticipant (domain 0)
       ├─ Topic ("TimeOfDay", TimeOfDayMessage type)
       └─ Publisher
            └─ DataWriter (TimeOfDayMessageDataWriter)
```

All entities are created in `start()` and destroyed in `shutdown()` via `delete_contained_entities()`.

## Configuration

All configuration is currently hard-coded as constants:

| Constant              | Value         | Description                    |
| --------------------- | ------------- | ------------------------------ |
| `DOMAIN_ID`           | `0`           | DDS domain ID                  |
| `TOPIC_NAME`          | `"TimeOfDay"` | DDS topic name                 |
| `PUBLISH_INTERVAL_MS` | `2000`        | Milliseconds between publishes |

## Resources

- [src/main/resources/quotes.txt](src/main/resources/quotes.txt) — 10 quotes loaded at startup and cycled through sequentially

## Dependencies

- **idl** — provides `TimeOfDayMessage`, `TimeOfDayMessageDataWriter`, `TimeOfDayMessageTypeSupport`
- **nddsjava** — RTI Connext DDS Java API
