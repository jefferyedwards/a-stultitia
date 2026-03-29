# Running the Demo

Instructions for building and running the TimeOfDay producer/consumer demo. Supports both DDS and Kafka transports.

## Prerequisites

- **JDK 17** or later
- **Apache Maven 3.9+**
- **RTI Connext DDS 7.x** (required at build time; required at runtime only for DDS profile)
- `NDDSHOME` environment variable pointing to your RTI Connext installation

## Build

From the project root:

```bash
export NDDSHOME=/path/to/rti_connext_dds-7.6.0
mvn clean package
```

This compiles all modules and produces executable fat JARs for the producer and consumer.

## Run with DDS

Open two terminals. In both, ensure `NDDSHOME` is set.

**Terminal 1 — start the consumer:**

```bash
./demo/bin/run-consumer.sh
```

**Terminal 2 — start the producer:**

```bash
./demo/bin/run-producer.sh
```

## Run with Kafka

No `NDDSHOME` or RTI installation needed at runtime.

**Terminal 1 — start the consumer (starts embedded Kafka broker):**

```bash
./demo/bin/run-consumer-kafka.sh
```

**Terminal 2 — start the producer (connects to the consumer's broker):**

```bash
./demo/bin/run-producer-kafka.sh
```

You can also pass Spring Boot property overrides directly:

```bash
./demo/bin/run-producer.sh --producer.publish-interval-ms=5000
```

## Expected Output

**Producer:**

```text
Starting TimeOfDay Producer...
  NDDSHOME: /path/to/rti_connext_dds-7.6.0
  RTI_ARCH: arm64Darwin23clang16.0

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

INFO --- n.e.producer.ProducerApplication    : Starting ProducerApplication
INFO --- n.e.producer.ProducerApplication    : The following 1 profile is active: "dds"
INFO --- n.edwardsonthe.dds.DdsParticipantConfig : DDS DomainParticipant created on domain 0
INFO --- n.edwardsonthe.dds.DdsProducerConfig    : DDS DataWriter created for topic 'TimeOfDay'
INFO --- n.e.producer.TimeOfDayProducer      : Loaded 10 quotes
INFO --- n.e.producer.ProducerApplication    : Started ProducerApplication in 0.35 seconds
INFO --- n.e.producer.TimeOfDayProducer      : Publishing every 2000ms
INFO --- n.e.producer.TimeOfDayProducer      : Published [1]: 2026-03-29T12:00:00Z — The only way to do great work is to love what you do. — Steve Jobs
```

**Consumer:**

```text
Starting TimeOfDay Consumer...
  NDDSHOME: /path/to/rti_connext_dds-7.6.0
  RTI_ARCH: arm64Darwin23clang16.0

INFO --- n.e.consumer.ConsumerApplication      : Starting ConsumerApplication
INFO --- n.e.consumer.ConsumerApplication      : The following 1 profile is active: "dds"
INFO --- n.edwardsonthe.dds.DdsParticipantConfig : DDS DomainParticipant created on domain 0
INFO --- n.edwardsonthe.dds.DdsConsumerConfig    : DDS DataReader created for topic 'TimeOfDay'
INFO --- n.e.consumer.ConsumerApplication      : Started ConsumerApplication in 0.36 seconds
INFO --- n.e.consumer.TimeOfDayConsumer        : Received [1]: 2026-03-29T12:00:00Z — The only way to do great work is to love what you do. — Steve Jobs
```

Press `Ctrl+C` in either terminal to stop the application gracefully.

## Directory Layout

```text
demo/
├── README.md                        # This file
└── bin/
    ├── run-producer.sh              # DDS transport
    ├── run-consumer.sh              # DDS transport
    ├── run-producer-kafka.sh        # Kafka transport
    └── run-consumer-kafka.sh        # Kafka transport
```

## Transport Configuration

The active transport is determined by `spring.profiles.active`:

| Profile | Transport       | Run scripts                      | NDDSHOME required |
| ------- | --------------- | -------------------------------- | ----------------- |
| `dds`   | RTI Connext DDS | `run-producer/consumer.sh`       | Yes               |
| `kafka` | Apache Kafka    | `run-producer/consumer-kafka.sh` | No                |

Both transports coexist in the same build artifact — only the active profile determines which one is used.
