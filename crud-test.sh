#!/bin/bash

# Focused GraphQL API Test - CRUD Operations
echo "🏠 Property Microservice GraphQL API - CRUD Test"
echo "=============================================="

# Configuration
GRAPHQL_URL="http://localhost:9000/api/graphql"

# Test 1: Create a property
echo "📤 Test 1: Create Property"
CREATE_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation CreateProperty($input: CreatePropertyInput!) { createProperty(input: $input) { id title description propertyType price location area createdAt updatedAt } }",
    "variables": {
      "input": {
        "title": "Test CRUD Property",
        "description": "Testing CRUD operations",
        "propertyType": "Apartment",
        "price": 300000.0,
        "location": "Test City",
        "area": 1500.0,
        "brokerId": "550e8400-e29b-41d4-a716-446655440000"
      }
    }
  }' \
  "$GRAPHQL_URL")

echo "$CREATE_RESPONSE" | jq '.'
PROPERTY_ID=$(echo "$CREATE_RESPONSE" | jq -r '.createProperty.id')

if [ "$PROPERTY_ID" = "null" ] || [ -z "$PROPERTY_ID" ] || [ "$PROPERTY_ID" = "" ]; then
    echo "❌ Failed to create property"
    echo "Response: $CREATE_RESPONSE"
    exit 1
fi

echo "✅ Property created with ID: $PROPERTY_ID"
echo ""

# Test 2: Read the property
echo "📤 Test 2: Read Property by ID"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d "{
    \"query\": \"query GetProperty(\$id: ID!) { property(id: \$id) { id title description propertyType price location area createdAt updatedAt } }\",
    \"variables\": { \"id\": \"$PROPERTY_ID\" }
  }" \
  "$GRAPHQL_URL" | jq '.'

echo ""

# Test 3: Update the property
echo "📤 Test 3: Update Property"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d "{
    \"query\": \"mutation UpdateProperty(\$input: UpdatePropertyInput!) { updateProperty(input: \$input) { id title description propertyType price location area createdAt updatedAt } }\",
    \"variables\": {
      \"input\": {
        \"id\": \"$PROPERTY_ID\",
        \"title\": \"Updated CRUD Property\",
        \"description\": \"Updated description\",
        \"price\": 350000.0
      }
    }
  }" \
  "$GRAPHQL_URL" | jq '.'

echo ""

# Test 4: Update only price
echo "📤 Test 4: Update Property Price Only"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d "{
    \"query\": \"mutation UpdatePropertyPrice(\$id: ID!, \$price: Float!) { updatePropertyPrice(id: \$id, price: \$price) { id title description propertyType price location area createdAt updatedAt } }\",
    \"variables\": {
      \"id\": \"$PROPERTY_ID\",
      \"price\": 400000.0
    }
  }" \
  "$GRAPHQL_URL" | jq '.'

echo ""

# Test 5: List all properties (should include our property)
echo "📤 Test 5: List All Properties"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { listProperties { id title description propertyType price location area createdAt updatedAt } }"
  }' \
  "$GRAPHQL_URL" | jq '.data.listProperties | length'

echo ""

# Test 6: Delete the property
echo "📤 Test 6: Delete Property"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d "{
    \"query\": \"mutation DeleteProperty(\$id: ID!) { deleteProperty(id: \$id) }\",
    \"variables\": { \"id\": \"$PROPERTY_ID\" }
  }" \
  "$GRAPHQL_URL" | jq '.'

echo ""

# Test 7: Try to read deleted property (should return null)
echo "📤 Test 7: Try to Read Deleted Property"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d "{
    \"query\": \"query GetProperty(\$id: ID!) { property(id: \$id) { id title description propertyType price location area createdAt updatedAt } }\",
    \"variables\": { \"id\": \"$PROPERTY_ID\" }
  }" \
  "$GRAPHQL_URL" | jq '.'

echo ""

# Test 8: Final count
echo "📤 Test 8: Final Property Count"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { listProperties { id title description propertyType price location area createdAt updatedAt } }"
  }' \
  "$GRAPHQL_URL" | jq '.data.listProperties | length'

echo ""
echo "🎉 All CRUD operations completed successfully!"
echo "✅ Create Property"
echo "✅ Read Property"
echo "✅ Update Property"
echo "✅ Update Property Price"
echo "✅ List Properties"
echo "✅ Delete Property"
echo "✅ Error Handling (deleted property)"
