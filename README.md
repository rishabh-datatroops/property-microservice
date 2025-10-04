# Property Management Microservices

A comprehensive property management system built with Scala, Play Framework, GraphQL, and Apache Kafka. This microservices architecture provides a scalable solution for managing property listings with real-time notifications and event-driven communication.

## 🏗️ Architecture Overview

The system consists of three main microservices:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  GraphQL Service │    │ Listing Service │    │Notification Svc │
│   (Port: 9000)  │    │   (Port: 9001)  │    │   (Port: 9002)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Apache Kafka (Port: 9092)                    │
│                      Event Streaming                            │
└─────────────────────────────────────────────────────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                    PostgreSQL (Port: 5432)                     │
│                      Data Persistence                           │
└─────────────────────────────────────────────────────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 11+ (for local development)
- SBT (Scala Build Tool)

### Running the System

1. **Start all services with Docker Compose:**
```bash
docker-compose up -d
```

2. **Verify services are running:**
```bash
# Check GraphQL service health
curl http://localhost:9000/health

# Check Listing service
curl http://localhost:9001/

# Check Notification service  
curl http://localhost:9002/
```

3. **Access GraphQL Playground:**
   - Visit: `http://localhost:9000/api/graphql`
   - Use the interactive GraphQL interface for testing

## 📋 Services Overview

### 1. GraphQL Service (Port: 9000)
**Purpose:** Unified API gateway providing GraphQL interface for all property operations

**Key Features:**
- Single GraphQL endpoint for all operations
- Real-time subscriptions for new properties
- Unified interface for property CRUD operations
- Health monitoring endpoint

**Technology Stack:**
- Scala with Play Framework
- GraphQL Java implementation
- HTTP client for service communication

### 2. Listing Service (Port: 9001)
**Purpose:** Core property management service handling CRUD operations and data persistence

**Key Features:**
- Property CRUD operations
- PostgreSQL database integration
- Kafka event publishing
- RESTful API endpoints

**Technology Stack:**
- Scala with Play Framework
- PostgreSQL with Slick ORM
- Apache Kafka producer
- Akka Streams

### 3. Notification Service (Port: 9002)
**Purpose:** Event-driven notification system processing property changes

**Key Features:**
- Kafka event consumption
- User preference management
- Property change detection
- Notification processing

**Technology Stack:**
- Scala with Play Framework
- Apache Kafka consumer
- Akka Streams
- Event-driven architecture

## 🔌 API Endpoints

### GraphQL Service Endpoints

#### Main GraphQL Endpoint
```
POST /api/graphql
Content-Type: application/json

Body: {
  "query": "your GraphQL query/mutation here",
  "variables": { /* optional variables */ }
}
```

#### Health Check
```
GET /health
Response: {"status": "healthy", "service": "graphql-service"}
```

### GraphQL Schema

#### Types
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

input CreatePropertyInput {
  title: String!
  price: Float!
  location: String!
  propertyType: String!
  description: String
  area: Float
  brokerId: String
}

input UpdatePropertyInput {
  id: ID!
  title: String
  price: Float
  location: String
  propertyType: String
  description: String
  area: Float
}
```

#### Queries
```graphql
# List all properties
query {
  listProperties {
    id
    title
    price
    location
    propertyType
    area
    description
    createdAt
    updatedAt
  }
}

# Get specific property
query {
  property(id: "property-uuid-here") {
    id
    title
    price
    location
    propertyType
    area
    description
    createdAt
    updatedAt
  }
}
```

#### Mutations
```graphql
# Create new property
mutation {
  createProperty(input: {
    title: "Luxury Downtown Condo"
    price: 850000.0
    location: "Downtown District"
    propertyType: "Condo"
    description: "Beautiful luxury condominium with city views"
    area: 1800.0
    brokerId: "broker-123"
  }) {
    id
    title
    price
    location
    propertyType
    area
    description
    createdAt
  }
}

# Update property
mutation {
  updateProperty(input: {
    id: "property-uuid-here"
    title: "Updated Title"
    price: 950000.0
    description: "Updated description"
  }) {
    id
    title
    price
    description
    updatedAt
  }
}

