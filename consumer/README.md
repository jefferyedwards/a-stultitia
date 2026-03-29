# Consumer Module

A standalone Java application that subscribes to `TimeOfDayMessage` instances from a DDS topic and logs each received message.

## What It Does

The consumer creates a DDS DomainParticipant, registers the `TimeOfDayMessage` type, and attaches an asynchronous listener to a DataReader. When messages arrive, the listener logs all fields (messageId, timestamp, quote) to stdout. The main thread blocks on a `CountDownLatch` until a shutdown signal (Ctrl+C) is received.

## Key Class

**`net.edwardsonthe.consumer.TimeOfDayConsumer`**

| Method            | Description                                                                              |
| ----------------- | ---------------------------------------------------------------------------------------- |
| `start()`         | Creates all DDS entities: DomainParticipant, Topic, Subscriber, DataReader with listener |
| `awaitShutdown()` | Blocks the main thread until `shutdown()` is called                                      |
| `shutdown()`      | Tears down DDS entities and releases the latch                                           |
| `main()`          | Entry point — registers a JVM shutdown hook, starts DDS, blocks until terminated         |

### Inner Class: `TimeOfDayListener`

Extends `DataReaderAdapter` and overrides `on_data_available()`:

1. Casts the generic `DataReader` to `TimeOfDayMessageDataReader`
2. Calls `take()` to consume all available samples
3. Iterates over the sample sequence, logging each valid message
4. Returns the loan to DDS in a `finally` block

The listener callback executes on a **DDS-managed thread**, not the main thread.

## DDS Entity Lifecycle

```text
DomainParticipantFactory
  └─ DomainParticipant (domain 0)
       ├─ Topic ("TimeOfDay", TimeOfDayMessage type)
       └─ Subscriber
            └─ DataReader (with TimeOfDayListener)
```

All entities are created in `start()` and destroyed in `shutdown()` via `delete_contained_entities()`.

## Configuration

All configuration is currently hard-coded as constants:

| Constant     | Value         | Description    |
| ------------ | ------------- | -------------- |
| `DOMAIN_ID`  | `0`           | DDS domain ID  |
| `TOPIC_NAME` | `"TimeOfDay"` | DDS topic name |

## Dependencies

- **idl** — provides `TimeOfDayMessage`, `TimeOfDayMessageDataReader`, `TimeOfDayMessageSeq`, `TimeOfDayMessageTypeSupport`
- **nddsjava** — RTI Connext DDS Java API
