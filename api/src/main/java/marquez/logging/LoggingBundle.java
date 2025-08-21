package marquez.logging;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import marquez.logging.factories.LogParserAppenderFactory;

/**
 * Класс для регистрации кастомного аппендера в системе. Регистрирует фабрику
 * LogParserAppenderFactory в Jackson, что позволяет использовать ее в конфигурации YAML.
 */
public class LoggingBundle implements ConfiguredBundle<Object> {
  @Override
  public void initialize(Bootstrap<?> bootstrap) {
    System.out.println("LOGGING BUNDLE: Initializing...");
    bootstrap.getObjectMapper().registerSubtypes(LogParserAppenderFactory.class);
    System.out.println("LOGGING BUNDLE: LogParserAppenderFactory registered");
  }

  @Override
  public void run(Object configuration, Environment environment) {
    System.out.println("LOGGING BUNDLE: Running...");
  }
}
