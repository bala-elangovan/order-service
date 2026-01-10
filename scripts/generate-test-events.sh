#!/usr/bin/env bash
# Generate and publish release + shipment events for an existing order
# Usage: ./scripts/generate-test-events.sh <order_id>
# Example: ./scripts/generate-test-events.sh 10-20251230-0000001

set -e

ORDER_ID=$1
API_BASE_URL=${API_BASE_URL:-"http://localhost:8080"}
RELEASE_TOPIC=${RELEASE_TOPIC:-release-events}
SHIPMENT_TOPIC=${SHIPMENT_TOPIC:-shipment-events}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Validate input
if [ -z "$ORDER_ID" ]; then
    echo "Usage: $0 <order_id>"
    echo ""
    echo "Arguments:"
    echo "  order_id    The order ID to generate events for (e.g., 10-20251230-0000001)"
    echo ""
    echo "Environment variables:"
    echo "  API_BASE_URL      API base URL (default: http://localhost:8080)"
    echo "  RELEASE_TOPIC     Kafka topic for release events (default: release-events)"
    echo "  SHIPMENT_TOPIC    Kafka topic for shipment events (default: shipment-events)"
    exit 1
fi

# Check if jq is available
if ! command -v jq &> /dev/null; then
    log_error "jq is required but not installed. Install it with: brew install jq"
    exit 1
fi

# Check if kafka is available
if ! docker exec order-kafka kafka-topics --bootstrap-server localhost:9092 --list &> /dev/null; then
    log_error "Kafka is not running. Start it with: make kafka-up"
    exit 1
fi

# Fetch order from API
log_info "Fetching order $ORDER_ID from API..."
ORDER_RESPONSE=$(curl -s -w "\n%{http_code}" "$API_BASE_URL/orders/v1/$ORDER_ID")
HTTP_CODE=$(echo "$ORDER_RESPONSE" | tail -n1)
ORDER_JSON=$(echo "$ORDER_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" != "200" ]; then
    log_error "Failed to fetch order $ORDER_ID (HTTP $HTTP_CODE)"
    echo "$ORDER_JSON"
    exit 1
fi

log_info "Order fetched successfully"

# Extract order details
ORDER_KEY=$(echo "$ORDER_JSON" | jq -r '.order_key')

if [ "$ORDER_KEY" == "null" ] || [ -z "$ORDER_KEY" ]; then
    log_error "Order does not have an order_key. This might be a newly created order that hasn't been persisted yet."
    exit 1
fi

log_info "Order Key: $ORDER_KEY"

# Extract line details
LINES=$(echo "$ORDER_JSON" | jq -c '.lines[]')
LINE_COUNT=$(echo "$ORDER_JSON" | jq '.lines | length')

log_info "Found $LINE_COUNT order lines"

# Generate timestamps
CURRENT_DATETIME=$(date -u +"%Y-%m-%dT%H:%M:%S")
RELEASE_TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%S")
SHIP_DATE=$(date -u -v+1d +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -d "+1 day" +"%Y-%m-%dT%H:%M:%S")
DELIVERY_DATE=$(date -u -v+3d +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -d "+3 days" +"%Y-%m-%dT%H:%M:%S")

# Generate unique IDs based on timestamp
TIMESTAMP_ID=$(date +"%Y%m%d%H%M%S")
RELEASE_ID="${TIMESTAMP_ID}001"
RELEASE_KEY="${TIMESTAMP_ID}$(printf '%06d' $RANDOM)"
SHIPMENT_ID="${TIMESTAMP_ID}001"
SHIPMENT_KEY="${TIMESTAMP_ID}$(printf '%06d' $RANDOM)"

log_info "Generated Release ID: $RELEASE_ID, Release Key: $RELEASE_KEY"
log_info "Generated Shipment ID: $SHIPMENT_ID, Shipment Key: $SHIPMENT_KEY"

# Build release lines array
RELEASE_LINES="["
FIRST=true
while IFS= read -r line; do
    LINE_KEY=$(echo "$line" | jq -r '.line_key')
    LINE_NUMBER=$(echo "$line" | jq -r '.line_number')
    ITEM_ID=$(echo "$line" | jq -r '.item_id')
    ITEM_NAME=$(echo "$line" | jq -r '.item_name')
    ITEM_DESC=$(echo "$line" | jq -r '.item_description')
    QUANTITY=$(echo "$line" | jq -r '.quantity')
    UNIT_PRICE=$(echo "$line" | jq -r '.unit_price')
    CURRENCY=$(echo "$line" | jq -r '.currency')
    SUBTOTAL=$(echo "$line" | jq -r '.subtotal')
    TAX_AMOUNT=$(echo "$line" | jq -r '.tax_amount')
    DISCOUNT=$(echo "$line" | jq -r '.discount_amount // empty')
    FULFILLMENT=$(echo "$line" | jq -r '.fulfillment_type')
    TOTAL=$(echo "$line" | jq -r '.total_amount')

    if [ "$FIRST" = true ]; then
        FIRST=false
    else
        RELEASE_LINES="$RELEASE_LINES,"
    fi

    DISCOUNT_JSON="null"
    if [ -n "$DISCOUNT" ] && [ "$DISCOUNT" != "null" ]; then
        DISCOUNT_JSON="$DISCOUNT"
    fi

    RELEASE_LINES="$RELEASE_LINES{\"line_key\":\"$LINE_KEY\",\"line_number\":$LINE_NUMBER,\"item_id\":$ITEM_ID,\"item_name\":\"$ITEM_NAME\",\"item_description\":\"$ITEM_DESC\",\"ordered_quantity\":$QUANTITY,\"released_quantity\":$QUANTITY,\"unit_price\":$UNIT_PRICE,\"currency\":\"$CURRENCY\",\"subtotal\":$SUBTOTAL,\"tax_amount\":$TAX_AMOUNT,\"discount_amount\":$DISCOUNT_JSON,\"total_amount\":$TOTAL,\"fulfillment_type\":\"$FULFILLMENT\",\"line_status\":\"RELEASED\"}"
