# GraphQL Subscription Implementation

## Overview

The `PropertySubscriptionDataFetcher` has been completely rewritten to provide real-time property updates using reactive streams. This implementation enables GraphQL clients to subscribe to property changes and receive live updates.

## Key Features

### 1. Real-time Subscriptions
- **Reactive Streams**: Uses custom Publisher/Subscriber implementation for GraphQL subscriptions
- **Broadcasting**: Multiple clients can subscribe to the same property updates
- **Thread-safe**: Concurrent subscriber management with proper cleanup

### 2. Integration Points
- **Mutation Integration**: Property creation/updates automatically broadcast to subscribers
- **Kafka Integration**: External property events can trigger subscriptions
- **Service Integration**: Seamless integration with existing microservices

### 3. Architecture Components

#### PropertySubscriptionDataFetcher
- Custom Publisher implementation for GraphQL subscriptions
- Manages subscriber lifecycle and broadcasting
- Thread-safe concurrent subscriber management

#### PropertyUpdateService
- Central service for broadcasting property events
- Integrates with mutation data fetchers
- Handles property creation, updates, and deletions

#### PropertyEventConsumer
- Kafka consumer for external property events
- Automatically broadcasts events to GraphQL subscribers
- Integrates with the notification service

## Usage

### GraphQL Subscription Query

```graphql
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
```

### Testing Endpoints

#### Test Property Creation
```bash
curl -X POST http://localhost:9000/api/test/property-creation
```

#### Test Property Update
```bash
curl -X POST http://localhost:9000/api/test/property-update
```

#### Subscription Health Check
```bash
curl http://localhost:9000/api/test/subscription-health
```

### GraphQL Mutation (Triggers Subscription)

```graphql
mutation {
  createProperty(input: {
    title: "New Property"
    price: 250000.0
    location: "Test Location"
    propertyType: "Apartment"
    description: "Test description"
    area: 100.0
  }) {
    id
    title
    price
  }
}
```

## Implementation Details

### Custom Publisher Implementation

The subscription uses a custom `PropertyPublisher` that:
- Maintains a thread-safe list of subscribers
- Handles subscription lifecycle (subscribe/unsubscribe)
- Broadcasts property updates to all active subscribers
- Implements proper error handling and cleanup

### Integration Flow

1. **Property Creation/Update**: Mutation data fetcher calls `PropertyUpdateService`
2. **Broadcasting**: Service broadcasts the property to all GraphQL subscribers
3. **Real-time Delivery**: Subscribers receive the property update immediately
4. **External Events**: Kafka consumer can also trigger broadcasts

### Error Handling

- Graceful subscriber error handling
- Automatic cleanup of failed subscribers
- Proper resource management and cleanup

## Configuration

### Dependencies Added
- `akka-stream-typed` for reactive streams
- `reactive-streams` for Publisher/Subscriber interfaces

### Kafka Configuration
The service listens to `property-events` topic for external property updates.

## Testing

### Manual Testing
1. Start the GraphQL service
2. Use a GraphQL client (like GraphQL Playground) to subscribe to `newProperty`
3. Trigger property creation via mutation or test endpoint
4. Observe real-time updates in the subscription

### Example Test Flow
```bash
# 1. Subscribe to newProperty in GraphQL client
# 2. Create a property via mutation
# 3. See the property appear in the subscription
# 4. Update the property
# 5. See the update in the subscription
```

## Production Considerations

### Backpressure
The current implementation doesn't implement backpressure. For production:
- Implement proper request/response flow
- Add subscriber capacity management
- Monitor subscription performance

### Scalability
- Consider using distributed pub/sub (Redis, Apache Pulsar)
- Implement subscription persistence for reliability
- Add metrics and monitoring

### Security
- Add authentication/authorization for subscriptions
- Implement rate limiting for subscribers
- Add subscription validation and filtering
