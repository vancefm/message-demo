# Message Demo

A Spring Boot microservices project demonstrating **message publishing, consuming, routing keys, and the outbox pattern** with RabbitMQ.

## Project Structure

```
message-demo/
├── message-api/          # REST API & Publisher service (port 8080)
│   ├── controller/       # REST endpoints (ShapeController, HelloController)
│   ├── service/          # MessagePublisher, OutboxPoller
│   ├── model/            # Entity classes (Square, Circle, Pentagon, OutboxEvent)
│   ├── repository/       # OutboxRepository for database persistence
│   ├── config/           # RabbitMQConfig, JacksonConfig
│   └── resources/        # H2 database configuration
├── message-consumer/     # Message consumer service (port 8081)
│   ├── service/          # MessageConsumerListener
│   ├── config/           # ConsumerRabbitMQConfig
│   └── resources/        # Application properties
├── pom.xml               # Parent POM (multi-module)
├── docker-compose.yml    # RabbitMQ setup
└── .vscode/              # VS Code debugging configuration
```

## Components

### message-api
**Purpose:** REST API that publishes messages to RabbitMQ using the **outbox pattern** for reliability.

**Endpoints:**
- `GET /square` — Creates and publishes a Square object (routing key: `shape.square`)
- `GET /circle` — Creates and publishes a Circle object (routing key: `shape.circle`)
- `GET /pentagon` — Creates and publishes a Pentagon object (routing keys: `shape.pentagon.1` and `shape.pentagon.2`)

**Key Classes:**
- `MessageApiApplication` — Spring Boot entry point with `@SpringBootApplication`
- `ShapeController` / `HelloController` — REST endpoints
- `MessagePublisher` — Service that writes messages to the outbox (database)
- `OutboxPoller` — Scheduled service that publishes from outbox to RabbitMQ (runs every 1 second)
- `OutboxRepository` — JPA repository for persisting unpublished events
- `OutboxEvent` — JPA entity representing a message awaiting publication
- `RabbitMQConfig` — Defines exchange, queues, bindings, and routing keys

**Port:** 8080
**Database:** H2 in-memory (persists outbox events)

### message-consumer
**Purpose:** Consumes messages from RabbitMQ and logs them.

**Key Classes:**
- `MessageConsumerApplication` — Spring Boot entry point with `@EnableRabbit`
- `MessageConsumerListener` — Service that listens to all shape queues (square, circle, pentagon)
- `ConsumerRabbitMQConfig` — Defines exchange, queues, bindings, and routing keys (mirrors API config)

**Port:** 8081

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker (recommended for RabbitMQ) or native RabbitMQ installation

### 1. Start RabbitMQ (Docker)

```powershell
docker-compose up -d
```

Verify RabbitMQ is running:
```powershell
netstat -aon | findstr :5672
```

Access the management UI at http://localhost:15672 (default credentials: `guest`/`guest`).

### 2. Build the Project

```powershell
cd <path\to\project>\message-demo
mvn -DskipTests=true clean install
```

### 3. Start the Services

**Option A: From VS Code (F5)**
- Open the workspace folder in VS Code.
- Open Run and Debug (Ctrl+Shift+D).
- Select "All Services" from the dropdown and press F5.
- Or select individual services: "message-api" or "message-consumer".

**Option B: From Command Line**

Terminal 1 - Start API:
```powershell
cd message-api
mvnw.cmd spring-boot:run
```

Terminal 2 - Start Consumer:
```powershell
cd message-consumer
mvnw.cmd spring-boot:run
```

### 4. Test the Flow

**Test 2: Circle Shape**
```powershell
Invoke-WebRequest -Uri 'http://localhost:8080/circle' -UseBasicParsing
```
Expected:
- API returns: `"Circle message sent with routing key 'shape.circle': ..."`
- Consumer logs: `"Received Circle from circle.queue (routing key: shape.circle): ..."`

**Test 3: Pentagon Shape (Multiple Routing Keys)**
```powershell
Invoke-WebRequest -Uri 'http://localhost:8080/pentagon' -UseBasicParsing
```
Expected:
- API returns: `"Pentagon message sent with routing keys 'shape.pentagon.1' and 'shape.pentagon.2': ..."`
- Consumer logs **two messages**: 
  - `"Received Pentagon from pentagon.queue (routing keys: shape.pentagon.1 or shape.pentagon.2): ..."`
  - `"Received Pentagon from pentagon.queue (routing keys: shape.pentagon.1 or shape.pentagon.2): ..."`

