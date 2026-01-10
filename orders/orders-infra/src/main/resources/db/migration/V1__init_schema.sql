-- =============================================================================
-- Order Management Service - Complete Schema Initialization
-- =============================================================================
-- ID Strategy:
-- - All tables use BIGINT timestamp-based IDs as PK (YYYYMMDDHHmmssSSSNN format)
-- - All tables have a UUID *_key column for foreign key references
-- - Orders have an additional business order_id (channel-date-sequence format)
--   Example: 10-20251225-0000001 (Web order on Dec 25, 2025)
--
-- Order Types: STANDARD, GUEST, RETURN, EXCHANGE, STORE, SUBSCRIPTION
-- Fulfillment Types: STH, BOPS, STS
-- Line Status: CREATED -> ALLOCATED -> RELEASED -> SHIPPED -> DELIVERED
-- =============================================================================

-- =============================================================================
-- ADDRESSES TABLE
-- =============================================================================
CREATE TABLE addresses (
    id BIGINT PRIMARY KEY,
    address_key UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    full_name VARCHAR(200) NOT NULL,
    address_line1 VARCHAR(500) NOT NULL,
    address_line2 VARCHAR(500),
    city VARCHAR(100) NOT NULL,
    state_province VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    phone_number VARCHAR(50),
    email VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for addresses
CREATE INDEX idx_addresses_address_key ON addresses(address_key);
CREATE INDEX idx_addresses_postal_code ON addresses(postal_code);
CREATE INDEX idx_addresses_country ON addresses(country);

-- =============================================================================
-- ORDERS TABLE
-- =============================================================================
CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    order_key UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    order_id VARCHAR(20) UNIQUE NOT NULL,  -- Business ID: channel-date-sequence (e.g., 10-20251225-0000001)
    external_order_id UUID UNIQUE,  -- External order ID from upstream system (e.g., checkout service) for duplicate detection
    customer_id VARCHAR(100) NOT NULL,
    order_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',  -- STANDARD, GUEST, RETURN, EXCHANGE, STORE, SUBSCRIPTION
    channel VARCHAR(20) NOT NULL,  -- WEB, MOBILE, API, POS, CALL_CENTER
    status VARCHAR(50) NOT NULL,
    billing_address_key UUID NOT NULL REFERENCES addresses(address_key),
    notes TEXT,
    subtotal NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    tax_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    discount_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for orders
CREATE INDEX idx_orders_order_key ON orders(order_key);
CREATE INDEX idx_orders_order_id ON orders(order_id);
CREATE INDEX idx_orders_external_order_id ON orders(external_order_id) WHERE external_order_id IS NOT NULL;
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_order_type ON orders(order_type);
CREATE INDEX idx_orders_channel ON orders(channel);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX idx_orders_billing_address_key ON orders(billing_address_key);

-- Constraints for orders
ALTER TABLE orders ADD CONSTRAINT chk_orders_subtotal_positive CHECK (subtotal >= 0);
ALTER TABLE orders ADD CONSTRAINT chk_orders_total_positive CHECK (total_amount >= 0);
ALTER TABLE orders ADD CONSTRAINT chk_orders_channel_valid CHECK (channel IN ('WEB', 'MOBILE', 'API', 'POS', 'CALL_CENTER'));
ALTER TABLE orders ADD CONSTRAINT chk_orders_order_type_valid CHECK (order_type IN ('STANDARD', 'GUEST', 'RETURN', 'EXCHANGE', 'STORE', 'SUBSCRIPTION'));

-- =============================================================================
-- RELEASE SNAPSHOTS TABLE
-- Stores release events from Release Service using a hybrid approach:
-- - Key fields are indexed columns for efficient queries
-- - Full event payload is stored as JSONB for flexibility
-- Updates are handled via upsert on release_id.
-- One order can have multiple releases (1:N relationship).
-- =============================================================================
CREATE TABLE release_snapshots (
    id BIGINT PRIMARY KEY,
    release_key UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    order_key UUID NOT NULL REFERENCES orders(order_key) ON DELETE CASCADE,
    release_id VARCHAR(100) UNIQUE NOT NULL,  -- Unique for upsert semantics
    release_status VARCHAR(50) NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    payload JSONB NOT NULL,                    -- Full event payload from Kafka
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for release snapshots
CREATE INDEX idx_release_snapshots_release_key ON release_snapshots(release_key);
CREATE INDEX idx_release_snapshots_order_key ON release_snapshots(order_key);
CREATE INDEX idx_release_snapshots_release_id ON release_snapshots(release_id);
CREATE INDEX idx_release_snapshots_release_status ON release_snapshots(release_status);
CREATE INDEX idx_release_snapshots_payload ON release_snapshots USING GIN (payload);

-- =============================================================================
-- SHIPMENT SNAPSHOTS TABLE
-- Stores shipment events from Shipment Service using a hybrid approach:
-- - Key fields are indexed columns for efficient queries
-- - Full event payload is stored as JSONB for flexibility
-- Updates are handled via upsert on shipment_id.
-- One order can have multiple shipments (1:N relationship).
-- =============================================================================
CREATE TABLE shipment_snapshots (
    id BIGINT PRIMARY KEY,
    shipment_key UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    order_key UUID NOT NULL REFERENCES orders(order_key) ON DELETE CASCADE,
    shipment_id VARCHAR(100) UNIQUE NOT NULL,  -- Unique for upsert semantics
    shipment_status VARCHAR(50) NOT NULL,
    tracking_number VARCHAR(200),
    event_timestamp TIMESTAMP NOT NULL,
    payload JSONB NOT NULL,                    -- Full event payload from Kafka
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for shipment snapshots
CREATE INDEX idx_shipment_snapshots_shipment_key ON shipment_snapshots(shipment_key);
CREATE INDEX idx_shipment_snapshots_order_key ON shipment_snapshots(order_key);
CREATE INDEX idx_shipment_snapshots_shipment_id ON shipment_snapshots(shipment_id);
CREATE INDEX idx_shipment_snapshots_tracking_number ON shipment_snapshots(tracking_number) WHERE tracking_number IS NOT NULL;
CREATE INDEX idx_shipment_snapshots_shipment_status ON shipment_snapshots(shipment_status);
CREATE INDEX idx_shipment_snapshots_payload ON shipment_snapshots USING GIN (payload);

-- =============================================================================
-- ORDER LINES TABLE
-- =============================================================================
CREATE TABLE order_lines (
    id BIGINT PRIMARY KEY,
    line_key UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    order_key UUID NOT NULL REFERENCES orders(order_key) ON DELETE CASCADE,
    line_number INTEGER NOT NULL,
    item_id BIGINT NOT NULL,
    item_name VARCHAR(500) NOT NULL,
    item_description TEXT,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    tax_rate NUMERIC(5, 4),
    discount_amount NUMERIC(19, 2),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    fulfillment_type VARCHAR(20) NOT NULL DEFAULT 'STH',  -- STH, BOPS, STS
    shipping_address_key UUID REFERENCES addresses(address_key),  -- Line-level shipping address
    -- Estimated dates from checkout/promise system
    estimated_ship_date DATE,
    estimated_delivery_date DATE,
    -- Promised dates from allocation service (actual dates)
    promised_ship_date DATE,
    promised_delivery_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for order_lines
CREATE INDEX idx_order_lines_line_key ON order_lines(line_key);
CREATE INDEX idx_order_lines_order_key ON order_lines(order_key);
CREATE INDEX idx_order_lines_item_id ON order_lines(item_id);
CREATE INDEX idx_order_lines_fulfillment_type ON order_lines(fulfillment_type);
CREATE INDEX idx_order_lines_shipping_address_key ON order_lines(shipping_address_key) WHERE shipping_address_key IS NOT NULL;

-- Constraints for order_lines
ALTER TABLE order_lines ADD CONSTRAINT chk_order_lines_quantity_positive CHECK (quantity > 0);
ALTER TABLE order_lines ADD CONSTRAINT chk_order_lines_unit_price_positive CHECK (unit_price > 0);
ALTER TABLE order_lines ADD CONSTRAINT chk_order_lines_tax_rate_valid CHECK (tax_rate IS NULL OR tax_rate >= 0);
ALTER TABLE order_lines ADD CONSTRAINT chk_order_lines_fulfillment_type_valid CHECK (fulfillment_type IN ('STH', 'BOPS', 'STS'));

-- =============================================================================
-- ORDER LINE STATUS TABLE (renamed from line_status)
-- =============================================================================
CREATE TABLE order_line_status (
    id BIGINT PRIMARY KEY,
    line_status_key UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    line_key UUID UNIQUE NOT NULL REFERENCES order_lines(line_key) ON DELETE CASCADE,  -- One status per line
    quantity INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    status_code VARCHAR(10) NOT NULL,  -- Numeric status code (100, 200, etc.)
    status_description VARCHAR(200) NOT NULL,  -- Human-readable description
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for order_line_status
CREATE INDEX idx_order_line_status_line_status_key ON order_line_status(line_status_key);
CREATE INDEX idx_order_line_status_line_key ON order_line_status(line_key);
CREATE INDEX idx_order_line_status_status ON order_line_status(status);
CREATE INDEX idx_order_line_status_status_code ON order_line_status(status_code);

-- Constraints for order_line_status
ALTER TABLE order_line_status ADD CONSTRAINT chk_order_line_status_quantity_positive CHECK (quantity > 0);
ALTER TABLE order_line_status ADD CONSTRAINT chk_order_line_status_status_valid CHECK (
    status IN ('CREATED', 'ALLOCATED', 'RELEASED', 'SHIPPED', 'SHIPPED_AND_INVOICED', 'DELIVERED', 'RETURN_INITIATED', 'RETURN_COMPLETED', 'CANCELLED')
);

-- =============================================================================
-- ANALYTICS VIEWS
-- =============================================================================

-- View for order analytics
CREATE OR REPLACE VIEW v_order_analytics AS
SELECT
    o.id AS internal_id,
    o.order_key,
    o.order_id,
    o.customer_id,
    o.order_type,
    o.channel,
    o.status,
    o.total_amount,
    o.currency,
    COUNT(ol.id) AS line_count,
    SUM(ol.quantity) AS total_items,
    o.created_at,
    o.updated_at,
    ba.country AS billing_country
FROM orders o
LEFT JOIN order_lines ol ON o.order_key = ol.order_key
LEFT JOIN addresses ba ON o.billing_address_key = ba.address_key
GROUP BY o.id, o.order_key, o.order_id, o.customer_id, o.order_type, o.channel, o.status, o.total_amount, o.currency, o.created_at, o.updated_at, ba.country;

-- View for line status summary
CREATE OR REPLACE VIEW v_line_status_summary AS
SELECT
    ol.id AS internal_id,
    ol.line_key,
    ol.order_key,
    ol.item_name,
    ol.quantity,
    ol.fulfillment_type,
    ols.status,
    ols.status_code,
    ols.status_description
FROM order_lines ol
LEFT JOIN order_line_status ols ON ol.line_key = ols.line_key;

-- View for fulfillment analytics
CREATE OR REPLACE VIEW v_fulfillment_analytics AS
SELECT
    o.order_id,
    o.order_type,
    ol.fulfillment_type,
    COUNT(ol.id) AS line_count,
    SUM(ol.quantity) AS total_quantity,
    ols.status AS line_status
FROM orders o
JOIN order_lines ol ON o.order_key = ol.order_key
LEFT JOIN order_line_status ols ON ol.line_key = ols.line_key
GROUP BY o.order_id, o.order_type, ol.fulfillment_type, ols.status;

-- =============================================================================
-- COMMENTS FOR DOCUMENTATION
-- =============================================================================
COMMENT ON TABLE addresses IS 'Stores billing and shipping addresses for orders and order lines';
COMMENT ON TABLE orders IS 'Main orders table supporting multiple order types (STANDARD, GUEST, RETURN, etc.)';
COMMENT ON TABLE order_lines IS 'Individual line items with line-level fulfillment type and shipping address';
COMMENT ON TABLE order_line_status IS 'Tracks status of order lines with status code and description';
COMMENT ON TABLE release_snapshots IS 'Stores release events from Release Service using hybrid approach. Key fields indexed, full payload in JSONB.';
COMMENT ON TABLE shipment_snapshots IS 'Stores shipment events from Shipment Service using hybrid approach. Key fields indexed, full payload in JSONB.';
COMMENT ON COLUMN release_snapshots.payload IS 'Full event payload from Kafka as JSONB for flexibility and future-proofing';
COMMENT ON COLUMN shipment_snapshots.payload IS 'Full event payload from Kafka as JSONB for flexibility and future-proofing';

COMMENT ON COLUMN orders.id IS 'Internal timestamp-based ID (YYYYMMDDHHmmssSSSNN format)';
COMMENT ON COLUMN orders.order_key IS 'UUID for foreign key references from other tables';
COMMENT ON COLUMN orders.order_id IS 'Business order ID (channel-date-sequence format, e.g., 10-20251225-0000001)';
COMMENT ON COLUMN orders.order_type IS 'Order type: STANDARD, GUEST, RETURN, EXCHANGE, STORE, SUBSCRIPTION';
COMMENT ON COLUMN orders.channel IS 'Order source channel: WEB(10), MOBILE(20), API(30), POS(40), CALL_CENTER(50)';
COMMENT ON COLUMN order_lines.fulfillment_type IS 'Fulfillment method: STH (Ship to Home), BOPS (Buy Online Pick in Store), STS (Ship to Store)';
COMMENT ON COLUMN order_lines.shipping_address_key IS 'Line-level shipping address for mixed fulfillment scenarios';
COMMENT ON COLUMN order_line_status.status_code IS 'Numeric status code: 100=Created, 200=Allocated, 300=Released, 400=Shipped, etc.';