done <<< "$LINES"
RELEASE_LINES="$RELEASE_LINES]"

# Get shipping address from first line
SHIP_TO_ADDRESS=$(echo "$ORDER_JSON" | jq -c '.lines[0].shipping_address | {full_name,address_line1:.address_line_1,address_line2:.address_line_2,city,state_province,postal_code,country,phone_number,email}')

TOTAL_QUANTITY=$(echo "$ORDER_JSON" | jq '.total_quantity')

# Generate Release Event (in memory)
RELEASE_EVENT=$(cat << EOF
{"release_id":"$RELEASE_ID","order_id":"$ORDER_ID","release_status":"RELEASED","event_timestamp":"$RELEASE_TIMESTAMP","payload":{"release_key":"$RELEASE_KEY","release_id":"$RELEASE_ID","order_key":"$ORDER_KEY","order_id":"$ORDER_ID","ship_node":"DC001","ship_node_type":"DC","receiving_node":null,"status":"RELEASED","status_date":"$RELEASE_TIMESTAMP","priority":1,"requested_ship_date":"$SHIP_DATE","promised_ship_date":"$SHIP_DATE","expected_ship_date":"$SHIP_DATE","requested_delivery_date":"$DELIVERY_DATE","expected_delivery_date":"$DELIVERY_DATE","carrier":"UPS","scac":"UPSN","service_type":"GROUND","total_quantity":$TOTAL_QUANTITY,"total_lines":$LINE_COUNT,"ship_to_address":$SHIP_TO_ADDRESS,"release_lines":$RELEASE_LINES,"audit":{"created_by":"SYSTEM","created_date":"$CURRENT_DATETIME","modified_by":"RELEASE_AGENT","modified_date":"$RELEASE_TIMESTAMP"}}}
EOF
)

# Build shipment lines and container details
SHIPMENT_LINES="["
CONTAINER_DETAILS="["
FIRST=true
LINE_SEQ=1
while IFS= read -r line; do
    LINE_KEY=$(echo "$line" | jq -r '.line_key')
    LINE_NUMBER=$(echo "$line" | jq -r '.line_number')
    ITEM_ID=$(echo "$line" | jq -r '.item_id')
    ITEM_NAME=$(echo "$line" | jq -r '.item_name')
    ITEM_DESC=$(echo "$line" | jq -r '.item_description')
    QUANTITY=$(echo "$line" | jq -r '.quantity')
    UNIT_PRICE=$(echo "$line" | jq -r '.unit_price')
    CURRENCY=$(echo "$line" | jq -r '.currency')
    SUBTOTAL=$(echo "$line" | jq -r '.subtotal')
    TAX_AMOUNT=$(echo "$line" | jq -r '.tax_amount')
    DISCOUNT=$(echo "$line" | jq -r '.discount_amount // empty')
    TOTAL=$(echo "$line" | jq -r '.total_amount')

    SHIPMENT_LINE_KEY="${SHIPMENT_KEY}$(printf '%03d' $LINE_SEQ)"

    if [ "$FIRST" = true ]; then
        FIRST=false
    else
        SHIPMENT_LINES="$SHIPMENT_LINES,"
        CONTAINER_DETAILS="$CONTAINER_DETAILS,"
    fi

    DISCOUNT_FIELD=""
    if [ -n "$DISCOUNT" ] && [ "$DISCOUNT" != "null" ]; then
        DISCOUNT_FIELD="\"discount_amount\":$DISCOUNT,"
    fi

    SHIPMENT_LINES="$SHIPMENT_LINES{\"shipment_line_key\":\"$SHIPMENT_LINE_KEY\",\"line_key\":\"$LINE_KEY\",\"line_number\":$LINE_NUMBER,\"item_id\":$ITEM_ID,\"item_name\":\"$ITEM_NAME\",\"item_description\":\"$ITEM_DESC\",\"quantity\":$QUANTITY,\"unit_price\":$UNIT_PRICE,\"currency\":\"$CURRENCY\",\"subtotal\":$SUBTOTAL,\"tax_amount\":$TAX_AMOUNT,$DISCOUNT_FIELD\"total_amount\":$TOTAL}"
    CONTAINER_DETAILS="$CONTAINER_DETAILS{\"line_key\":\"$LINE_KEY\",\"item_id\":$ITEM_ID,\"quantity\":$QUANTITY}"

    ((LINE_SEQ++))