**Behind the Scenes (Outbox Pattern):**
1. Each call writes OutboxEvent(s) to H2 database
2. OutboxPoller queries unpublished events every 1 second
3. OutboxPoller publishes to RabbitMQ with the appropriate routing key
4. RabbitMQ routes to the correct queue based on the routing key
5. MessageConsumerListener consumes from the queue
6. OutboxEvent is marked as published

### 5. Monitor & Debug

**RabbitMQ Management UI:**
Visit http://localhost:15672 (credentials: `guest`/`guest`):
- Go to **Queues** → Select a queue (hello.queue, square.queue, circle.queue, pentagon.queue)
- View messages in the queue
- Observe messages being consumed in real-time

**Outbox Events in H2:**
H2 console at http://localhost:8080/h2-console (after api starts):
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** (blank)
- Run: `SELECT * FROM OUTBOX_EVENTS;` to see all published events
- Filter unpublished: `SELECT * FROM OUTBOX_EVENTS WHERE PUBLISHED = FALSE;`

**Service Logs:**
- **API logs:** Look for `Writing <Shape> to outbox` and `Published outbox event`
- **Consumer logs:** Look for `Received <Shape> from <queue>`
- **Poller logs:** Look for `Found X unpublished outbox events` and `Successfully published outbox event`

## Message Flow & Architecture

### Outbox Pattern

The **outbox pattern** decouples message publishing from the message broker, ensuring reliability:

1. **Write to Database:** When a shape is published, the `MessagePublisher` writes an `OutboxEvent` to the H2 database (not directly to RabbitMQ).
2. **Persist in Same Transaction:** The outbox event is created with:
   - `aggregateId` — UUID of the event
   - `eventType` — Type of shape (Square, Circle, Pentagon)
   - `payload` — JSON serialization of the shape
   - `routingKey` — Key for routing to the correct queue
   - `published` — Flag indicating if sent to broker (initially `false`)

3. **Async Polling:** The `OutboxPoller` runs every 1 second and:
   - Queries for unpublished events: `SELECT * FROM outbox_events WHERE published = false`
   - Publishes each event to RabbitMQ
   - Marks as published: `UPDATE outbox_events SET published = true, publishedAt = NOW()`

