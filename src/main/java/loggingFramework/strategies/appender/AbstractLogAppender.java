package loggingFramework.strategies.appender;

import loggingFramework.model.LogMessage;
import loggingFramework.strategies.formatter.LogFormatter;
import loggingFramework.strategies.formatter.SimpleTextFormatter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base appender implementation
 *
 * Key patterns:
 *  - Template method (append -> doAppend; close -> doClose)
 *  - Error handling (never throw from append)
 *  - Idempotent close (using CAS)
 */
public abstract class AbstractLogAppender implements LogAppender {
    protected volatile LogFormatter formatter;
    private final AtomicBoolean closed;

    protected AbstractLogAppender() {
        this.formatter = new SimpleTextFormatter();
        this.closed = new AtomicBoolean(false);
    }

    @Override
    public void append(LogMessage message) {
        if (closed.get()) {
            return;
        }

        try {
            doAppend(message);
        } catch (Exception e) {
            System.err.printf("ERROR: Failed to append log [%s]: %s \n", message.getLoggerName(), e.getMessage());
        }
    }

    /**
     * Subclasses implement actual writing logic here.
     * Can throw - will be caught by append().
     */
    protected abstract void doAppend(LogMessage message) throws Exception;

    @Override
    public final void close() throws Exception {
        // Idempotent close using CAS
        if (closed.compareAndSet(false, true)) {
            doClose();
        }
    }

    protected abstract void doClose() throws Exception;
}
