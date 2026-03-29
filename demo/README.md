# Running the Demo

Instructions for building and running the TimeOfDay producer/consumer demo.

## Prerequisites

- **JDK 17** or later
- **Apache Maven 3.9+**
- **RTI Connext DDS 7.x** (Express or Professional)
- `NDDSHOME` environment variable pointing to your RTI Connext installation
- RTI license file at `$NDDSHOME/rti_license.dat`

## Build

From the project root:

```bash
export NDDSHOME=/path/to/rti_connext_dds-7.6.0
mvn clean package
```

This compiles all modules, generates the IDL type support code, and produces executable fat JARs for the producer and consumer (via `spring-boot-maven-plugin`).

## Run

Open two terminals. In both, ensure `NDDSHOME` is set.

**Terminal 1 — start the consumer first:**

```bash
./demo/bin/run-consumer.sh
```

**Terminal 2 — start the producer:**

```bash
./demo/bin/run-producer.sh
```

You can also pass Spring Boot property overrides directly:

```bash
./demo/bin/run-producer.sh --dds.domain-id=1 --producer.publish-interval-ms=5000
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
├── README.md                    # This file
└── bin/
    ├── run-producer.sh          # Launches the producer (java -jar)
    └── run-consumer.sh          # Launches the consumer (java -jar)
```

## DDS Configuration

The DDS `DomainParticipant` is created with default QoS by `DdsParticipantConfig` in the `dds-support` module. Domain ID and topic name are externalized to each module's `application.properties`.
