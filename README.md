# Test Automation Framework

A Spring Boot-based test automation framework that uses Playwright for browser automation and MongoDB for data storage.

## Features

- Test case management with MongoDB
- Browser automation with Playwright
- Real-time test status updates via WebSocket
- RESTful API for test management
- Screenshot capture and storage
- Detailed test reporting
- Advanced browser configuration options
- Test context for variables and artifacts
- Step-level options (force, delay, retries, etc.)
- Asynchronous test execution with callbacks
- Conditional test execution (continue on failure)

## Requirements

- JDK 11 or higher
- MongoDB
- Gradle

## Configuration

The application can be configured using environment variables or by modifying the `application.properties` file.

### Environment Variables

```
# MongoDB Database Configuration
MONGODB_URL=mongodb://admin:admin@localhost:27017/test_automation?authSource=admin
MONGODB_NAME=test_automation
MONGODB_SYNC=true
MONGODB_LOGGING=false
MONGODB_AUTH_SOURCE=admin

# Agent Pool Configuration
MIN_AGENTS=5
MAX_AGENTS=10
AGENT_IDLE_TIMEOUT=300000

# Reporting Configuration
REPORTS_DIR=/path/to/reports
SCREENSHOTS_DIR=/path/to/screenshots
```

## Building the Project

```bash
./gradlew build
```

## Running the Application

```bash
./gradlew bootRun
```

Or run the JAR file directly:

```bash
java -jar build/libs/test-automation-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Test Management

- `GET /api/tests` - Get all tests
- `GET /api/tests/{id}` - Get a test by ID
- `POST /api/tests` - Create a new test
- `PUT /api/tests/{id}` - Update a test
- `DELETE /api/tests/{id}` - Delete a test
- `POST /api/tests/{id}/run` - Run a test by ID
- `POST /api/tests/run` - Run a test with advanced options
- `POST /api/tests/run-batch` - Run multiple tests
- `POST /api/tests/{id}/cancel` - Cancel a running test
- `GET /api/tests/{id}/results` - Get test results

### Agent Management

- `POST /api/agents/register` - Register a new agent
- `POST /api/agents/{agentId}/heartbeat` - Send agent heartbeat
- `POST /api/agents/{agentId}/deregister` - Deregister an agent
- `GET /api/agents/{agentId}/tests` - Get tests assigned to an agent
- `GET /api/agents/{agentId}/results` - Get test results for an agent

### Reporting

- `GET /api/reports/test-summary` - Get test summary
- `GET /api/reports/result-summary` - Get test result summary
- `GET /api/reports/date-range` - Get test results by date range

## WebSocket Endpoints

- `/ws` - WebSocket connection endpoint
- `/topic/tests/{testId}/status` - Test status updates
- `/topic/tests/{testId}/result` - Test result updates
- `/topic/tests/{testId}/logs` - Test log updates
- `/topic/agents/{agentId}/status` - Agent status updates

## Project Structure

```
com.testautomation
├── config
│   ├── MongoConfig.java
│   ├── WebSocketConfig.java
│   └── AppConfig.java
├── model
│   ├── enums
│   │   ├── TestStatus.java
│   │   ├── TestPriority.java
│   │   ├── TestCategory.java
│   │   ├── TestActionType.java
│   │   ├── BrowserType.java
│   │   └── SelectorStrategy.java
│   ├── Test.java
│   ├── TestStep.java
│   ├── TestResult.java
│   ├── TestStepResult.java
│   ├── TestContext.java
│   ├── BrowserOptions.java
│   ├── TestStepOptions.java
│   ├── TestAction.java
│   ├── TestRequest.java
│   └── LogEntry.java
├── repository
│   ├── TestRepository.java
│   ├── TestResultRepository.java
│   └── LogRepository.java
├── service
│   ├── core
│   │   ├── TestService.java
│   │   ├── AgentService.java
│   │   └── ReportService.java
│   ├── runners
│   │   ├── TestRunner.java
│   │   ├── TestStepExecutor.java
│   │   ├── StepExecutor.java
│   │   ├── ElementUtils.java
│   │   └── ScreenshotUtils.java
│   └── websocket
│       └── WebSocketService.java
├── controller
│   ├── TestController.java
│   ├── AgentController.java
│   └── ReportController.java
├── websocket
│   └── TestStatusWebSocketHandler.java
└── TestAutomationApplication.java
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