4. **Resilience:** If RabbitMQ goes down:
   - Already-published messages are marked `published = true` (won't retry)
   - Unpublished messages remain in the database and retry on next poll
   - No message loss

**Benefits:**
- ✅ Exactly-once semantics (no duplicates, no loss)
- ✅ Decoupled from broker availability
- ✅ Auditable: all events persisted to database
- ✅ Retryable: failed publishes automatically retry

### Routing Keys

**RabbitMQ Routing Keys** direct messages from an exchange to specific queues based on pattern matching.

**Direct Exchange Routing:**
The project uses a `DirectExchange` where the routing key must **exactly match** the queue binding key.

**Routing Configuration:**

| Shape | Routing Key | Queue | Notes |
|-------|-------------|-------|-------|
| Square | `shape.square` | `square.queue` | Single routing key |
| Circle | `shape.circle` | `circle.queue` | Single routing key |
| Pentagon | `shape.pentagon.1` | `pentagon.queue` | Dual routing keys |
| Pentagon | `shape.pentagon.2` | `pentagon.queue` | (same queue, different key) |

**Example - Pentagon with Multiple Routing Keys:**
```
Pentagon object created
    ↓
MessagePublisher.publishPentagon(pentagon)
    ↓
Creates 2 OutboxEvents (same payload, different routing keys):
  - OutboxEvent 1: routingKey = "shape.pentagon.1"
  - OutboxEvent 2: routingKey = "shape.pentagon.2"
    ↓
OutboxPoller reads both unpublished events
    ↓
Publishes to RabbitMQ with respective routing keys:
  - rabbitTemplate.convertAndSend(EXCHANGE_NAME, "shape.pentagon.1", payload)
  - rabbitTemplate.convertAndSend(EXCHANGE_NAME, "shape.pentagon.2", payload)
    ↓
Both messages route to pentagon.queue (both bindings match)
    ↓
MessageConsumerListener.receivePentagon() processes each message
```

**Config Definition (RabbitMQConfig.java):**
```java
// Bindings determine which routing keys route to which queues
@Bean
public Binding squareBinding(@Qualifier("squareQueue") Queue squareQueue, DirectExchange shapesExchange) {
    return BindingBuilder.bind(squareQueue)
            .to(shapesExchange)
            .with(SQUARE_ROUTING_KEY);  // "shape.square"
}

// Pentagon has TWO bindings (same queue, different keys)
@Bean
public Binding pentagonBinding1(@Qualifier("pentagonQueue") Queue pentagonQueue, DirectExchange shapesExchange) {
    return BindingBuilder.bind(pentagonQueue)
            .to(shapesExchange)
            .with(PENTAGON_ROUTING_KEY_1);  // "shape.pentagon.1"
}

@Bean
public Binding pentagonBinding2(@Qualifier("pentagonQueue") Queue pentagonQueue, DirectExchange shapesExchange) {
    return BindingBuilder.bind(pentagonQueue)
            .to(shapesExchange)
            .with(PENTAGON_ROUTING_KEY_2);  // "shape.pentagon.2"
}
```

### Complete Message Flow

```
GET /pentagon
    ↓
ShapeController.publishPentagon()
    ↓
MessagePublisher.publishPentagon(pentagon)
    ↓
2 × OutboxEvent.save() to H2 database (same payload, 2 routing keys)
    ↓
OutboxPoller.pollAndPublish() (every 1 second)
    ↓
rabbitTemplate.convertAndSend(EXCHANGE, "shape.pentagon.1", payload)
rabbitTemplate.convertAndSend(EXCHANGE, "shape.pentagon.2", payload)
    ↓
RabbitMQ "shapes.exchange" (DirectExchange)
    ↓
Routing key matching:
  "shape.pentagon.1" → pentagon.queue (via pentagonBinding1)
  "shape.pentagon.2" → pentagon.queue (via pentagonBinding2)
    ↓
pentagon.queue receives 2 messages
    ↓
@RabbitListener(queues = "pentagon.queue")
MessageConsumerListener.receivePentagon() processes each message
    ↓
Logs: "Received Pentagon from pentagon.queue (routing keys: shape.pentagon.1 or shape.pentagon.2)"
```

## Configuration

### RabbitMQ Connection
Both services connect to RabbitMQ using properties defined in `application.properties`:

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

To connect to a different RabbitMQ instance, update these properties in:
- `message-api/src/main/resources/application.properties`
- `message-consumer/src/main/resources/application.properties`

### Database Configuration (message-api)
The outbox pattern uses H2 (in-memory database) for local development:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```

**Table: `outbox_events`**
```sql
CREATE TABLE outbox_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    routing_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP
);
```

Schema is auto-created via Hibernate with `hibernate.ddl-auto=create-drop`.

### Exchange & Queue Configuration

**Exchange:** `shapes.exchange` (DirectExchange)

**Queues:**
- `square.queue` — Receives square shapes (routing key: `shape.square`)
- `circle.queue` — Receives circle shapes (routing key: `shape.circle`)
- `pentagon.queue` — Receives pentagon shapes (routing keys: `shape.pentagon.1`, `shape.pentagon.2`)

These are defined in the `RabbitMQConfig` classes in each service.

## Troubleshooting

### "Project is not a valid Java project" error in VS Code
1. Run: `mvn -DskipTests=true clean install`
2. In VS Code: Ctrl+Shift+P → "Java: Clean the Java Language Server Workspace"
3. Reload Window: Ctrl+Shift+P → "Reload Window"

### Outbox events not being published
**Issue:** Events remain in database with `published = false`

**Debugging:**
1. Verify `OutboxPoller` is running:
   - Look for log: `Found X unpublished outbox events`
   - Check that Spring scheduling is enabled (it is, via `@EnableScheduling` on `OutboxPoller`)
2. Verify RabbitMQ is accessible:
   - Check connection logs in API service startup
   - Verify `spring.rabbitmq.host` and port are correct
3. Check for exceptions in logs:
   - Search for `"Failed to publish outbox event"`
   - Verify `OutboxRepository` is correctly injected
4. Manually test publishing:
   - Query H2: `SELECT COUNT(*) FROM OUTBOX_EVENTS WHERE PUBLISHED = FALSE;`
   - Wait 5 seconds and re-query (should show 0 if poller is working)

### Duplicate messages in consumer
This should not occur with the outbox pattern. If it does:
1. Verify `published` flag is being set correctly in database
2. Check that `OutboxPoller` completes the transaction before the next poll
3. Ensure no manual publishes are bypassing the outbox

### Messages not being consumed
- Ensure both services are running and connected (check startup logs for AMQP connection)
- Verify queue exists: Check RabbitMQ UI at http://localhost:15672
- Verify routing key matches binding:
  - API publishes with routing key `X`
  - Consumer's `@RabbitListener` listens on the queue bound to routing key `X`
  - RabbitMQ routes message from exchange → queue only if routing key matches
- Check consumer listener method is receiving: Look for log messages in consumer

### RabbitMQ connection refused
- Start RabbitMQ: `docker-compose up -d`
- Verify port 5672 is open: `netstr -aon | findstr :5672`
- Check Docker is running: `docker ps | findstr rabbitmq`

### H2 Database Issues
- H2 is in-memory and resets on service restart (data is lost)
- To persist data across restarts, change connection URL:
  ```properties
  spring.datasource.url=jdbc:h2:file:./data/testdb
  ```
- Access H2 console: http://localhost:8080/h2-console

## Stopping Services

### Stop RabbitMQ (Docker)
```powershell
docker-compose down
```

### Stop Services
- Press Ctrl+C in the terminal running each service, or
- In VS Code, click the stop button in the Debug panel.

## Project Details

- **Parent POM:** Manages Java version (21), Spring Boot version (4.0.0), and shared dependencies.
- **Multi-Module Build:** Both `message-api` and `message-consumer` are built together with `mvn clean install`.
- **Spring Boot Version:** 4.0.0
- **Spring AMQP:** For RabbitMQ integration

## Implementation Details

### Outbox Pattern Classes

**OutboxEvent Entity (Model)**
```java
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String aggregateId;      // UUID of the event
    private String eventType;        // "Square", "Circle", "Pentagon"
    private String payload;          // JSON serialized shape
    private String routingKey;       // "shape.square", etc.
    private LocalDateTime createdAt; // When event was created
    private boolean published;       // false = not yet sent to broker
    private LocalDateTime publishedAt; // When event was sent to broker
}
```

**OutboxRepository (Persistence)**
```java
@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    // Query unpublished events, ordered by creation date
    List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc();
}
```

**MessagePublisher Service (Writing to Outbox)**
```java
@Service
public class MessagePublisher {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void publishSquare(Square square) {
        String payload = objectMapper.writeValueAsString(square);
        OutboxEvent event = new OutboxEvent(
            UUID.randomUUID().toString(),
            "Square",
            payload,
            RabbitMQConfig.SQUARE_ROUTING_KEY
        );
        outboxRepository.save(event); // Persists to database
    }
    
