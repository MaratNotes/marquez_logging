package marquez.logging.db;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.jdbi.v3.core.Jdbi;

/**
 * Класс для работы с таблицей разобранных логов.
 */
public class LogParserDao {
  private final Jdbi jdbi;

  public LogParserDao(String jdbcUrl, String username, String password) {
    System.out.println("LOG PARSER DAO: Initializing with URL: " + jdbcUrl);
    try {
      this.jdbi = Jdbi.create(jdbcUrl, username, password);
      System.out.println("LOG PARSER DAO: JDBI created successfully");
      createTable();
    } catch (Exception e) {
      System.err.println("LOG PARSER DAO: Failed to initialize: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }

  private void createTable() {
    System.out.println("LOG PARSER DAO: Creating table...");
    try {
      jdbi.useHandle(
          handle -> {
            handle.execute(
                "CREATE TABLE IF NOT EXISTS parsed_logs ("
                    + "id SERIAL PRIMARY KEY,"
                    + "log_level VARCHAR(10) NOT NULL,"
                    + "logger_name VARCHAR(255) NOT NULL,"
                    + "log_message TEXT NOT NULL,"
                    + "thread_name VARCHAR(100) NOT NULL,"
                    + "created_at TIMESTAMP NOT NULL DEFAULT NOW()"
                    + ")");
          });
      System.out.println("LOG PARSER DAO: Table created/verified successfully");
    } catch (Exception e) {
      System.err.println("LOG PARSER DAO: Failed to create table: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void saveLog(ILoggingEvent event) {
    System.out.println("LOG PARSER DAO: Call insert...");
    try {
      jdbi.useHandle(
          handle -> {
            handle
                .createUpdate(
                    "INSERT INTO parsed_logs "
                        + "(log_level, logger_name, log_message, thread_name) "
                        + "VALUES (:level, :logger, :message, :thread)")
                .bind("level", event.getLevel().toString())
                .bind("logger", event.getLoggerName())
                .bind("message", event.getFormattedMessage())
                .bind("thread", event.getThreadName())
                .execute();
          });
    } catch (Exception e) {
      System.err.println("LOG PARSER DAO: Failed to save log: " + e.getMessage());
      // Не бросаем исключение, чтобы не сломать основное приложение
    }
  }
}
