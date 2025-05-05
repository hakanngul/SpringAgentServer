# Spring Boot Test Otomasyon Mimarisi

## 1. Proje Yapısı

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
│   │   └── SelectorStrategy.java
│   ├── Test.java
│   ├── TestStep.java
│   ├── TestResult.java
│   ├── TestStepResult.java
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

## 2. Gradle Bağımlılıkları (build.gradle)

```groovy
plugins {
    id 'org.springframework.boot' version '3.1.5'
    id 'io.spring.dependency-management' version '1.1.3'
    id 'java'
}

group = 'com.testautomation'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot temel bağımlılıkları
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // MongoDB entegrasyonu için
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
    
    // WebSocket desteği için
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    
    // Playwright Java
    implementation 'com.microsoft.playwright:playwright:1.36.0'
    
    // UUID oluşturmak için
    implementation 'com.fasterxml.uuid:java-uuid-generator:4.1.0'
    
    // JSON işlemleri için
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310'
    
    // Lombok - Kod kısaltma için
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Test bağımlılıkları
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.6.3'
}

test {
    useJUnitPlatform()
}
```

## 3. Temel Sınıflar ve Arayüzler

### Model Sınıfları

#### TestStatus.java
```java
package com.testautomation.model.enums;

public enum TestStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    TIMEOUT,
    CANCELLED
}
```

#### TestPriority.java
```java
package com.testautomation.model.enums;

public enum TestPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
```

#### TestCategory.java
```java
package com.testautomation.model.enums;

public enum TestCategory {
    FUNCTIONAL,
    REGRESSION,
    INTEGRATION,
    PERFORMANCE,
    SECURITY,
    SMOKE,
    ACCEPTANCE
}
```

#### TestActionType.java
```java
package com.testautomation.model.enums;

public enum TestActionType {
    NAVIGATE,
    CLICK,
    TYPE,
    WAIT,
    WAIT_FOR_NAVIGATION,
    WAIT_FOR_ELEMENT,
    PRESS_ENTER,
    SELECT,
    VERIFY_URL,
    VERIFY_TEXT,
    TAKE_SCREENSHOT,
    FILL,
    CHECK("check", "Check a checkbox"),
    UNCHECK("uncheck", "Uncheck a checkbox"),
    SELECT("select", "Select an option from a dropdown"),
    WAIT_FOR_SELECTOR("waitforselector", "Wait for an element to be visible"),
    WAIT_FOR_NAVIGATION("waitfornavigation", "Wait for navigation to complete"),
    SCREENSHOT("screenshot", "Take a screenshot"),
    EXPECT("expect", "Assert that an element contains specific text"),
    HOVER("hover", "Hover over an element"),
    PRESS("press", "Press a key on the keyboard"),
    DOUBLE_CLICK("doubleclick", "Double-click on an element"),
    RIGHT_CLICK("rightclick", "Right-click on an element"),
    DRAG_AND_DROP("draganddrop", "Drag and drop an element"),
    FOCUS("focus", "Focus on an element"),
    SCROLL_INTO_VIEW("scrollintoview", "Scroll an element into view"),
    EVALUATE("evaluate", "Evaluate JavaScript in the browser context");
}
```

#### SelectorStrategy.java
```java
package com.testautomation.model.enums;

public enum SelectorStrategy {
    ID,
    CLASS,
    NAME,
    XPATH,
    CSS
}
```

#### Test.java
```java
package com.testautomation.model;

import com.testautomation.model.enums.TestCategory;
import com.testautomation.model.enums.TestPriority;
import com.testautomation.model.enums.TestStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Document(collection = "tests")
public class Test {
    @Id
    private String id;
    private String name;
    private String description;
    private TestStatus status;
    private TestPriority priority;
    private TestCategory category;
    private List<String> tags;
    private String browserPreference;
    private boolean headless;
    private boolean takeScreenshots;
    private boolean browserFullScreen;
    private List<TestStep> steps;
    private LocalDateTime createdAt;
    private LocalDateTime queuedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String agentId;
    private Object results;
    private String error;
    private Map<String, Object> metadata;
    private String preconditions;
    private String expectedResults;
    
    // Constructor, getter/setter ve diğer metodlar...
    
    public void updateStatus(TestStatus status, Map<String, Object> data) {
        // Test durumunu güncelleme mantığı...
    }
}
```

#### TestStep.java
```java
package com.testautomation.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TestStep {
    private String action;
    private String target;
    private String strategy;
    private String value;
    private String description;
    private Map<String, Object> additionalProperties = new HashMap<>();
}
```

