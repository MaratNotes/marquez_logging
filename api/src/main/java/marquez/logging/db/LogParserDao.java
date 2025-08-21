package marquez.logging.db;

import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.Map;
import org.jdbi.v3.core.Jdbi;

/** Класс для работы с таблицей разобранных логов. */
public class LogParserDao {
  private final Jdbi jdbi;

  public LogParserDao(String jdbcUrl, String username, String password) {
    System.out.println("LOG PARSER DAO: Initializing with URL: " + jdbcUrl);
    try {
      this.jdbi = Jdbi.create(jdbcUrl, username, password);
      System.out.println("LOG PARSER DAO: JDBI created successfully");
    } catch (Exception e) {
      System.err.println("LOG PARSER DAO: Failed to initialize: " + e.getMessage());
      e.printStackTrace();
      throw e;
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

  public void saveAuditLog(Map<String, Object> auditData) {
    try {
      jdbi.useHandle(
          handle -> {
            handle
                .createUpdate(
                    "INSERT INTO audit.marquez_audit "
                        + "(suser, src, dhost, dhostname, app, start, eventtype, eventclass, name) "
                        + "VALUES (:suser, :src, :dhost, :dhostname, :app, :start, :eventtype, :eventclass, :name)")
                .bindMap(auditData)
                .execute();
          });
    } catch (Exception e) {
      System.err.println("LOG PARSER DAO: Failed to save audit log: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
