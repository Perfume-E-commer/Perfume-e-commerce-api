#!/bin/bash
# Script to check SQLite database content

DB_PATH="/Users/macbook/Documents/Year4/internet_programming/Project/Perfume-e-commerce-api/Perfume_E-commerce/perfume.db"

echo "=== SQLite Database Check ==="
echo "Database file: $DB_PATH"
echo "File size: $(ls -lh "$DB_PATH" | awk '{print $5}')"
echo ""

echo "=== Tables in database ==="
sqlite3 "$DB_PATH" ".tables"
echo ""

echo "=== Product count ==="
sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM products;" 2>/dev/null || echo "Table doesn't exist yet"
echo ""

echo "=== User count ==="
sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM users;" 2>/dev/null || echo "Table doesn't exist yet"
echo ""

echo "=== Order count ==="
sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM orders;" 2>/dev/null || echo "Table doesn't exist yet"
echo ""

echo "=== Sample Products ==="
sqlite3 "$DB_PATH" "SELECT id, name, brand, price, stock FROM products LIMIT 5;" 2>/dev/null || echo "No data or table doesn't exist"
