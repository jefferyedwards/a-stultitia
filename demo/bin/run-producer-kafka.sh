#!/usr/bin/env bash
#
# Launches the TimeOfDay Producer with Kafka transport.
# No RTI/NDDSHOME required — connects to embedded Kafka broker started by the consumer.
#
# Start the consumer first (run-consumer-kafka.sh), then start this producer.
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEMO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
PROJECT_DIR="$(cd "$DEMO_DIR/.." && pwd)"

BROKER_FILE="${TMPDIR:=/tmp}/.kafka-bootstrap-servers"

# Wait for the consumer to start the broker and write the address
echo "Waiting for Kafka broker address (start consumer first)..."
for i in $(seq 1 30); do
    if [ -f "$BROKER_FILE" ]; then
        BOOTSTRAP_SERVERS=$(cat "$BROKER_FILE")
        echo "  Kafka broker: $BOOTSTRAP_SERVERS"
        break
    fi
    sleep 1
done

if [ -z "${BOOTSTRAP_SERVERS:-}" ]; then
    echo "ERROR: Broker address file not found at $BROKER_FILE"
    echo "Make sure the consumer is running with Kafka profile first."
    exit 1
fi

echo "Starting TimeOfDay Producer (Kafka)..."
java -jar "${PROJECT_DIR}/producer/target/producer-1.0.0-SNAPSHOT.jar" \
    --spring.profiles.active=kafka \
    --spring.kafka.bootstrap-servers="$BOOTSTRAP_SERVERS" "$@"
