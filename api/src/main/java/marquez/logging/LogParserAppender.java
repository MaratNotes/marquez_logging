package marquez.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import marquez.logging.parserprocessor.LogParserProcessor;

/**
 * Кастомный аппендер Logback, для перехвата события логирования.
 * Также передает их в LogParserProcessor для асинхронной обработки.
 * Реализует стандартный жизненный цикл AppenderBase.
 */
public class LogParserAppender extends AppenderBase<ILoggingEvent> {
  private LogParserProcessor processor;

  @Override
  public void start() {
    super.start();
    System.out.println(
        "LOG PARSER APPENDER: STARTED"
            + (processor != null ? " with processor" : " WITHOUT PROCESSOR!"));
  }

  @Override
  protected void append(ILoggingEvent event) {
    if (processor != null) {
      System.out.println("LOG PARSER APPENDER: call addLogEvent");
      processor.addLogEvent(event);
    } else {
      System.out.println(
          "LOG PARSER APPENDER: No processor configured, ignoring event: "
              + event.getFormattedMessage());
    }
  }

  @Override
  public void stop() {
    System.out.println("LOG PARSER APPENDER: STOPPING...");
    if (processor != null) {
      processor.stop();
    }
    super.stop();
    System.out.println("LOG PARSER APPENDER: STOPPED");
  }

  public void setProcessor(LogParserProcessor processor) {
    System.out.println("LOG PARSER APPENDER: Processor set");
    this.processor = processor;
  }
}
