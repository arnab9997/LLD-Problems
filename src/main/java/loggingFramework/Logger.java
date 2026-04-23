package loggingFramework;

import loggingFramework.enums.LogLevel;
import loggingFramework.model.LogMessage;
import loggingFramework.strategies.appender.LogAppender;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class Logger {
    private final String name;
    private final List<LogAppender> appenders;
    private volatile LogLevel logLevel;

    public Logger(String name, LogLevel logLevel) {
        this.name = name;
        this.logLevel = logLevel;
        this.appenders = new CopyOnWriteArrayList<>();
    }

    public void addAppender(LogAppender appender) {
        this.appenders.add(appender);
    }

    public void log(LogLevel messageLevel, String message) {
        if (!messageLevel.isGreaterOrEqualSeverity(logLevel)) {
            return;
        }

        LogMessage logMessage = new LogMessage(message, messageLevel, this.name);
        for (LogAppender appender : appenders) {
            appender.append(logMessage);
        }
    }

    public void debug(String message) { log(LogLevel.DEBUG, message); }
    public void info(String message)  { log(LogLevel.INFO, message); }
    public void warn(String message)  { log(LogLevel.WARN, message); }
    public void error(String message) { log(LogLevel.ERROR, message); }
}