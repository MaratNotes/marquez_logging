package marquez.logging.factories;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import marquez.logging.LogParserAppender;
import marquez.logging.config.LogParserConfig;
import marquez.logging.parserprocessor.LogParserProcessor;

/**
 * Фабрика для создания LogParserAppender, интегрированная с Dropwizard. Конфигурируется через
 * YAML-файл, создает и инициализирует цепочку: Appender -> Processor -> DAO.
 */
@JsonTypeName("log-parser")
public class LogParserAppenderFactory implements AppenderFactory<ILoggingEvent> {

  private LogParserConfig config;

  @JsonProperty("parser")
  public LogParserConfig getConfig() {
    System.out.println(
        "LOG PARSER FACTORY: getConfig called, config: " + (config != null ? "present" : "null"));
    return config;
  }

  @JsonProperty("parser")
  public void setConfig(LogParserConfig config) {
    System.out.println(
        "LOG PARSER FACTORY: setConfig called with config: "
            + (config != null ? config.getDbUrl() : "null"));
    this.config = config;
  }

  @Override
  public Appender<ILoggingEvent> build(
      LoggerContext context,
      String applicationName,
      LayoutFactory<ILoggingEvent> layoutFactory,
      LevelFilterFactory<ILoggingEvent> levelFilterFactory,
      AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory) {

    System.out.println(
        "LOG PARSER FACTORY: Building appender, config present: " + (config != null));

    LogParserAppender appender = new LogParserAppender();
    appender.setContext(context); // Добавьте это!

    if (config != null && config.getDbUrl() != null && !config.getDbUrl().isEmpty()) {
      System.out.println(
          "LOG PARSER FACTORY: Creating processor with config: " + config.getDbUrl());
      LogParserProcessor processor = new LogParserProcessor(config);
      appender.setProcessor(processor);
    } else {
      System.out.println("LOG PARSER FACTORY: No valid config, processor not created");
    }

    System.out.println("LOG PARSER FACTORY: Appender built successfully");

    System.out.println("LOG PARSER FACTORY: Appender start");
    appender.start();
    return appender;
  }
}
