#!/bin/bash

# GraphQL Subscription Test
echo "🔔 Testing GraphQL Subscription"
echo "=============================="

# Test subscription endpoint
echo "📤 Testing Subscription Health"
curl -s -X GET http://localhost:9000/api/test/subscription-health | jq '.'

echo ""
echo "📤 Testing Property Creation (should trigger subscription)"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation CreateProperty($input: CreatePropertyInput!) { createProperty(input: $input) { id title description propertyType price location area createdAt updatedAt } }",
    "variables": {
      "input": {
        "title": "Subscription Test Property",
        "description": "Testing subscription events",
        "propertyType": "Apartment",
        "price": 250000.0,
        "location": "Subscription City",
        "area": 1200.0,
        "brokerId": "550e8400-e29b-41d4-a716-446655440000"
      }
    }
  }' \
  http://localhost:9000/api/graphql | jq '.'

echo ""
echo "📤 Testing Property Update (should trigger subscription)"
PROPERTY_ID=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { listProperties { id title } }"
  }' \
  http://localhost:9000/api/graphql | jq -r '.listProperties[0].id')

if [ "$PROPERTY_ID" != "null" ] && [ ! -z "$PROPERTY_ID" ]; then
    curl -s -X POST \
      -H "Content-Type: application/json" \
      -d "{
        \"query\": \"mutation UpdatePropertyPrice(\$id: ID!, \$price: Float!) { updatePropertyPrice(id: \$id, price: \$price) { id title price updatedAt } }\",
        \"variables\": {
          \"id\": \"$PROPERTY_ID\",
          \"price\": 500000.0
        }
      }" \
      http://localhost:9000/api/graphql | jq '.'
else
    echo "No properties found to update"
fi

echo ""
echo "✅ Subscription tests completed!"
echo "💡 Check the notification service logs to see if events were received"
