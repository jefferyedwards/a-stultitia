#!/usr/bin/env bash
#
# Launches the TimeOfDay Consumer (Spring Boot).
# Requires NDDSHOME to be set to the RTI Connext DDS installation directory.
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEMO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
PROJECT_DIR="$(cd "$DEMO_DIR/.." && pwd)"

# --- Validate environment ---

if [ -z "${NDDSHOME:-}" ]; then
    echo "ERROR: NDDSHOME is not set."
    echo "Set it to your RTI Connext DDS installation directory, e.g.:"
    echo "  export NDDSHOME=/path/to/rti_connext_dds-7.6.0"
    exit 1
fi

if [ ! -d "$NDDSHOME" ]; then
    echo "ERROR: NDDSHOME directory does not exist: $NDDSHOME"
    exit 1
fi

# --- Detect platform and set native library path ---

OS="$(uname -s)"
ARCH="$(uname -m)"

case "$OS" in
    Linux)
        case "$ARCH" in
            x86_64)  RTI_ARCH="x64Linux4gcc7.3.0" ;;
            aarch64) RTI_ARCH="armv8Linux4gcc7.3.0" ;;
            *)       echo "ERROR: Unsupported Linux architecture: $ARCH"; exit 1 ;;
        esac
        export LD_LIBRARY_PATH="${NDDSHOME}/lib/${RTI_ARCH}:${LD_LIBRARY_PATH:-}"
        ;;
    Darwin)
        case "$ARCH" in
            x86_64)  RTI_ARCH="x64Darwin17clang9.0" ;;
            arm64)   RTI_ARCH="arm64Darwin23clang16.0" ;;
            *)       echo "ERROR: Unsupported macOS architecture: $ARCH"; exit 1 ;;
        esac
        export DYLD_LIBRARY_PATH="${NDDSHOME}/lib/${RTI_ARCH}:${DYLD_LIBRARY_PATH:-}"
        ;;
    *)
        echo "ERROR: Unsupported OS: $OS"
        exit 1
        ;;
esac

# --- Run ---

echo "Starting TimeOfDay Consumer..."
echo "  NDDSHOME: $NDDSHOME"
echo "  RTI_ARCH: $RTI_ARCH"

java -jar "${PROJECT_DIR}/consumer/target/consumer-1.0.0-SNAPSHOT.jar" "$@"
