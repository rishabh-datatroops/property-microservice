#!/bin/bash

# Simple GraphQL API Test
echo "🧪 Testing GraphQL API..."

# Test 1: Simple query without variables
echo "📤 Test 1: List Properties"
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"query":"query { listProperties { id title description propertyType price location area createdAt updatedAt } }"}' \
  http://localhost:9000/api/graphql

echo ""
echo "----------------------------------------"
echo ""

# Test 2: Create a property
echo "📤 Test 2: Create Property"
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation CreateProperty($input: CreatePropertyInput!) { createProperty(input: $input) { id title description propertyType price location area createdAt updatedAt } }",
    "variables": {
      "input": {
        "title": "Test Property",
        "description": "A test property",
        "propertyType": "Apartment",
        "price": 200000.0,
        "location": "Test City",
        "area": 1000.0,
        "brokerId": "test-broker-123"
      }
    }
  }' \
  http://localhost:9000/api/graphql

echo ""
echo "----------------------------------------"
echo ""

echo "✅ Basic tests completed!"
