package loggingFramework;

import loggingFramework.enums.LogLevel;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Setter
public class LogManager {
    private static final LogManager INSTANCE = new LogManager();

    private final Map<String, Logger> loggers = new ConcurrentHashMap<>();
    private LogLevel defaultLevel = LogLevel.INFO;

    private LogManager() {}

    public static LogManager getInstance() {
        return INSTANCE;
    }

    public Logger getLogger(String name) {
        Logger existing = loggers.get(name);
        if (existing != null) return existing;

        synchronized (this) {
            return loggers.computeIfAbsent(name, k -> new Logger(k, defaultLevel));
        }
    }
}
