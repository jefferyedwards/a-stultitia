# Common Module

Shared domain types used by all modules. Contains transport-neutral POJOs that have no dependency on DDS, Kafka, or any messaging framework.

## Key Class

### `TimeOfDayEvent`

A simple POJO with three fields:

| Field       | Type     | Description                      |
| ----------- | -------- | -------------------------------- |
| `timestamp` | `String` | ISO 8601 format                  |
| `messageId` | `int`    | Incrementing counter from 1      |
| `quote`     | `String` | Quote of the moment              |

Business logic (producer and consumer) works exclusively with this type. Transport modules (e.g., `dds-support`) handle conversion to/from wire formats.

## Dependencies

None — this module has no external dependencies.
