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
- Automatic agent scaling based on test queue
- Multi-environment configuration (dev, test, prod)
- Comprehensive logging and monitoring

## Requirements

- JDK 11 or higher
- MongoDB 4.4+
- Gradle 7.0+
- Docker (optional, for containerized deployment)

## Configuration

The application can be configured using environment variables, profiles, or by modifying the `application.properties` file.

### Environment Variables

```
# Application Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev  # Options: dev, test, prod

# MongoDB Database Configuration
MONGODB_URI=mongodb://admin:admin@localhost:27017/automation_framework?authSource=admin
MONGODB_DATABASE=automation_framework

# Logging Configuration
ROOT_LOG_LEVEL=INFO
APP_LOG_LEVEL=DEBUG
LOG_FILE=logs/application.log

# Agent Pool Configuration
MIN_AGENTS=3
MAX_AGENTS=10
AGENT_IDLE_TIMEOUT=300000

# Auto Scaler Configuration
AUTOSCALER_ENABLED=true
AUTOSCALER_CHECK_INTERVAL=10000
SCALE_UP_THRESHOLD=2
SCALE_DOWN_THRESHOLD=0

# Reporting Configuration
REPORTS_DIR=/path/to/reports
SCREENSHOTS_DIR=/path/to/screenshots

# Task Execution Configuration
TASK_CORE_POOL_SIZE=5
TASK_MAX_POOL_SIZE=10
TASK_QUEUE_CAPACITY=25

# Test Queue Configuration
QUEUE_MAX_SIZE=100
QUEUE_TIMEOUT=1800000
```

### Configuration Profiles

The application supports multiple configuration profiles:

- **dev**: Development environment with detailed logging and minimal agents
- **test**: Testing environment with moderate logging and agents
- **prod**: Production environment with minimal logging and optimized performance

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
│   ├── MongoConfig.java                  # MongoDB yapılandırması
│   ├── MongoConnectionPoolConfig.java    # MongoDB bağlantı havuzu yapılandırması
│   ├── WebSocketConfig.java              # WebSocket yapılandırması
│   ├── AppConfig.java                    # Uygulama yapılandırması
│   └── OpenApiConfig.java                # API dokümantasyon yapılandırması
├── model
│   ├── enums
│   │   ├── TestStatus.java               # Test durumları (QUEUED, RUNNING, COMPLETED, FAILED, CANCELLED)
│   │   ├── TestPriority.java             # Test öncelikleri (HIGH, MEDIUM, LOW)
│   │   ├── TestCategory.java             # Test kategorileri (FUNCTIONAL, REGRESSION, SMOKE, PERFORMANCE)
│   │   ├── TestActionType.java           # Test eylem türleri (CLICK, TYPE, NAVIGATE, WAIT, VERIFY, etc.)
│   │   ├── BrowserType.java              # Tarayıcı türleri (CHROMIUM, FIREFOX, WEBKIT)
│   │   ├── AgentStatus.java              # Agent durumları (IDLE, BUSY, OFFLINE)
│   │   └── SelectorStrategy.java         # Seçici stratejileri (CSS, XPATH, ID, TEXT, etc.)
│   ├── Test.java                         # Test modeli
│   ├── TestStep.java                     # Test adımı modeli
│   ├── TestResult.java                   # Test sonucu modeli
│   ├── TestStepResult.java               # Test adımı sonucu modeli
│   ├── TestContext.java                  # Test bağlamı modeli
│   ├── BrowserOptions.java               # Tarayıcı seçenekleri modeli
│   ├── TestStepOptions.java              # Test adımı seçenekleri modeli
│   ├── TestAction.java                   # Test eylemi modeli
│   ├── TestRequest.java                  # Test isteği modeli
│   ├── Agent.java                        # Agent modeli
│   └── LogEntry.java                     # Log girişi modeli
├── repository
│   ├── TestRepository.java               # Test veritabanı işlemleri
│   ├── TestResultRepository.java         # Test sonucu veritabanı işlemleri
│   ├── AgentRepository.java              # Agent veritabanı işlemleri
│   └── LogRepository.java                # Log veritabanı işlemleri
├── service
│   ├── core
│   │   ├── TestService.java              # Test yönetimi servisi
│   │   ├── AgentService.java             # Agent yönetimi servisi
│   │   ├── AgentPoolService.java         # Agent havuzu yönetimi servisi
│   │   ├── TestQueueService.java         # Test kuyruğu yönetimi servisi
│   │   ├── AutoScalerService.java        # Otomatik ölçeklendirme servisi
│   │   └── ReportService.java            # Raporlama servisi
│   ├── runners
│   │   ├── TestRunner.java               # Test çalıştırıcı
│   │   ├── TestStepExecutor.java         # Test adımı yürütücü
│   │   ├── StepExecutor.java             # Adım yürütücü
│   │   ├── ElementUtils.java             # Element yardımcı sınıfı
│   │   └── ScreenshotUtils.java          # Ekran görüntüsü yardımcı sınıfı
│   └── websocket
│       └── WebSocketService.java         # WebSocket servisi
├── controller
│   ├── TestController.java               # Test API kontrolcüsü
│   ├── AgentController.java              # Agent API kontrolcüsü
│   └── ReportController.java             # Rapor API kontrolcüsü
├── websocket
│   └── TestStatusWebSocketHandler.java   # Test durumu WebSocket işleyicisi
└── TestAutomationApplication.java        # Ana uygulama sınıfı
```

## Kullanım Örnekleri

### Test Çalıştırma

```bash
# Temel kullanım
./run-test.sh

# Farklı test dosyası kullanma
./run-test.sh -f src/main/resources/login-test.json

# Test sonuçlarını bekleme
./run-test.sh -w

# Ayrıntılı çıktı ile sonuçları bekleme
./run-test.sh -w -v

# Özel zaman aşımı ile sonuçları bekleme
./run-test.sh -w -t 120
```

### Profil Seçimi ile Uygulama Çalıştırma

```bash
# Geliştirme profili ile çalıştırma
java -jar -Dspring.profiles.active=dev build/libs/test-automation-0.0.1-SNAPSHOT.jar

# Test profili ile çalıştırma
java -jar -Dspring.profiles.active=test build/libs/test-automation-0.0.1-SNAPSHOT.jar

# Üretim profili ile çalıştırma
java -jar -Dspring.profiles.active=prod build/libs/test-automation-0.0.1-SNAPSHOT.jar
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