#### TestResult.java
```java
package com.testautomation.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "test_results")
public class TestResult {
    @Id
    private String id;
    private String testId;
    private String agentId;
    private String name;
    private String description;
    private String browserPreference;
    private boolean headless;
    private boolean takeScreenshots;
    private boolean browserFullScreen;
    private boolean success;
    private String startTime;
    private String endTime;
    private long duration;
    private List<TestStepResult> steps = new ArrayList<>();
    private List<LogEntry> logs = new ArrayList<>();
    private List<String> screenshots = new ArrayList<>();
}
```

#### TestStepResult.java
```java
package com.testautomation.model;

import lombok.Data;

@Data
public class TestStepResult {
    private int index;
    private String action;
    private String description;
    private boolean success;
    private String error;
    private String screenshot;
    private long duration;
    private String startTime;
    private String endTime;
}
```

#### LogEntry.java
```java
package com.testautomation.model;

import lombok.Data;

@Data
public class LogEntry {
    private String timestamp;
    private String level;
    private String message;
    private String agentId;
}
```

### Repository Sınıfları

#### TestRepository.java
```java
package com.testautomation.repository;

import com.testautomation.model.Test;
import com.testautomation.model.enums.TestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends MongoRepository<Test, String> {
    List<Test> findByStatus(TestStatus status);
    List<Test> findByAgentId(String agentId);
}
```

#### TestResultRepository.java
```java
package com.testautomation.repository;

import com.testautomation.model.TestResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestResultRepository extends MongoRepository<TestResult, String> {
    List<TestResult> findByTestId(String testId);
    List<TestResult> findByAgentId(String agentId);
}
```

#### LogRepository.java
```java
package com.testautomation.repository;

import com.testautomation.model.LogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends MongoRepository<LogEntry, String> {
    List<LogEntry> findByAgentId(String agentId);
}
```

### Service Sınıfları

#### TestRunner.java
```java
package com.testautomation.service.runners;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.testautomation.model.LogEntry;
import com.testautomation.model.Test;
import com.testautomation.model.TestResult;
import com.testautomation.model.enums.TestStatus;
import com.testautomation.repository.TestRepository;
import com.testautomation.repository.TestResultRepository;
import com.testautomation.service.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestRunner {
    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final WebSocketService webSocketService;
    private final String screenshotsDir;
    
    public CompletableFuture<TestResult> runTest(Test test, String agentId) {
        return CompletableFuture.supplyAsync(() -> {
            // Test çalıştırma mantığı...
            return result;
        });
    }
    
    public CompletableFuture<List<TestResult>> runTests(List<Test> tests, String agentId) {
        // Çoklu test çalıştırma mantığı...
    }
    
    // Yardımcı metodlar...
}
```

#### TestStepExecutor.java
```java
package com.testautomation.service.runners;

import com.microsoft.playwright.Page;
import com.testautomation.model.TestStep;
import com.testautomation.model.TestStepResult;
import com.testautomation.model.TestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
@RequiredArgsConstructor
public class TestStepExecutor {
    private Page page;
    private String testName;
    private String screenshotsDir;
    private boolean takeScreenshots;
    private BiConsumer<String, String> logFn;
    
    // Constructor...
    
    public boolean executeSteps(
        List<TestStep> steps,
        TestResult result,
        Map<String, Object> variables,
        Map<String, Object> dataSet
    ) throws Exception {
        // Test adımlarını çalıştırma mantığı...
    }
    
    // Yardımcı metodlar...
}
```

#### StepExecutor.java
```java
package com.testautomation.service.runners;

import com.microsoft.playwright.Page;
import com.testautomation.model.TestStep;
import com.testautomation.model.enums.TestActionType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StepExecutor {
    
    public static String replaceVariables(String value, Map<String, Object> variables, Map<String, Object> dataSet) {
        // Değişkenleri değiştirme mantığı...
    }
    
    private static TestActionType getActionType(String action) {
        // Action tipini belirleme mantığı...
    }
    
    public static void executeStep(
        Page page,
        TestStep step,
        int stepIndex,
        int totalSteps,
        Map<String, Object> variables,
        Map<String, Object> dataSet
    ) throws Exception {
        // Adım çalıştırma mantığı...
    }
    
    // Yardımcı metodlar...
}
```