# Quick price update
mutation {
  updatePropertyPrice(id: "property-uuid-here", price: 1100000.0) {
    id
    title
    price
    updatedAt
  }
}

# Delete property
mutation {
  deleteProperty(id: "property-uuid-here")
}
```

#### Subscriptions
```graphql
# Subscribe to new properties
subscription {
  newProperty {
    id
    title
    price
    location
    propertyType
    createdAt
  }
}
```

### Listing Service REST Endpoints

```
POST   /api/properties              # Create property
GET    /api/properties              # List all properties
GET    /api/properties/:id          # Get specific property
PUT    /api/properties/:id          # Update property
DELETE /api/properties/:id          # Delete property
GET    /                           # Health check
```

### Notification Service REST Endpoints

```
GET    /api/preferences/:userId     # Get user preferences
POST   /api/notifications/property  # Send property notification
GET    /                           # Health check
```

## 🔄 Kafka Event Flow

### Event Types

The system uses three main event types for property lifecycle management:

#### 1. PropertyCreatedEvent
```scala
case class PropertyCreatedEvent(
  propertyId: UUID,
  brokerId: UUID,
  title: String,
  description: String,
  propertyType: String,
  price: Double,
  location: String,
  area: Double,
  timestamp: Instant = Instant.now()
)
```

#### 2. PropertyUpdatedEvent
```scala
case class PropertyUpdatedEvent(
  propertyId: UUID,
  brokerId: UUID,
  title: String,
  description: String,
  propertyType: String,
  price: Double,
  location: String,
  area: Double,
  timestamp: Instant = Instant.now()
)
```

#### 3. PropertyDeletedEvent
```scala
case class PropertyDeletedEvent(
  propertyId: UUID,
  brokerId: UUID,
  title: String,
  location: String,
  timestamp: Instant = Instant.now()
)
```

### Event Flow Diagram

```
┌─────────────────┐
│ Listing Service │
│                 │
│ Property CRUD   │
│ Operations      │
└─────────┬───────┘
          │
          │ Publishes Events
          ▼
┌─────────────────┐
│   Kafka Topic   │
│ "property-events"│
└─────────┬───────┘
          │
          │ Consumes Events
          ▼
