#!/bin/bash

# Real-time Subscription Test
echo "🔔 Real-time GraphQL Subscription Test"
echo "====================================="

echo ""
echo "📡 Testing Subscription Broadcasting..."
echo ""

# Function to monitor logs in background
monitor_logs() {
    echo "🔍 Monitoring GraphQL service logs for subscription events..."
    docker-compose logs -f graphql-service | grep -i "broadcast\|subscription" &
    MONITOR_PID=$!
    sleep 2
}

# Function to cleanup
cleanup() {
    if [ ! -z "$MONITOR_PID" ]; then
        kill $MONITOR_PID 2>/dev/null
    fi
    echo ""
    echo "🧹 Cleanup completed"
}

# Set trap for cleanup
trap cleanup EXIT

# Start monitoring
monitor_logs

echo "📤 Test 1: Trigger Property Creation (should broadcast to subscribers)"
echo "----------------------------------------------------------------------"
curl -s -X POST http://localhost:9000/api/test/property-creation | jq '.message'

echo ""
echo "📤 Test 2: Trigger Property Update (should broadcast to subscribers)"
echo "------------------------------------------------------------------"
curl -s -X POST http://localhost:9000/api/test/property-update | jq '.message'

echo ""
echo "📤 Test 3: Create Property via GraphQL (should broadcast to subscribers)"
echo "------------------------------------------------------------------------"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation CreateProperty($input: CreatePropertyInput!) { createProperty(input: $input) { id title description propertyType price location area createdAt updatedAt } }",
    "variables": {
      "input": {
        "title": "Live Subscription Test",
        "description": "Testing real-time subscription broadcasting",
        "propertyType": "Apartment",
        "price": 275000.0,
        "location": "Subscription City",
        "area": 1100.0,
        "brokerId": "550e8400-e29b-41d4-a716-446655440000"
      }
    }
  }' \
  http://localhost:9000/api/graphql | jq '.createProperty.title'

echo ""
echo "📤 Test 4: Update Property Price (should broadcast to subscribers)"
echo "----------------------------------------------------------------"
# Get first property ID
PROPERTY_ID=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"query": "query { listProperties { id title } }"}' \
  http://localhost:9000/api/graphql | jq -r '.listProperties[0].id')

if [ "$PROPERTY_ID" != "null" ] && [ ! -z "$PROPERTY_ID" ]; then
    curl -s -X POST \
      -H "Content-Type: application/json" \
      -d "{
        \"query\": \"mutation UpdatePropertyPrice(\$id: ID!, \$price: Float!) { updatePropertyPrice(id: \$id, price: \$price) { id title price updatedAt } }\",
        \"variables\": {
          \"id\": \"$PROPERTY_ID\",
          \"price\": 999999.0
        }
      }" \
      http://localhost:9000/api/graphql | jq '.updatePropertyPrice.title'
else
    echo "No properties found to update"
fi

echo ""
echo "⏱️  Waiting 3 seconds to see subscription events..."
sleep 3

echo ""
echo "✅ Subscription Test Complete!"
echo ""
echo "📊 What to look for in the logs:"
echo "   - 'Broadcasted new property to subscribers: [property-id]'"
echo "   - 'Broadcasted property update to subscribers: [property-id]'"
echo ""
echo "🔔 If you see these messages, subscriptions are working!"
echo "   The GraphQL service is successfully broadcasting events to all subscribers."
