#!/bin/bash

# GraphQL Subscription Testing Guide
echo "🔔 GraphQL Subscription Testing Guide"
echo "====================================="

echo ""
echo "📋 Method 1: Using GraphQL Playground"
echo "-------------------------------------"
echo "1. Open GraphQL Playground: http://localhost:9000"
echo "2. Use this subscription query:"
echo ""
cat << 'EOF'
subscription {
  newProperty {
    id
    title
    description
    propertyType
    price
    location
    area
    createdAt
    updatedAt
  }
}
EOF

echo ""
echo "3. Then in another tab, create a property:"
echo ""
cat << 'EOF'
mutation {
  createProperty(input: {
    title: "Subscription Test Property"
    description: "Testing real-time subscription"
    propertyType: "Apartment"
    price: 300000.0
    location: "Test City"
    area: 1200.0
    brokerId: "550e8400-e29b-41d4-a716-446655440000"
  }) {
    id
    title
    price
  }
}
EOF

echo ""
echo "📋 Method 2: Using curl with Server-Sent Events"
echo "-----------------------------------------------"
echo "Test subscription endpoint:"
echo "curl -N -H 'Accept: text/event-stream' -H 'Content-Type: application/json' \\"
echo "  -d '{\"query\":\"subscription { newProperty { id title price } }\"}' \\"
echo "  http://localhost:9000/api/graphql"

echo ""
echo "📋 Method 3: Using WebSocket Client"
echo "-----------------------------------"
echo "GraphQL subscriptions typically use WebSocket connections."
echo "You can use tools like:"
echo "- GraphQL Playground (recommended)"
echo "- Insomnia"
echo "- Postman"
echo "- Custom WebSocket client"

echo ""
echo "📋 Method 4: Programmatic Testing"
echo "--------------------------------"
echo "Here's a Node.js example to test subscriptions:"
echo ""
cat << 'EOF'
const { createClient } = require('graphql-ws');

const client = createClient({
  url: 'ws://localhost:9000/subscriptions',
});

client.subscribe(
  {
    query: `
      subscription {
        newProperty {
          id
          title
          price
          location
        }
      }
    `,
  },
  {
    next: (data) => console.log('Received:', data),
    error: (err) => console.error('Error:', err),
    complete: () => console.log('Subscription completed'),
  }
);
EOF

echo ""
echo "📋 Method 5: Test Endpoints"
echo "---------------------------"
echo "Your service provides test endpoints:"
echo ""

echo "🧪 Test 1: Subscription Health Check"
curl -s http://localhost:9000/api/test/subscription-health | jq '.'

echo ""
echo "🧪 Test 2: Trigger Property Creation"
curl -s -X POST http://localhost:9000/api/test/property-creation | jq '.'

echo ""
echo "🧪 Test 3: Trigger Property Update"
curl -s -X POST http://localhost:9000/api/test/property-update | jq '.'

echo ""
echo "📋 Method 6: Monitor Logs"
echo "------------------------"
echo "Watch the GraphQL service logs for subscription events:"
echo "docker-compose logs -f graphql-service | grep -i 'broadcast\|subscription'"

echo ""
echo "🎯 Quick Test Commands:"
echo "======================"
echo "1. Check subscription health:"
echo "   curl http://localhost:9000/api/test/subscription-health"
echo ""
echo "2. Trigger property creation:"
echo "   curl -X POST http://localhost:9000/api/test/property-creation"
echo ""
echo "3. Trigger property update:"
echo "   curl -X POST http://localhost:9000/api/test/property-update"
echo ""
echo "4. Monitor logs:"
echo "   docker-compose logs -f graphql-service"
echo ""
echo "✅ All subscription testing methods are ready!"
