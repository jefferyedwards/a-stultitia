# A Stultitia

A step-by-step tutorial demonstrating how to modernize a legacy Java application that uses RTI Connext DDS (NDDS) for messaging.

Each step introduces modern frameworks and architectural patterns, progressively transforming raw DDS code into a Spring-based application with pluggable messaging.

## Prerequisites

- **JDK 17** or later
- **Apache Maven 3.9+**
- **RTI Connext DDS 7.x** (Express or Professional) — [download here](https://www.rti.com/get-connext)
- `NDDSHOME` environment variable pointing to your RTI Connext installation

## Tutorial Steps

| Step | Branch   | Description                                                        |
| ---- | -------- | ------------------------------------------------------------------ |
| 1    | `step-1` | Raw RTI DDS producer/consumer with shell scripts and SLF4J logging |
| 2    | `step-2` | Convert to Spring Boot applications                                |
| 3    | `step-3` | Add Spring Boot Actuators for monitoring and management            |
| 4    | `step-4` | Introduce Spring Integration with custom DDS channel adapters      |
| 5    | `step-5` | Swap messaging to Kafka via configuration — zero code changes      |

## Navigating the Tutorial

Each step builds incrementally on the previous one. To follow along:

```bash
# Start with Step 1
git checkout step-1

# See what changed between steps
git diff step-1..step-2
```

Each step branch includes a `STEP-<n>.md` file documenting what the step achieves, all changes from the previous step, and build/run instructions.

## Project Structure

```text
a-stultitia/
├── pom.xml                    # Parent reactor POM
├── README.md                  # This file
├── STEP-1.md                  # Step 1: Raw RTI DDS Producer/Consumer
├── STEP-2.md                  # Step 2: Spring Boot Conversion
├── STEP-3.md                  # Step 3: Spring Boot Actuators
├── demo/                      # Runtime scripts and configuration
│   ├── README.md              # Build, run, and test instructions
│   └── bin/
│       ├── run-producer.sh
│       └── run-consumer.sh
├── idl/                       # Shared IDL type definitions
│   ├── README.md
│   ├── pom.xml
│   └── src/main/idl/
├── dds-support/               # Shared DDS Spring configuration
│   ├── README.md
│   ├── pom.xml
│   └── src/
├── producer/                  # Message producer module
│   ├── README.md
│   ├── pom.xml
│   └── src/
└── consumer/                  # Message consumer module
    ├── README.md
    ├── pom.xml
    └── src/
```

## Quick Start

```bash
# Set RTI environment
export NDDSHOME=/path/to/rti_connext_dds-7.6.0

# Build all modules
mvn clean package

# In terminal 1 — start the consumer
./demo/bin/run-consumer.sh

# In terminal 2 — start the producer
./demo/bin/run-producer.sh
```

See [demo/README.md](demo/README.md) for detailed build and run instructions.
