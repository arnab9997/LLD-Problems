package loggingFramework;

import loggingFramework.enums.LogLevel;
import loggingFramework.strategies.appender.ConsoleAppender;

public class LoggingFrameworkDemo {
    public static void main(String[] args) {
        LogManager logManager = LogManager.getInstance();

        Logger mainLogger = logManager.getLogger("com.example.Main");
        mainLogger.addAppender(new ConsoleAppender());
        mainLogger.info("Application starting up.");
        mainLogger.debug("Should NOT appear — default level is INFO.");

        // Override level on a specific logger
        Logger serviceLogger = logManager.getLogger("com.example.service.UserService");
        serviceLogger.addAppender(new ConsoleAppender());
        serviceLogger.setLogLevel(LogLevel.DEBUG);
        serviceLogger.debug("Should appear — service overrides to DEBUG.");

        // Dynamic level change
        mainLogger.setLogLevel(LogLevel.DEBUG);
        mainLogger.debug("Should now appear after level change.");
    }
}