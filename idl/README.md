# IDL Module

Defines the DDS message types used by the producer and consumer. Java type support code is **generated at build time** from the IDL file — no generated source is checked into version control.

## IDL Definition

The module defines a single structured type in [src/main/idl/TimeOfDayMessage.idl](src/main/idl/TimeOfDayMessage.idl):

```idl
module net {
module edwardsonthe {
module messages {

struct TimeOfDayMessage {
    string timestamp;   // ISO 8601 format
    long messageId;     // Incremented starting from 1
    string quote;       // Quote of the moment
};

}; };  };
```

The IDL module hierarchy (`net.edwardsonthe.messages`) maps directly to the Java package `net.edwardsonthe.messages`.

## Code Generation

During the Maven `generate-sources` phase, the `exec-maven-plugin` invokes RTI's `rtiddsgen` code generator:

```
$NDDSHOME/bin/rtiddsgen -language java -replace -d target/generated-sources/idl src/main/idl/TimeOfDayMessage.idl
```

This produces the following classes in `target/generated-sources/idl/net/edwardsonthe/messages/`:

| Class                         | Purpose                                                     |
| ----------------------------- | ----------------------------------------------------------- |
| `TimeOfDayMessage`            | Data POJO with `timestamp`, `messageId`, and `quote` fields |
| `TimeOfDayMessageTypeSupport` | Registers the type with a DDS DomainParticipant             |
| `TimeOfDayMessageDataWriter`  | Typed wrapper for publishing messages                       |
| `TimeOfDayMessageDataReader`  | Typed wrapper for receiving messages                        |
| `TimeOfDayMessageSeq`         | Sequence container for bulk sample operations               |
| `TimeOfDayMessageTypeCode`    | Runtime type metadata                                       |

The `build-helper-maven-plugin` adds `target/generated-sources/idl` to the compilation source path so these classes are compiled and packaged into the `idl` JAR.

## Regenerating Manually

If you need to regenerate outside of Maven (e.g., for inspection):

```bash
cd idl
$NDDSHOME/bin/rtiddsgen -language java -replace -d /tmp/idl-gen src/main/idl/TimeOfDayMessage.idl
```

## Dependencies

- **RTI Connext DDS** (`nddsjava.jar`) — provides the base classes that generated code extends
