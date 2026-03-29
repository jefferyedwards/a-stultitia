#!/usr/bin/env bash
#
# Launches the TimeOfDay Consumer with Kafka transport.
# No RTI/NDDSHOME required — uses embedded Kafka broker.
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEMO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
PROJECT_DIR="$(cd "$DEMO_DIR/.." && pwd)"

echo "Starting TimeOfDay Consumer (Kafka)..."
java -jar "${PROJECT_DIR}/consumer/target/consumer-1.0.0-SNAPSHOT.jar" \
    --spring.profiles.active=kafka "$@"
