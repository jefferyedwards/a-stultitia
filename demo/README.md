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

This compiles all modules and generates the IDL type support code automatically during the build.

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

## Expected Output

**Producer:**

```text
Starting TimeOfDay Producer...
  NDDSHOME: /path/to/rti_connext_dds-7.6.0
  RTI_ARCH: arm64Darwin23clang16.0
INFO: Loaded 10 quotes
INFO: DDS DomainParticipant created on domain 0
INFO: Publishing on topic 'TimeOfDay'
INFO: Published [1]: 2026-03-29T12:00:00.000Z — The only way to do great work is to love what you do. — Steve Jobs
INFO: Published [2]: 2026-03-29T12:00:02.000Z — Innovation distinguishes between a leader and a follower. — Steve Jobs
```

**Consumer:**

```text
Starting TimeOfDay Consumer...
  NDDSHOME: /path/to/rti_connext_dds-7.6.0
  RTI_ARCH: arm64Darwin23clang16.0
INFO: DDS DomainParticipant created on domain 0
INFO: Subscribed to topic 'TimeOfDay'
INFO: Consumer running. Press Ctrl+C to exit.
INFO: Received [1]: 2026-03-29T12:00:00.000Z — The only way to do great work is to love what you do. — Steve Jobs
INFO: Received [2]: 2026-03-29T12:00:02.000Z — Innovation distinguishes between a leader and a follower. — Steve Jobs
```

Press `Ctrl+C` in either terminal to stop the application gracefully.

## Directory Layout

```text
demo/
├── README.md                    # This file
├── bin/
│   ├── run-producer.sh          # Launches the producer
│   └── run-consumer.sh          # Launches the consumer
└── etc/
    └── USER_QOS_PROFILES.xml    # DDS QoS configuration (UDPv4, localhost discovery)
```

## QoS Configuration

The `etc/USER_QOS_PROFILES.xml` file configures the DDS middleware:

- **Transport:** UDPv4 only (no shared memory)
- **Discovery:** Unicast peers on `127.0.0.1` (localhost only, multicast disabled)
- **Profile:** `AStultitiaLibrary::DefaultProfile` — loaded automatically by RTI when the file is in the working directory

The run scripts `cd` into the `demo/` directory before launching Java so that RTI picks up the QoS file from `etc/`.
