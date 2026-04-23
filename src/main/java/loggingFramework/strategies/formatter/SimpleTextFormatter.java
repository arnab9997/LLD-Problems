package loggingFramework.strategies.formatter;

import loggingFramework.model.LogMessage;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SimpleTextFormatter implements LogFormatter {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    @Override
    public String formatLog(LogMessage message) {
        return String.format("%s [%s] %s %s - %s \n",
                FORMATTER.format(message.getTimeStamp()),
                message.getThreadName(),
                message.getLogLevel(),
                message.getLoggerName(),
                message.getMessage());
    }
}
