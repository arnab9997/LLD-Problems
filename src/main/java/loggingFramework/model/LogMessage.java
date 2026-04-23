package loggingFramework.model;

import loggingFramework.enums.LogLevel;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LogMessage {
    private final String message;
    private final LogLevel logLevel;
    private final String loggerName;
    private final String threadName;
    private final LocalDateTime timeStamp;

    public LogMessage(String message, LogLevel logLevel, String loggerName) {
        this.message = message;
        this.logLevel = logLevel;
        this.loggerName = loggerName;
        this.threadName = Thread.currentThread().getName();
        this.timeStamp = LocalDateTime.now();
    }
}
