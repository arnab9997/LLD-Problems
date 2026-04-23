package loggingFramework.strategies.appender;

import loggingFramework.model.LogMessage;

/**
 * Thread safety contract:
 *  - append() must be thread safe.
 *  - append() must not throw errors - handle errors internally.
 *  - close() is invoked once during shutdown to release resources. Needs to be idempotent
 */
public interface LogAppender {
    void append(LogMessage message);
    void close() throws Exception;
}