done <<< "$LINES"
SHIPMENT_LINES="$SHIPMENT_LINES]"
CONTAINER_DETAILS="$CONTAINER_DETAILS]"

# Generate tracking number
TRACKING_NUMBER="1Z999AA1$(printf '%010d' $RANDOM)"

# Ship from address (DC)
SHIP_FROM_ADDRESS='{"address_id":"DC001","full_name":"Distribution Center 001","address_line1":"1000 Warehouse Blvd","address_line2":null,"city":"Memphis","state_province":"TN","postal_code":"38118","country":"USA","phone_number":"+1-901-555-0100","email":null}'

# Generate Shipment Event (in memory, using release details)
SHIPMENT_EVENT=$(cat << EOF
{"shipment_id":"$SHIPMENT_ID","order_id":"$ORDER_ID","shipment_status":"SHIPPED","tracking_number":"$TRACKING_NUMBER","event_timestamp":"$SHIP_DATE","payload":{"shipment_key":"$SHIPMENT_KEY","shipment_id":"$SHIPMENT_ID","release_key":"$RELEASE_KEY","release_id":"$RELEASE_ID","order_key":"$ORDER_KEY","order_id":"$ORDER_ID","ship_node":"DC001","ship_node_type":"DC","status":"SHIPPED","status_description":"Shipped","status_date":"$SHIP_DATE","carrier":"UPS","scac":"UPSN","service_type":"GROUND","bill_of_lading":"BOL-$SHIPMENT_ID","ship_date":"$SHIP_DATE","expected_delivery_date":"$DELIVERY_DATE","actual_delivery_date":null,"total_weight":2.5,"weight_uom":"LB","total_volume":0.25,"volume_uom":"CFT","total_quantity":$TOTAL_QUANTITY,"total_lines":$LINE_COUNT,"ship_from_address":$SHIP_FROM_ADDRESS,"ship_to_address":$SHIP_TO_ADDRESS,"containers":[{"container_key":"${SHIPMENT_KEY}001","container_no":"PKG001","container_type":"CARTON","tracking_number":"$TRACKING_NUMBER","tracking_url":"https://www.ups.com/track?tracknum=$TRACKING_NUMBER","status":"SHIPPED","status_description":"Shipped","status_date":"$SHIP_DATE","weight":2.5,"weight_uom":"LB","length":12.0,"width":10.0,"height":8.0,"dimension_uom":"IN","container_details":$CONTAINER_DETAILS}],"shipment_lines":$SHIPMENT_LINES,"tracking_events":[{"event_key":"${SHIPMENT_KEY}EVT001","event_type":"SHIPPED","event_date":"$SHIP_DATE","location":"Memphis, TN","description":"Shipment picked up"}],"audit":{"created_by":"WMS_AGENT","created_date":"$CURRENT_DATETIME","modified_by":"SHIP_CONFIRM_AGENT","modified_date":"$SHIP_DATE"}}}
EOF
)

# Create topics if they don't exist
docker exec order-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic "$RELEASE_TOPIC" --partitions 3 --replication-factor 1 --if-not-exists 2>/dev/null || true
docker exec order-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic "$SHIPMENT_TOPIC" --partitions 3 --replication-factor 1 --if-not-exists 2>/dev/null || true

# Publish release event
log_info "Publishing release event to topic '$RELEASE_TOPIC'..."
if echo "$RELEASE_EVENT" | docker exec -i order-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic "$RELEASE_TOPIC" 2>/dev/null; then
    log_info "Release event published successfully"
else
    log_error "Failed to publish release event"
    exit 1
fi

# Small delay to ensure release is processed first
sleep 1

# Publish shipment event
log_info "Publishing shipment event to topic '$SHIPMENT_TOPIC'..."
if echo "$SHIPMENT_EVENT" | docker exec -i order-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic "$SHIPMENT_TOPIC" 2>/dev/null; then
    log_info "Shipment event published successfully"
else
    log_error "Failed to publish shipment event"
    exit 1
fi

echo ""
log_info "All events published successfully!"
echo ""
echo "Summary:"
echo "  Order ID:       $ORDER_ID"
echo "  Order Key:      $ORDER_KEY"
echo "  Release ID:     $RELEASE_ID"
echo "  Release Key:    $RELEASE_KEY"
echo "  Shipment ID:    $SHIPMENT_ID"
echo "  Shipment Key:   $SHIPMENT_KEY"
echo "  Tracking:       $TRACKING_NUMBER"
