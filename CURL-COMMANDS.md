# GraphQL Property Management - Complete Curl Commands

This document contains all the curl commands for testing the enhanced GraphQL Property Management API.

## Service Configuration
- **GraphQL Endpoint**: `http://localhost:9000/api/graphql`
- **Health Check**: `http://localhost:9000/health`
- **GraphQL Playground**: `http://localhost:9000/graphql`

---

## 1. Health Check

```bash
curl -X GET http://localhost:9000/health
```

---

## 2. Query Operations

### List All Properties
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "query { listProperties { id title price location propertyType area description createdAt updatedAt } }"}'
```

### Get Property by ID
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "query { property(id: \"your-property-id-here\") { id title price location propertyType area description createdAt updatedAt } }"}'
```

---

## 3. Create Property Operations

### Create Property (Enhanced Method)
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { createProperty(input: { title: \"Luxury Downtown Condo\", price: 850000.0, location: \"Downtown District\", propertyType: \"Condo\", description: \"Beautiful luxury condominium with city views\", area: 1800.0, brokerId: \"broker-123\" }) { id title price location propertyType area description createdAt } }"
  }'
```

### Create Property (Original Method)
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { registerProperty(title: \"Cozy Suburban House\", price: 675000.0, location: \"Suburban Area\", propertyType: \"House\", description: \"Perfect family home with garden\", area: 2200.0) { id title price location propertyType area createdAt } }"
  }'
```

### Create Property (Minimal Fields)
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { createProperty(input: { title: \"Basic Property\", price: 450000.0, location: \"Basic Location\", propertyType: \"Apartment\" }) { id title price location propertyType } }"
  }'
```

### Create Multiple Properties
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { prop1: createProperty(input: { title: \"Bulk Property 1\", price: 450000.0, location: \"Bulk City 1\", propertyType: \"Studio\" }) { id title } prop2: createProperty(input: { title: \"Bulk Property 2\", price: 650000.0, location: \"Bulk City 2\", propertyType: \"Apartment\" }) { id title } prop3: createProperty(input: { title: \"Bulk Property 3\", price: 850000.0, location: \"Bulk City 3\", propertyType: \"House\" }) { id title } }"
  }'
```

---

## 4. Update Property Operations

### Update Property (Full Update)
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { updateProperty(input: { id: \"your-property-id-here\", title: \"UPDATED Title\", price: 950000.0, location: \"UPDATED Location\", propertyType: \"Condo\", description: \"UPDATED description\", area: 2000.0 }) { id title price location propertyType area description updatedAt } }"
  }'
```

### Update Property (Partial Update - Title Only)
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { updateProperty(input: { id: \"your-property-id-here\", title: \"NEW UPDATED Title\" }) { id title price location propertyType updatedAt } }"
  }'
```

### Update Property (Partial Update - Description Only)
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { updateProperty(input: { id: \"your-property-id-here\", description: \"UPDATED: Premium property with all amenities\" }) { id title description updatedAt } }"
  }'
```

### Update Property (Price Only - Quick Update)
```bash
 curl -X POST http://localhost:9000/api/graphql \
   -H "Content-Type: application/json" \
   -d '{
     "query": "mutation { updatePropertyPrice(id: \"your-property-id-here\", price: 1100000.0) { id title price updatedAt } }"
   }'
```

---

## 5. Delete Property Operations

### Delete Property
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { deleteProperty(id: \"your-property-id-here\") }"
  }'
```

---

## 6. Error Handling Tests

### Invalid GraphQL Query
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "invalid { prop"}'
```

### Missing Required Fields
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { createProperty(input: { title: \"Incomplete Property\" }) { id title } }"
  }'
```

### Update Non-existent Property
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { updateProperty(input: { id: \"00000000-0000-0000-0000-000000000999\", title: \"This Should Fail\" }) { id title } }"
  }'
```

### Delete Non-existent Property
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { deleteProperty(id: \"00000000-0000-0000-0000-000000000999\") }"
  }'
```

---

## 7. Advanced Operations

### Query with Variables
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation CreateProperty($title: String!, $price: Float!, $location: String!, $propertyType: String!) { createProperty(input: { title: $title, price: $price, location: $location, propertyType: $propertyType }) { id title price } }",
    "variables": {
      "title": "Variable-based Property",
      "price": 789000.0,
      "location": "Variable City",
      "propertyType": "Duplex"
    }
  }'
```

### Complex Multi-field Query
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { listProperties { id title description price location propertyType area createdAt updatedAt } }"
  }'
```

---

## 8. Complete Test Sequence

### Step-by-Step Testing Workflow

1. **Health Check**
```bash
curl -X GET http://localhost:9000/health
```

2. **Create Test Property**
```bash
TEST_PROP=$(curl -s -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { createProperty(input: { title: \"Test Property\", price: 550000.0, location: \"Test City\", propertyType: \"Apartment\", description: \"Test property for updates\", area: 1500.0 }) { id title price } }"
  }')
echo "Created property: $TEST_PROP"
```

3. **List All Properties**
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "query { listProperties { id title price } }"}'
```

4. **Update Property Price**
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { updatePropertyPrice(id: \"YOUR_EXTRACTED_ID_HERE\", price: 650000.0) { id title price updatedAt } }"
  }'
```

5. **Update Property Details**
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { updateProperty(input: { id: \"YOUR_EXTRACTED_ID_HERE\", title: \"Updated Test Property\", description: \"Updated test property description\" }) { id title description updatedAt } }"
  }'
```

6. **Delete Test Property**
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { deleteProperty(id: \"YOUR_EXTRACTED_ID_HERE\") }"
  }'
```

7. **Final Verification**
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "query { listProperties { id title price } }"}'
```

---

## 9. Quick Reference Table

| Operation | Mutation | Purpose |
|-----------|----------|---------|
| Create | `createProperty` | Create new property with structured input |
| Create | `registerProperty` | Original property creation method |
| Update | `updateProperty` | Full or partial property updates |
| Update | `updatePropertyPrice` | Quick price-only updates |
| Delete | `deleteProperty` | Remove property from system |
| Query | `listProperties` | Get all properties |
| Query | `property(id)` | Get specific property |

---

## 10. Automated Testing Script

For comprehensive automated testing, use the provided script:

```bash
./test-all-curls.sh
```

This script runs all the curl commands automatically with user interaction and detailed output formatting.

---

## 💡 Tips for Testing

1. **Start Services**: `docker-compose up -d`
2. **Replace IDs**: Replace `your-property-id-here` with actual property IDs from responses
3. **Check Responses**: Look for `"data"` objects in successful responses and `"errors"` arrays for failures
4. **Interactive Testing**: Visit `http://localhost:9000/graphql` for the GraphQL playground
5. **Extract IDs**: Use `grep` to extract property IDs: `echo "$response" | grep -o '"id":"[^"]*"'`

---

## 🎯 Expected Response Format

### Success Response
```json
{
  "data": {
    "createProperty": {
      "id": "uuid-here",
      "title": "Property Title",
      "price": 550000.0,
      "location": "Location Name",
      "propertyType": "Apartment"
    }
  }
}
```

### Error Response
```json
{
  "errors": [
    {
      "message": "Error description here",
      "locations": [],
      "path": ["mutation", "field"]
    }
  ]
}
```

Use these curl commands to thoroughly test all aspects of your enhanced GraphQL Property Management API! 🚀
