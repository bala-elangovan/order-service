#!/usr/bin/env bash
# Generate random order events and produce them to Kafka (batched for speed)
# Usage: ./scripts/generate-orders.sh [count] [topic]
# Example: ./scripts/generate-orders.sh 100 checkout-order-create

set -e

COUNT=${1:-10}
TOPIC=${2:-checkout-order-create}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Generating $COUNT orders to topic '$TOPIC'..."

# Check if kafka is available
if ! docker exec order-kafka kafka-topics --bootstrap-server localhost:9092 --list &> /dev/null; then
    echo "Error: Kafka is not running. Start it with: make kafka-up"
    exit 1
fi

# Create topic if it doesn't exist
docker exec order-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic "$TOPIC" --partitions 3 --replication-factor 1 --if-not-exists 2>/dev/null

echo "Generating orders..."

# Generate orders using Python (much faster than bash) and pipe directly to Kafka
if python3 "$SCRIPT_DIR/generate-orders.py" "$COUNT" | docker exec -i order-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic "$TOPIC" 2>/dev/null; then
    echo ""
    echo "Done! Successfully produced $COUNT orders to '$TOPIC'"
else
    echo ""
    echo "Error: Failed to produce orders to Kafka"
    exit 1
fi
