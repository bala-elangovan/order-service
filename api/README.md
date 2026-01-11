# Order Management Service - API Testing

This directory contains HTTP client files for testing the Order Management Service API using IntelliJ IDEA's HTTP Client.

## Files

- **`http-client.env.json`** - Environment configuration (dev, local)
- **`orders.http`** - Order management endpoints (CRUD operations)
- **`health.http`** - Health check and actuator endpoints
- **`test-data/`** - Sample event payloads for Kafka testing

## How to Use

### Prerequisites

1. Start infrastructure (PostgreSQL + Kafka):
   ```bash
   make infra-up
   ```

2. Start the API application:
   ```bash
   make run-api
   ```

3. Start the Consumer application (for Kafka events):
   ```bash
   make run-consumer
   ```

### Running Requests in IntelliJ IDEA

1. Open any `.http` file in IntelliJ IDEA
2. Select the environment (dev/local) from the dropdown at the top
3. Click the green play button next to any request
4. View results in the "Run" panel at the bottom

## API Endpoints

### Order Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/orders/v1/` | Create a new order |
| GET | `/orders/v1/{id}` | Get order by ID |
| GET | `/orders/v1/` | List orders (with pagination) |
| GET | `/orders/v1/?customerId={id}` | Get orders by customer |
| PATCH | `/orders/v1/{id}` | Update order (notes, billing address) |
| POST | `/orders/v1/{id}/cancel` | Cancel order |
| DELETE | `/orders/v1/{id}` | Delete order |

### Health & Monitoring

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Overall health status |
| GET | `/actuator/health/liveness` | Liveness probe (K8s) |
| GET | `/actuator/health/readiness` | Readiness probe (K8s) |
| GET | `/actuator/info` | Application info |

## Kafka Events

Orders can also be created via Kafka events. Use the Makefile commands:

```bash
# Produce a sample order event
make kafka-produce

# Produce multiple random order events
make kafka-produce-many COUNT=100

# Produce release + shipment events for an existing order
make kafka-produce-fulfillment ORDER_ID=10-20251230-0000001
```

### Event Topics

| Topic | Description |
|-------|-------------|
| `checkout-order-create` | Order creation events |
| `release-events` | Release/allocation events |
| `shipment-events` | Shipment events |

## Test Data

Sample event payloads are in the `test-data/` directory:

- `order-event-sample.json` - Sample order creation event
- `release-event-sample.json` - Sample release event
- `shipment-event-sample.json` - Sample shipment event

## Environment Variables

The `http-client.env.json` file contains environment-specific variables:

```json
{
  "dev": {
    "baseUrl": "http://localhost:8080",
    "userId": "user-123",
    "userGroups": "order-admins,order-users"
  }
}
```

### Custom Headers

All requests include these headers:

- `X-User-Id` - User identifier
- `X-User-Groups` - Comma-separated list of user groups
- `X-Transaction-Id` - Unique transaction ID (auto-generated with `{{$uuid}}`)

## Testing Scenarios

### Order Types
- `STANDARD` - Regular customer order
- `GUEST` - Guest checkout (no account)
- `RETURN` - Return order
- `STORE` - In-store order

### Fulfillment Types
- `STH` - Ship to Home
- `BOPS` - Buy Online, Pick up in Store
- `STS` - Ship to Store

### Error Cases
- Create order with invalid data (400 Bad Request)
- Get non-existent order (404 Not Found)
- Cancel already cancelled order (409 Conflict)

## Troubleshooting

### Application not responding
```bash
# Check if containers are running
docker ps

# Restart infrastructure
make infra-down
make infra-up
```

### Kafka issues
```bash
# List Kafka topics
make kafka-topics

# Create topics if missing
make kafka-topics-create
```