    // Similar methods for Circle, Pentagon (Pentagon creates 2 events)
}
```

**OutboxPoller Service (Publishing from Outbox)**
```java
@Service
@EnableScheduling
public class OutboxPoller {
    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 1000) // Runs every 1 second
    @Transactional
    public void pollAndPublish() {
        // 1. Query unpublished events
        List<OutboxEvent> events = outboxRepository.findByPublishedFalseOrderByCreatedAtAsc();
        
        // 2. For each event, publish to RabbitMQ
        for (OutboxEvent event : events) {
            try {
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    event.getRoutingKey(),
                    event.getPayload()
                );
                
                // 3. Mark as published in same transaction
                event.setPublished(true);
                event.setPublishedAt(LocalDateTime.now());
                outboxRepository.save(event);
            } catch (Exception ex) {
                // Event remains unpublished, will retry on next poll
                logger.error("Failed to publish outbox event: {}", ex.getMessage());
            }
        }
    }
}
```

### Routing Keys Implementation

**Bindings with @Qualifier**
```java
@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "shapes.exchange";
    public static final String PENTAGON_ROUTING_KEY_1 = "shape.pentagon.1";
    public static final String PENTAGON_ROUTING_KEY_2 = "shape.pentagon.2";
    
    @Bean
    public DirectExchange shapesExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue pentagonQueue() {
        return new Queue("pentagon.queue", true);
    }

    // Two bindings, same queue, different routing keys
    @Bean
    public Binding pentagonBinding1(
            @Qualifier("pentagonQueue") Queue pentagonQueue,
            DirectExchange shapesExchange) {
        return BindingBuilder.bind(pentagonQueue)
                .to(shapesExchange)
                .with(PENTAGON_ROUTING_KEY_1);
    }

    @Bean
    public Binding pentagonBinding2(
            @Qualifier("pentagonQueue") Queue pentagonQueue,
            DirectExchange shapesExchange) {
        return BindingBuilder.bind(pentagonQueue)
                .to(shapesExchange)
                .with(PENTAGON_ROUTING_KEY_2);
    }
}
```

**Note:** `@Qualifier` is used because we have multiple `Queue` beans (squareQueue, circleQueue, pentagonQueue). Without it, Spring wouldn't know which queue to inject into each binding method.

### Consumer Listeners

```java
@Service
public class MessageConsumerListener {
    
