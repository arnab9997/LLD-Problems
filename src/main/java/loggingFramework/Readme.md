## Functional Requirements
* Applications should be able to log messages with different severity levels. 
* Log messages should contain metadata eg: timestamp, log level, message, logger name, thread name
* The system should allow:
  * multiple loggers
  * configurable log levels
  * multiple appenders per logger
  * configurable formatters
* Loggers should support runtime log level updates.
* Log messages should be formatted before being written to output destinations.
* The system should support multiple output destinations (console initially, extensible to file, network etc).

---

## Non-functional requirements
* Thread-safe logging.
* Low overhead for log filtering.
* Extensible architecture for adding:
  * new appenders 
  * new formatters
* Fail-safe logging (logging failures should not crash the application).
* Idempotent resource cleanup for appenders.
* Efficient concurrent access to logger instances.

---

## Core entities
* LogAppender
* LogFormatter
* LogMessage
* Logger
* LogManager

---

## Enums
* LogLevel

---

## State Models
N/A

---

## Design Patterns
* Singleton Design Pattern - For single instance of LogManager
* Strategy Design Pattern - For LogAppender & LogFormatter
* Template Design Pattern - Define logging workflow while allowing subclasses to implement specific behaviour.

---

## API Design
* Fetch logger: getLogger("com.example.Main")
* Attach appender: addAppender(new ConsoleAppender())
* Change Log Level: setLogLevel(LogLevel.DEBUG)

---

## DB Persistence
N/A

---

## Notes
* ConcurrentHashMap ensures thread-safe logger retrieval in LogManager.
* CopyOnWriteArrayList allows safe concurrent iteration over appenders.
* volatile logLevel ensures visibility across threads.
* Logging failures are isolated inside appenders and do not propagate to the application.
* AtomicBoolean ensures safe and idempotent close operations for appenders.