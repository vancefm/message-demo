# Message Demo

A Spring Boot microservices project demonstrating message publishing and consuming with RabbitMQ.

## Project Structure

```
message-demo/
├── message-api/          # REST API service (port 8080)
│   ├── controller/       # REST endpoints
│   ├── service/          # Message publishing service
│   └── config/           # RabbitMQ configuration
├── message-consumer/     # Message consumer service (port 8081)
│   ├── service/          # Message listening service
│   ├── config/           # RabbitMQ configuration
│   └── resources/        # Application properties
├── pom.xml               # Parent POM (multi-module)
├── docker-compose.yml    # RabbitMQ setup
└── .vscode/              # VS Code debugging configuration
```

## Components

### message-api
**Purpose:** REST API that publishes messages to RabbitMQ.

**Endpoint:**
- `GET /hello` — Logs "Hello World!" and publishes a message to the `message.exchange` with routing key `hello`.

**Key Classes:**
- `MessageApiApplication` — Spring Boot entry point
- `HelloController` — REST controller with `/hello` endpoint
- `MessagePublisher` — Service that publishes messages to RabbitMQ
- `RabbitMQConfig` — Defines exchange, queue, and binding

**Port:** 8080

### message-consumer
**Purpose:** Consumes messages from RabbitMQ and logs them.

**Key Classes:**
- `MessageConsumerApplication` — Spring Boot entry point with `@EnableRabbit`
- `MessageConsumerListener` — Service that listens to `hello.queue`
- `ConsumerRabbitMQConfig` — Defines exchange, queue, and binding (same as API)

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

Call the API endpoint:
```powershell
Invoke-WebRequest -Uri 'http://localhost:8080/hello' -UseBasicParsing
```

**Expected behavior:**
- API returns: `"Message sent: Hello World! at <timestamp>"`
- Consumer logs: `"Consumer received message: Hello World! at <timestamp>"`

### 5. Monitor in RabbitMQ UI

Visit http://localhost:15672:
- Go to **Queues** → **hello.queue**
- You should see messages in the queue (or consumed if the consumer is running).

## Message Flow

```
GET /hello
    ↓
MessagePublisher.publishHelloMessage()
    ↓
RabbitMQ Exchange: "message.exchange"
    ↓
Routing Key: "hello"
    ↓
Queue: "hello.queue"
    ↓
MessageConsumerListener.receive()
    ↓
Log: "Consumer received message: ..."
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

### Exchange & Queue Configuration
- **Exchange Name:** `message.exchange`
- **Queue Name:** `hello.queue`
- **Routing Key:** `hello`

These are defined in the `RabbitMQConfig` classes in each service.

## Troubleshooting

### "Project is not a valid Java project" error in VS Code
1. Run: `mvn -DskipTests=true clean install`
2. In VS Code: Ctrl+Shift+P → "Java: Clean the Java Language Server Workspace"
3. Reload Window: Ctrl+Shift+P → "Reload Window"

### Messages not being consumed
- Ensure RabbitMQ is running: `docker ps | findstr rabbitmq`
- Check that both services are running and connected (look for AMQP connection logs).
- Verify the queue exists in RabbitMQ UI (http://localhost:15672).

### RabbitMQ connection refused
- Start RabbitMQ: `docker-compose up -d`
- Verify port 5672 is open: `netstat -aon | findstr :5672`

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

## Development

### Adding a New Consumer
1. Create a new method in `MessageConsumerListener` with `@RabbitListener(queues = "queue-name")`.
2. The method will automatically receive messages from that queue.

### Adding a New Publisher
1. Inject `RabbitTemplate` in a service.
2. Call `rabbitTemplate.convertAndSend(exchange, routingKey, message)`.

### Running Tests
```powershell
mvn clean test
```

## License

This project is for educational purposes.