    @RabbitListener(queues = "square.queue")
    public void receiveSquare(String message) {
        logger.info("Received Square from square.queue: {}", message);
    }

    @RabbitListener(queues = "pentagon.queue")
    public void receivePentagon(String message) {
        // Called twice per pentagon (one for each routing key)
        logger.info("Received Pentagon: {}", message);
    }
}
```

Each `@RabbitListener` automatically:
- Connects to its queue
- Waits for messages
- Calls the method when a message arrives
- Automatically acknowledges (removes) the message from the queue

## Development & Extension

### Adding a New Shape Type

**Step 1: Create a Model Class**
```java
package com.local.message_api.model;

public class Triangle {
    private String type = "Triangle";
    private LocalDateTime timestamp = LocalDateTime.now();
    
    public String getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "Triangle{" + "type='" + type + "', timestamp=" + timestamp + "}";
    }
}
```

**Step 2: Add Routing Key and Queue Constants**
In `RabbitMQConfig`:
```java
public static final String TRIANGLE_ROUTING_KEY = "shape.triangle";
public static final String TRIANGLE_QUEUE_NAME = "triangle.queue";
```

**Step 3: Create Queue and Binding Beans**
```java
@Bean
public Queue triangleQueue() {
    return new Queue(TRIANGLE_QUEUE_NAME, true);
}

@Bean
public Binding triangleBinding(@Qualifier("triangleQueue") Queue triangleQueue, DirectExchange shapesExchange) {
    return BindingBuilder.bind(triangleQueue)
            .to(shapesExchange)
            .with(TRIANGLE_ROUTING_KEY);
}
```

**Step 4: Add Publisher Method**
```java
public void publishTriangle(Triangle triangle) {
    logger.info("Writing Triangle to outbox");
    try {
        String payload = objectMapper.writeValueAsString(triangle);
        OutboxEvent event = new OutboxEvent(
            UUID.randomUUID().toString(),
            "Triangle",
            payload,
            RabbitMQConfig.TRIANGLE_ROUTING_KEY
        );
        outboxRepository.save(event);
    } catch (Exception ex) {
        logger.error("Failed to save Triangle to outbox: {}", ex.getMessage());
    }
}
```

**Step 5: Add REST Endpoint**
```java
@GetMapping("/triangle")
public String publishTriangle() {
    logger.info("Publishing Triangle");
    Triangle triangle = new Triangle();
    messagePublisher.publishTriangle(triangle);
    return "Triangle message sent with routing key 'shape.triangle': " + triangle;
}
```

**Step 6: Add Consumer Listener**
In `MessageConsumerListener`:
```java
@RabbitListener(queues = "triangle.queue")
public void receiveTriangle(String message) {
    logger.info("Received Triangle from triangle.queue (routing key: shape.triangle): {}", message);
}
```

**Step 7: Update Consumer Config**
In `ConsumerRabbitMQConfig`, add the same queue and binding (mirrors API config).

### Scaling the Outbox Poller

By default, `OutboxPoller` runs every 1 second. To adjust:

```java
@Scheduled(fixedDelay = 500)  // Poll every 500ms (more responsive)
public void pollAndPublish() { ... }

@Scheduled(fixedRate = 2000)  // Poll every 2 seconds (less resource usage)
public void pollAndPublish() { ... }

@Scheduled(cron = "0 * * * * *")  // Poll every minute
public void pollAndPublish() { ... }
```

### Batch Publishing from Outbox

Optimize for high throughput by publishing in batches:

```java
@Scheduled(fixedDelay = 1000)
@Transactional
public void pollAndPublish() {
    List<OutboxEvent> events = outboxRepository.findByPublishedFalseOrderByCreatedAtAsc(
        PageRequest.of(0, 100)  // Fetch max 100 at a time
    );
    
    // Publish all in batch
    for (OutboxEvent event : events) {
        rabbitTemplate.convertAndSend(...);
        event.setPublished(true);
    }
    outboxRepository.saveAll(events);  // Batch save
}
```

## License

This project is for educational purposes.
