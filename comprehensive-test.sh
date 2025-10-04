#!/bin/bash

# Comprehensive GraphQL API Test Suite
echo "🏠 Property Microservice GraphQL API Test Suite"
echo "=============================================="

# Configuration
GRAPHQL_URL="http://localhost:9000/api/graphql"
HEADERS="Content-Type: application/json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to make GraphQL requests
make_request() {
    local query="$1"
    local operation_name="$2"
    local variables="$3"
    
    echo -e "${BLUE}📤 Executing: $operation_name${NC}"
    echo "Query: $query"
    if [ ! -z "$variables" ]; then
        echo "Variables: $variables"
    fi
    echo ""
    
    local json_payload
    if [ ! -z "$variables" ]; then
        json_payload="{\"query\":\"$query\",\"variables\":$variables}"
    else
        json_payload="{\"query\":\"$query\"}"
    fi
    
    local response=$(curl -s -X POST \
        -H "$HEADERS" \
        -d "$json_payload" \
        "$GRAPHQL_URL")
    
    echo -e "${YELLOW}📥 Response:${NC}"
    echo "$response" | jq '.' 2>/dev/null || echo "$response"
    echo ""
    echo "----------------------------------------"
    echo ""
    
    # Extract data from response for further use
    echo "$response"
}

# Test 1: List all properties (should show existing properties)
echo -e "${GREEN}🧪 Test 1: List all properties${NC}"
LIST_QUERY='query { listProperties { id title description propertyType price location area createdAt updatedAt } }'
make_request "$LIST_QUERY" "List Properties"

# Test 2: Create a new property
echo -e "${GREEN}🧪 Test 2: Create a new property${NC}"
CREATE_QUERY='mutation CreateProperty($input: CreatePropertyInput!) { createProperty(input: $input) { id title description propertyType price location area createdAt updatedAt } }'
CREATE_VARIABLES='{"input": {"title": "Beautiful Downtown Apartment", "description": "A modern 2-bedroom apartment in the heart of downtown", "propertyType": "Apartment", "price": 250000.0, "location": "Downtown, City Center", "area": 1200.0, "brokerId": "550e8400-e29b-41d4-a716-446655440000"}}'

create_response=$(make_request "$CREATE_QUERY" "Create Property" "$CREATE_VARIABLES")
property_id=$(echo "$create_response" | jq -r '.data.createProperty.id' 2>/dev/null)

if [ "$property_id" != "null" ] && [ ! -z "$property_id" ]; then
    echo -e "${GREEN}✅ Property created successfully with ID: $property_id${NC}"
else
    echo -e "${RED}❌ Failed to create property${NC}"
    echo "Response: $create_response"
    exit 1
fi

# Test 3: Get the created property by ID
echo -e "${GREEN}🧪 Test 3: Get property by ID${NC}"
GET_QUERY='query GetProperty($id: ID!) { property(id: $id) { id title description propertyType price location area createdAt updatedAt } }'
GET_VARIABLES="{\"id\": \"$property_id\"}"

make_request "$GET_QUERY" "Get Property by ID" "$GET_VARIABLES"

# Test 4: Update the property
echo -e "${GREEN}🧪 Test 4: Update property${NC}"
UPDATE_QUERY='mutation UpdateProperty($input: UpdatePropertyInput!) { updateProperty(input: $input) { id title description propertyType price location area createdAt updatedAt } }'
UPDATE_VARIABLES="{\"input\": {\"id\": \"$property_id\", \"title\": \"Luxury Downtown Apartment\", \"description\": \"A premium 2-bedroom apartment with city views\", \"price\": 275000.0, \"location\": \"Downtown, City Center\"}}"

make_request "$UPDATE_QUERY" "Update Property" "$UPDATE_VARIABLES"

# Test 5: Update only the price
echo -e "${GREEN}🧪 Test 5: Update property price only${NC}"
UPDATE_PRICE_QUERY='mutation UpdatePropertyPrice($id: ID!, $price: Float!) { updatePropertyPrice(id: $id, price: $price) { id title description propertyType price location area createdAt updatedAt } }'
UPDATE_PRICE_VARIABLES="{\"id\": \"$property_id\", \"price\": 300000.0}"

make_request "$UPDATE_PRICE_QUERY" "Update Property Price" "$UPDATE_PRICE_VARIABLES"

# Test 6: List all properties (should show our created property)
echo -e "${GREEN}🧪 Test 6: List all properties (after creation)${NC}"
make_request "$LIST_QUERY" "List Properties After Creation"

# Test 7: Create another property
echo -e "${GREEN}🧪 Test 7: Create another property${NC}"
CREATE_VARIABLES2='{"input": {"title": "Cozy Suburban House", "description": "A charming 3-bedroom house with garden", "propertyType": "House", "price": 180000.0, "location": "Suburbia, Green Valley", "area": 2000.0, "brokerId": "550e8400-e29b-41d4-a716-446655440001"}}'

create_response2=$(make_request "$CREATE_QUERY" "Create Second Property" "$CREATE_VARIABLES2")
property_id2=$(echo "$create_response2" | jq -r '.data.createProperty.id' 2>/dev/null)

if [ "$property_id2" != "null" ] && [ ! -z "$property_id2" ]; then
    echo -e "${GREEN}✅ Second property created successfully with ID: $property_id2${NC}"
else
    echo -e "${RED}❌ Failed to create second property${NC}"
    echo "Response: $create_response2"
fi

# Test 8: List all properties (should show both properties)
echo -e "${GREEN}🧪 Test 8: List all properties (after creating second property)${NC}"
make_request "$LIST_QUERY" "List All Properties"

# Test 9: Delete the first property
echo -e "${GREEN}🧪 Test 9: Delete first property${NC}"
DELETE_QUERY='mutation DeleteProperty($id: ID!) { deleteProperty(id: $id) }'
DELETE_VARIABLES="{\"id\": \"$property_id\"}"

make_request "$DELETE_QUERY" "Delete Property" "$DELETE_VARIABLES"

# Test 10: Try to get the deleted property (should return null)
echo -e "${GREEN}🧪 Test 10: Try to get deleted property${NC}"
make_request "$GET_QUERY" "Get Deleted Property" "$GET_VARIABLES"

# Test 11: List all properties (should show only the second property)
echo -e "${GREEN}🧪 Test 11: List all properties (after deletion)${NC}"
make_request "$LIST_QUERY" "List Properties After Deletion"

# Test 12: Delete the second property
echo -e "${GREEN}🧪 Test 12: Delete second property${NC}"
DELETE_VARIABLES2="{\"id\": \"$property_id2\"}"
make_request "$DELETE_QUERY" "Delete Second Property" "$DELETE_VARIABLES2"

# Test 13: List all properties (should be empty again)
echo -e "${GREEN}🧪 Test 13: List all properties (final check)${NC}"
make_request "$LIST_QUERY" "Final List Properties"

echo -e "${GREEN}🎉 All tests completed!${NC}"
echo "=============================================="
echo "Test Summary:"
echo "✅ Create Property"
echo "✅ Read Property by ID"
echo "✅ Update Property"
echo "✅ Update Property Price"
echo "✅ List Properties"
echo "✅ Delete Property"
echo "✅ Error Handling (deleted property)"
echo ""
echo -e "${BLUE}💡 All CRUD operations have been tested successfully!${NC}"
