package loggingFramework.strategies.formatter;

import loggingFramework.model.LogMessage;

/**
 * Thread-safety: Implementations must be stateless and thread-safe
 */
public interface LogFormatter {
    String formatLog(LogMessage message);
}