┌─────────────────┐
│Notification Svc │
│                 │
│ Event Processing│
│ & Notifications │
└─────────────────┘
```

### Kafka Configuration

**Producer Settings (Listing Service):**
- Bootstrap Servers: `kafka:9092`
- Topic: `property-events`
- Serialization: String (JSON)
- Acknowledgment: `all`
- Retries: `3`

**Consumer Settings (Notification Service):**
- Bootstrap Servers: `kafka:9092`
- Topic: `property-events`
- Group ID: `notification-service-group`
- Auto Offset Reset: `earliest`
- Deserialization: String (JSON)

### Event Processing Logic

1. **Property Creation:**
   - Listing Service creates property in database
   - Publishes `PropertyCreatedEvent` to Kafka
   - Notification Service processes event and sends notifications

2. **Property Update:**
   - Listing Service updates property in database
   - Publishes `PropertyUpdatedEvent` to Kafka
   - Notification Service detects changes and processes notifications

3. **Property Deletion:**
   - Listing Service deletes property from database
   - Publishes `PropertyDeletedEvent` to Kafka
   - Notification Service processes deletion and updates user preferences

## 🗄️ Database Schema

### Properties Table
```sql
CREATE TABLE properties (
  id UUID PRIMARY KEY,
  broker_id UUID NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  property_type VARCHAR(50) NOT NULL,
  price DECIMAL(15,2) NOT NULL,
  location VARCHAR(255) NOT NULL,
  area DECIMAL(10,2),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
```

## 🧪 Testing

### Using cURL Commands

The project includes comprehensive cURL commands for testing all endpoints. See `CURL-COMMANDS.md` for detailed examples.

#### Quick Test Sequence:

1. **Health Check:**
```bash
curl -X GET http://localhost:9000/health
```

2. **Create Property:**
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { createProperty(input: { title: \"Test Property\", price: 550000.0, location: \"Test City\", propertyType: \"Apartment\" }) { id title price } }"
  }'
```

3. **List Properties:**
```bash
curl -X POST http://localhost:9000/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "query { listProperties { id title price location } }"}'
```

### Using GraphQL Playground

Visit `http://localhost:9000/api/graphql` for an interactive GraphQL interface where you can:
- Explore the schema
- Test queries and mutations
- View real-time subscriptions
- Debug GraphQL operations

## 🔧 Configuration

### Environment Variables

#### GraphQL Service
- `LISTING_SERVICE_URL`: URL of the listing service (default: `http://listing-service:9001`)
- `NOTIFICATION_SERVICE_URL`: URL of the notification service (default: `http://notification-service:9002`)

#### Listing Service
- `POSTGRES_HOST`: PostgreSQL host (default: `postgres`)
- `POSTGRES_PORT`: PostgreSQL port (default: `5432`)
- `POSTGRES_DB`: Database name (default: `propertydb`)
- `POSTGRES_USER`: Database user (default: `postgres`)
- `POSTGRES_PASSWORD`: Database password (default: `postgres`)

#### Notification Service
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers (default: `kafka:9092`)
- `KAFKA_CONSUMER_GROUP_ID`: Consumer group ID (default: `notification-service-group`)
- `KAFKA_CONSUMER_TOPIC`: Kafka topic (default: `property-events`)

### Docker Compose Services

The `docker-compose.yml` file defines:
- **PostgreSQL**: Database service
- **Zookeeper**: Kafka coordination
- **Kafka**: Message streaming
- **Listing Service**: Property management
- **Notification Service**: Event processing
- **GraphQL Service**: API gateway

## 📊 Monitoring and Logging

### Health Checks
Each service provides health check endpoints:
- GraphQL Service: `GET /health`
- Listing Service: `GET /`
- Notification Service: `GET /`

### Logging
- Application logs are stored in `logs/` directory for each service
- Logback configuration in `conf/logback.xml`
- Structured logging with timestamps and service identification

## 🚀 Deployment

### Production Considerations

1. **Database:**
   - Use managed PostgreSQL service
   - Configure connection pooling
   - Set up database backups

2. **Kafka:**
   - Use managed Kafka service
   - Configure replication factor
   - Set up monitoring and alerting

3. **Services:**
   - Use container orchestration (Kubernetes)
   - Configure health checks and readiness probes
   - Set up horizontal pod autoscaling
   - Implement circuit breakers

4. **Security:**
   - Enable HTTPS/TLS
   - Implement authentication and authorization
   - Use secrets management
   - Configure network policies

## 🔍 Troubleshooting

### Common Issues

1. **Services not starting:**
   - Check Docker containers: `docker-compose ps`
   - View logs: `docker-compose logs [service-name]`
   - Verify port availability

2. **Database connection issues:**
   - Check PostgreSQL container status
   - Verify connection parameters
   - Check network connectivity

3. **Kafka connection issues:**
   - Verify Kafka and Zookeeper containers
   - Check topic creation
   - Verify consumer group configuration

4. **GraphQL errors:**
   - Check GraphQL syntax
   - Verify required fields
   - Check service connectivity

### Debug Commands

```bash
# Check all container status
docker-compose ps

# View logs for specific service
docker-compose logs graphql-service
docker-compose logs listing-service
docker-compose logs notification-service

# Restart specific service
docker-compose restart graphql-service

# Rebuild and restart
docker-compose up --build -d
```

## 📚 Additional Resources

- **GraphQL Documentation**: [GraphQL.org](https://graphql.org/)
- **Play Framework**: [Play Framework Docs](https://www.playframework.com/)
- **Apache Kafka**: [Kafka Documentation](https://kafka.apache.org/documentation/)
- **Scala**: [Scala Documentation](https://docs.scala-lang.org/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Built with ❤️ using Scala, Play Framework, GraphQL, and Apache Kafka**
