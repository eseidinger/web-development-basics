#!/bin/bash

# Script to import Keycloak realm via admin REST API
# This runs after Keycloak starts and imports the realm export JSON

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8090}"
ADMIN_USER="admin"
ADMIN_PASSWORD="admin"
REALM_FILE="/tmp/keycloak-realm-export.json"

echo "Using Keycloak URL: $KEYCLOAK_URL"
echo "Waiting for Keycloak to be ready at $KEYCLOAK_URL..."
for i in {1..60}; do
  if curl -s "$KEYCLOAK_URL/health/ready" > /dev/null 2>&1; then
    echo "Keycloak is ready!"
    break
  fi
  if [ $i -eq 60 ]; then
    echo "Keycloak failed to become ready"
    exit 1
  fi
  sleep 1
done

# Get access token
echo "Getting admin access token..."
TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=admin-cli" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASSWORD" \
  -d "grant_type=password" | jq -r '.access_token')

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "Failed to get admin token"
  exit 1
fi

echo "Successfully obtained admin token"

# Check if realm already exists
echo "Checking if taskboard realm exists..."
REALM_CHECK=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/taskboard" \
  -H "Authorization: Bearer $TOKEN")

if echo "$REALM_CHECK" | jq -e '.realm' > /dev/null 2>&1; then
  echo "Realm taskboard already exists, skipping import"
  exit 0
fi

# Import realm
echo "Importing taskboard realm from $REALM_FILE..."
if [ ! -f "$REALM_FILE" ]; then
  echo "Error: Realm export file not found at $REALM_FILE"
  exit 1
fi

IMPORT_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$KEYCLOAK_URL/admin/realms" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d @"$REALM_FILE")

IMPORT_HTTP_CODE=$(echo "$IMPORT_RESPONSE" | tail -n1)
IMPORT_BODY=$(echo "$IMPORT_RESPONSE" | head -n-1)

echo "Import HTTP status: $IMPORT_HTTP_CODE"
if [ -n "$IMPORT_BODY" ]; then
  echo "Import response: $IMPORT_BODY"
fi

# Verify import
sleep 2
REALM_CHECK=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/taskboard" \
  -H "Authorization: Bearer $TOKEN")

if echo "$REALM_CHECK" | jq -e '.realm' > /dev/null 2>&1; then
  echo "Successfully imported taskboard realm"
  exit 0
else
  echo "Failed to import realm"
  exit 1
fi
