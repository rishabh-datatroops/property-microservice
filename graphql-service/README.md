# GraphQL Service

A well-structured GraphQL service that acts as a gateway to other microservices in the property management system, built with **graphql-java**.

## Architecture

This service follows a clean architecture pattern with trait-based APIs:

- **Messages**: Request/response data structures
- **API Traits**: Interface definitions for external service calls
- **REST Implementations**: HTTP client implementations of API traits
- **Services**: High-level service clients using trait-based APIs
- **Builders**: Separate builders for schema, queries, mutations, and subscriptions
- **Data Fetchers**: Separate data fetchers for each operation type
- **Controllers**: HTTP endpoints for GraphQL and health checks

## Structure

```
app/
├── api/
│   ├── property/
│   │   ├── PropertyApi.scala        # Property API trait definitions
│   │   └── rest/
│   │       └── PropertyRestApi.scala # REST implementations
│   └── notification/
│       ├── NotificationApi.scala    # Notification API trait definitions
│       └── rest/
│           └── NotificationRestApi.scala # REST implementations
├── constants/
│   └── Defaults.scala               # Default configuration values
├── controllers/
│   └── GraphQLController.scala      # GraphQL endpoint and health check
├── executor/
│   └── DefaultExecutor.scala        # Default execution context
├── graphql/
│   ├── builder/
│   │   ├── SchemaBuilder.scala      # Main GraphQL schema builder
│   │   ├── QueryBuilder.scala       # Query type builder
│   │   ├── MutationBuilder.scala    # Mutation type builder
│   │   └── SubscriptionBuilder.scala # Subscription type builder
│   ├── datafetchers/
│   │   ├── PropertyQueryDataFetcher.scala      # Query data fetchers
│   │   ├── PropertyMutationDataFetcher.scala   # Mutation data fetchers
│   │   └── PropertySubscriptionDataFetcher.scala # Subscription data fetchers
│   └── PropertyGraphQL.scala        # Main GraphQL orchestrator
├── messages/
│   ├── property/
│   │   └── PropertyMessages.scala   # Property message types
│   └── notification/
│       └── NotificationMessages.scala # Notification message types
├── models/
│   └── Property.scala               # Property data model (re-export)
├── rest/
│   └── RestRequest.scala            # HTTP request utility
└── services/
    ├── ListingServiceClient.scala   # Property service client
    └── NotificationServiceClient.scala  # Notification service client
```

## Features

### Queries
- `listProperties`: Get all properties from listing service
- `property(id: ID!)`: Get a specific property by ID

### Mutations
- `registerProperty`: Create a new property and notify users

### Subscriptions
- `newProperty`: Real-time updates when new properties are added

## External Service Integration

The service integrates with:
- **Listing Service** (port 9001): Property CRUD operations
- **Notification Service** (port 9002): User notifications

## Configuration

Service URLs are configured in the service clients:
- Listing Service: `http://localhost:9001`
- Notification Service: `http://localhost:9002`

## Endpoints

- `POST /api/graphql`: GraphQL endpoint
- `GET /health`: Health check endpoint

## Dependencies

- Play Framework
- **graphql-java** (GraphQL library)
- Akka Streams (for subscriptions)
- Play WS (for HTTP clients)

## API Pattern

The service uses a trait-based API pattern similar to the provided example:

```scala
// API Trait Definition
trait ListPropertiesRequest {
  def apply()(
    implicit timeout: FiniteDuration = Defaults.timeout
  ): Future[PropertyList]
}

// REST Implementation
@Singleton()
private[property] final class ListPropertiesRestRequest @Inject() (
  implicit private val defaultExecutor: DefaultExecutor,
  private val wsClient: WSClient)
  extends ListPropertiesRequest
    with PropertyUrl {

  protected val route = "properties"

  def apply()(
    implicit timeout: FiniteDuration): Future[PropertyList] = {

    RestRequest
      .to(url)
      .withTimeout(timeout)
      .getMessage[PropertyList]()
  }
}
```

### Key Features:
- **Implicit Parameters**: `timeout` for consistent request handling
- **Trait-based Design**: Clean separation between interface and implementation
- **Error Handling**: Centralized error handling in RestRequest utility
- **Simplified API**: Minimal implicit parameters for clean usage

## GraphQL Schema

The service uses graphql-java with a schema-first approach:

```graphql
type Property {
  id: ID!
  title: String!
  description: String
  propertyType: String!
  price: Float!
  location: String!
  area: Float
  createdAt: String!
  updatedAt: String!
}

type Query {
  listProperties: [Property!]!
  property(id: ID!): Property
}

type Mutation {
  registerProperty(
    title: String!
    price: Float!
    location: String!
    propertyType: String!
    description: String
    area: Float
  ): Property!
}

type Subscription {
  newProperty: Property!
}
```
