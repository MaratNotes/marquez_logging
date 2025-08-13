package marquez.logging.parser;

import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import marquez.logging.config.LogParserConfig;
import marquez.logging.db.LogParserDao;

/**
 * Асинхронный процессор для сохранения логов.
 * Использует внутреннюю очередь для буферизации событий и фоновый поток
 * для их обработки и сохранения в базу данных через LogParserDao.
 */
public class LogParserProcessor {
  private final BlockingQueue<ILoggingEvent> eventQueue = new LinkedBlockingQueue<>();
  private final LogParserDao logParserDao;
  private final Thread workerThread;
  private final AtomicBoolean running = new AtomicBoolean(true);

  public LogParserProcessor(LogParserConfig config) {
    System.out.println(
        "LOG PARSER PROCESSOR: Initializing with config: "
            + config.getDbUrl()
            + ", user: "
            + config.getDbUser());
    try {
      this.logParserDao =
          new LogParserDao(config.getDbUrl(), config.getDbUser(), config.getDbPassword());
      System.out.println("LOG PARSER PROCESSOR: DAO initialized successfully");

      this.workerThread = new Thread(this::processEvents, "log-parser-processor");
      this.workerThread.setDaemon(true);
      this.workerThread.start();
      System.out.println("LOG PARSER PROCESSOR: Worker thread started");
    } catch (Exception e) {
      System.err.println("LOG PARSER PROCESSOR: Failed to initialize: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }

  public void addLogEvent(ILoggingEvent event) {
    System.out.println("LOG PARSER PROCESSOR: Offer to eventQueue");
    boolean added = eventQueue.offer(event);
    if (!added) {
      System.err.println("LOG PARSER PROCESSOR: Failed to add event to queue (queue full?)");
    }
  }

  private void processEvents() {
    System.out.println("LOG PARSER PROCESSOR: Event processing started");
    while (running.get()) {
      try {
        ILoggingEvent event = eventQueue.take();
        System.out.println(
            "LOG PARSER PROCESSOR: Processing log event: "
                + event.getLevel()
                + " - "
                + event.getFormattedMessage());
        logParserDao.saveLog(event);
      } catch (InterruptedException e) {
        System.out.println("LOG PARSER PROCESSOR: Interrupted, stopping...");
        Thread.currentThread().interrupt();
        break;
      } catch (Exception e) {
        System.err.println("LOG PARSER PROCESSOR: Error processing event: " + e.getMessage());
        e.printStackTrace();
      }
    }
    System.out.println("LOG PARSER PROCESSOR: Event processing stopped");
  }

  public void stop() {
    System.out.println("LOG PARSER PROCESSOR: Stopping...");
    running.set(false);
    workerThread.interrupt();
  }
}
