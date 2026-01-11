#!/usr/bin/env python3
"""Generate random order events for Kafka testing."""

import json
import random
import sys
import uuid
from datetime import datetime, timedelta, timezone

FIRST_NAMES = ["John", "Jane", "Michael", "Sarah", "David", "Emily", "Robert", "Lisa", "William", "Jennifer"]
LAST_NAMES = ["Doe", "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Martinez"]
CITIES_STATES = [
    ("San Francisco", "CA"), ("New York", "NY"), ("Los Angeles", "CA"), ("Chicago", "IL"),
    ("Seattle", "WA"), ("Portland", "OR"), ("Austin", "TX"), ("Denver", "CO"),
    ("Boston", "MA"), ("Miami", "FL")
]
CHANNELS = ["WEB", "MOBILE", "API", "POS", "CALL_CENTER"]
ORDER_TYPES = ["STANDARD", "GUEST", "STORE"]
FULFILLMENT_TYPES = ["STH", "BOPS", "STS"]

PRODUCTS = [
    (1000000001, "Apple AirPods Pro 2nd Gen", 249.99, "Wireless earbuds with active noise cancellation"),
    (1000000002, "Sony WH-1000XM5 Headphones", 349.99, "Premium wireless noise-canceling headphones"),
    (1000000003, "Anker USB-C Charging Cable 6ft", 19.99, "Braided nylon fast charging cable"),
    (1000000004, "Nike Air Max 270 Running Shoes", 159.99, "Lightweight running shoes with Air Max cushioning"),
    (1000000005, "Lululemon Yoga Mat 5mm", 78.00, "Non-slip yoga mat with alignment lines"),
    (1000000006, "Kindle Paperwhite 11th Gen", 149.99, "E-reader with 6.8 inch display"),
    (1000000007, "MacBook Pro 16-inch M3 Max", 3499.00, "Apple laptop with M3 Max chip"),
    (1000000008, "Dell UltraSharp 32 4K Monitor", 899.99, "32-inch 4K USB-C Hub Monitor"),
    (1000000009, "Dyson V15 Detect Vacuum", 749.99, "Cordless vacuum with laser detection"),
    (1000000010, "Samsung Galaxy S24 Ultra", 1299.99, "Flagship smartphone with S Pen"),
    (1000000011, "Bose QuietComfort Ultra", 429.00, "Premium noise canceling headphones"),
    (1000000012, "Apple Watch Series 9", 399.00, "Smartwatch with health tracking"),
    (1000000013, "Nintendo Switch OLED", 349.99, "Gaming console with OLED screen"),
    (1000000014, "Instant Pot Duo 7-in-1", 89.99, "Multi-use pressure cooker"),
    (1000000015, "Nespresso VertuoPlus", 179.00, "Coffee and espresso machine"),
]


def generate_order(order_num: int) -> dict:
    first_name = random.choice(FIRST_NAMES)
    last_name = random.choice(LAST_NAMES)
    full_name = f"{first_name} {last_name}"
    email = f"{first_name.lower()}.{last_name.lower()}@example.com"
    city, state = random.choice(CITIES_STATES)

    now = datetime.now(timezone.utc)
    ship_date = (now + timedelta(days=1)).strftime("%Y-%m-%d")
    delivery_date = (now + timedelta(days=4)).strftime("%Y-%m-%d")
    timestamp = now.strftime("%Y-%m-%dT%H:%M:%S")

    num_lines = random.randint(1, 3)
    order_lines = []

    for i in range(1, num_lines + 1):
        product = random.choice(PRODUCTS)
        product_id, name, price, desc = product
        quantity = random.randint(1, 3)
        discount = round(price * 0.1, 2) if random.random() < 0.2 else None

        order_lines.append({
            "line_number": i,
            "item_id": product_id,
            "item_name": name,
            "item_description": desc,
            "quantity": quantity,
            "unit_price": price,
            "currency": "USD",
            "tax_rate": 0.08,
            "discount_amount": discount,
            "fulfillment_type": random.choice(FULFILLMENT_TYPES),
            "estimated_ship_date": ship_date,
            "estimated_delivery_date": delivery_date,
            "shipping_address": {
                "full_name": full_name,
                "address_line1": f"{random.randint(1, 999)} Main Street",
                "address_line2": None,
                "city": city,
                "state_province": state,
                "postal_code": str(random.randint(10000, 99999)),
                "country": "USA",
                "phone_number": f"+1-{random.randint(100, 999)}-555-{random.randint(1000, 9999)}",
                "email": email
            }
        })

    return {
        "external_order_id": str(uuid.uuid4()),
        "customer_id": f"CUST-{random.randint(1, 10000):05d}",
        "order_type": random.choice(ORDER_TYPES),
        "channel": random.choice(CHANNELS),
        "order_lines": order_lines,
        "billing_address": {
            "full_name": full_name,
            "address_line1": f"{random.randint(1, 999)} Main Street",
            "address_line2": None,
            "city": city,
            "state_province": state,
            "postal_code": str(random.randint(10000, 99999)),
            "country": "USA",
            "phone_number": f"+1-{random.randint(100, 999)}-555-{random.randint(1000, 9999)}",
            "email": email
        },
        "notes": f"Order #{order_num} - Generated for testing",
        "timestamp": timestamp
    }


def main():
    count = int(sys.argv[1]) if len(sys.argv) > 1 else 10

    for i in range(1, count + 1):
        print(json.dumps(generate_order(i), separators=(',', ':')))

        if i % 100 == 0:
            print(f"  Generated {i}/{count} orders...", file=sys.stderr)


if __name__ == "__main__":
    main()
