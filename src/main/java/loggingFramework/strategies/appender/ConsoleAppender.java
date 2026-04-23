package loggingFramework.strategies.appender;

import loggingFramework.model.LogMessage;

import java.io.PrintStream;

/**
 * Thread-safe: PrintStream is synchronized internally.
 */
public class ConsoleAppender extends AbstractLogAppender {
    private final PrintStream stream;

    public ConsoleAppender() {
        this(System.out);
    }

    public ConsoleAppender(PrintStream stream) {
        super();
        this.stream = stream;
    }

    @Override
    protected void doAppend(LogMessage message) throws Exception {
        String formatted = formatter.formatLog(message);
        stream.print(formatted);
    }

    @Override
    protected void doClose() throws Exception {
        // Don't close System.out - just flush
        stream.flush();
    }
}