#### ElementUtils.java
```java
package com.testautomation.service.runners;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.springframework.stereotype.Component;

@Component
public class ElementUtils {
    
    public static Locator findElement(Page page, String target, String strategy) {
        // Element bulma mantığı...
    }
    
    public static void findAndClick(Page page, String target, String strategy) {
        // Element bulup tıklama mantığı...
    }
    
    public static void findAndType(Page page, String target, String strategy, String value) {
        // Element bulup yazma mantığı...
    }
    
    public static void findAndSelect(Page page, String target, String strategy, String value) {
        // Element bulup seçme mantığı...
    }
    
    public static void waitForElement(Page page, String target, String strategy, int timeout) {
        // Element için bekleme mantığı...
    }
    
    public static void verifyText(Page page, String target, String strategy, String expectedText) {
        // Metin doğrulama mantığı...
    }
    
    // Diğer element işlemleri...
}
```

#### ScreenshotUtils.java
```java
package com.testautomation.service.runners;

import com.microsoft.playwright.Page;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ScreenshotUtils {
    
    public static String takeScreenshot(
        Page page,
        String testName,
        int stepIndex,
        String description,
        String screenshotsDir
    ) {
        // Ekran görüntüsü alma mantığı...
    }
    
    // Yardımcı metodlar...
}
```

#### WebSocketService.java
```java
package com.testautomation.service.websocket;

import com.testautomation.model.LogEntry;
import com.testautomation.model.Test;
import com.testautomation.model.TestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    
    public void sendTestStatus(Test test) {
        // Test durumu gönderme mantığı...
    }
    
    public void sendTestResult(TestResult result) {
        // Test sonucu gönderme mantığı...
    }
    
    public void sendTestLog(String testId, LogEntry logEntry) {
        // Test log gönderme mantığı...
    }
    
    // Diğer WebSocket metodları...
}
```

### Controller Sınıfları

#### TestController.java
```java
package com.testautomation.controller;

import com.testautomation.model.Test;
import com.testautomation.model.TestResult;
import com.testautomation.service.core.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;
    
    @PostMapping
    public ResponseEntity<Test> createTest(@RequestBody Test test) {
        // Test oluşturma mantığı...
    }
    
    @GetMapping
    public ResponseEntity<List<Test>> getAllTests() {
        // Tüm testleri getirme mantığı...
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Test> getTestById(@PathVariable String id) {
        // ID'ye göre test getirme mantığı...
    }
    
    @PostMapping("/{id}/run")
    public ResponseEntity<CompletableFuture<TestResult>> runTest(@PathVariable String id, @RequestParam String agentId) {
        // Test çalıştırma mantığı...
    }
    
    // Diğer API metodları...
}
```

### WebSocket Sınıfları

#### WebSocketConfig.java
```java
package com.testautomation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("*")
            .withSockJS();
    }
}
```

## 4. Prompt

Bir AI'ya Spring Boot ile test otomasyon yapısı oluşturmak için şu şekilde bir prompt verebilirsiniz:

```
Mevcut Node.js/TypeScript tabanlı test otomasyon yapımı Spring Boot ile yeniden oluşturmak istiyorum. Mevcut yapıda şu bileşenler bulunuyor:

1. Test modelleri (Test, TestStep, TestResult vb.)
2. Test durumları ve kategorileri için enum'lar
3. TestRunner sınıfı - testleri çalıştırır
4. TestStepExecutor sınıfı - test adımlarını çalıştırır
5. StepExecutor - adım çalıştırma mantığı
6. ElementUtils - element işlemleri
7. ScreenshotUtils - ekran görüntüsü işlemleri
8. MongoDB entegrasyonu
9. WebSocket ile test durumu bildirimi

Bu yapıyı Spring Boot ile nasıl oluşturabilirim? Şunları istiyorum:
- Gradle bağımlılıkları
- Paket yapısı
- Temel sınıfların ve arayüzlerin tanımları
- TestRunner, TestStepExecutor ve StepExecutor sınıflarının detaylı implementasyonu
- MongoDB repository sınıfları
- WebSocket entegrasyonu
- RESTful API controller'ları

Playwright Java kütüphanesini kullanmak istiyorum ve mevcut yapıdaki tüm özellikleri korumak istiyorum.
```

Bu prompt ve mimari, mevcut Node.js/TypeScript tabanlı test otomasyon yapınızı Spring Boot ile yeniden oluşturmanız için iyi bir başlangıç noktası olacaktır. Projeyi oluşturduktan sonra, her bir sınıfı ve arayüzü detaylı olarak implemente edebilirsiniz.
