// marquez/logging/parserprocessor/MarquezAuditDetector.java

package marquez.logging.parserprocessor;

import ch.qos.logback.classic.spi.ILoggingEvent;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarquezAuditDetector {

  // === Константы аудита ===
  private static final String SUSER_DEFAULT = "SYSTEM";
  private static final String SRC_DEFAULT = "SRC_DEFAULT"; // sdq-...
  private static final String DHOST_DEFAULT = "DHOST_DEFAULT";
  private static final String DHOSTNAME_DEFAULT = "DHOSTNAME_DEFAULT"; // // sdq-...
  private static final String APP_DEFAULT = "MARQUEZ";

  // === Типы событий ===
  public static final String EVENTCLASS_SERVICE_START = "SERVICE_START";
  public static final String EVENTCLASS_SERVICE_STOP = "SERVICE_STOP";
  public static final String EVENTCLASS_WEB_ACCESS = "WEB_ACCESS";
  public static final String EVENTCLASS_API_CALL = "API_CALL";

  // === Паттерн для thread_name ===
  private static final Pattern THREAD_HTTP_PATTERN = Pattern.compile("^dw-\\d+ - (\\S+) (\\S+)");

  public static Map<String, Object> detect(ILoggingEvent event) {
    String loggerName = event.getLoggerName();
    String message = event.getFormattedMessage();
    String threadName = event.getThreadName();

    System.out.println(
        "MarquezAuditDetector: logger='"
            + loggerName
            + "', msg='"
            + message
            + "', thread='"
            + threadName
            + "'");

    // 1. Событие старта
    if (isServiceStartEvent(loggerName, message)) {
      System.out.println("MarquezAuditDetector: SERVICE START detected");
      return buildServiceStartEvent(event);
    }

    // 2. Событие остановки
    if (isServiceStopEvent(loggerName, message)) {
      System.out.println("MarquezAuditDetector: SERVICE STOP detected");
      return buildServiceStopEvent(event);
    }

    // 3. HTTP-запросы (MDC)
    if (isHttpRequestEvent(loggerName, message, threadName)) {
      System.out.println("MarquezAuditDetector: HTTP REQUEST detected");
      return parseHttpRequestFromThreadName(threadName, message, event.getTimeStamp());
    }

    // 4. Не аудитим
    System.out.println("MarquezAuditDetector: NO AUDIT");
    return null;
  }

  private static boolean isServiceStartEvent(String loggerName, String message) {
    return "org.eclipse.jetty.server.Server".equals(loggerName) && message.contains("Started");
  }

  private static boolean isServiceStopEvent(String loggerName, String message) {
    return "org.eclipse.jetty.server.Server".equals(loggerName)
        && (message.contains("Stopped")
            || message.contains("Shutting down")
            || message.contains("Shutdown"));
  }

  private static boolean isHttpRequestEvent(String loggerName, String message, String threadName) {
    return "marquez.logging.LoggingMdcFilter".equals(loggerName)
        && message.startsWith("status:")
        && threadName != null
        && threadName.contains(" - ");
  }

  private static Map<String, Object> buildServiceStartEvent(ILoggingEvent event) {
    Map<String, Object> audit = new HashMap<>();
    audit.put("suser", SUSER_DEFAULT);
    audit.put("src", SRC_DEFAULT);
    audit.put("dhost", DHOST_DEFAULT);
    audit.put("dhostname", DHOSTNAME_DEFAULT);
    audit.put("app", APP_DEFAULT);
    audit.put("start", Instant.ofEpochMilli(event.getTimeStamp()).atOffset(ZoneOffset.UTC));
    audit.put("eventtype", "info");
    audit.put("eventclass", EVENTCLASS_SERVICE_START);
    audit.put("name", "Marquez server started");
    return audit;
  }

  private static Map<String, Object> buildServiceStopEvent(ILoggingEvent event) {
    Map<String, Object> audit = new HashMap<>();
    audit.put("suser", SUSER_DEFAULT);
    audit.put("src", SRC_DEFAULT);
    audit.put("dhost", DHOST_DEFAULT);
    audit.put("dhostname", DHOSTNAME_DEFAULT);
    audit.put("app", APP_DEFAULT);
    audit.put("start", Instant.ofEpochMilli(event.getTimeStamp()).atOffset(ZoneOffset.UTC));
    audit.put("eventtype", "info");
    audit.put("eventclass", EVENTCLASS_SERVICE_STOP);
    audit.put("name", "Marquez server stopped");
    return audit;
  }

  private static Map<String, Object> parseHttpRequestFromThreadName(
      String threadName, String statusMessage, long timestamp) {

    Matcher m = THREAD_HTTP_PATTERN.matcher(threadName);
    if (!m.find()) {
      return null;
    }

    String method = m.group(1);
    String uri = m.group(2);
    int status = extractStatusFromMessage(statusMessage);

    Map<String, Object> audit = new HashMap<>();
    audit.put("suser", "unknown");
    audit.put("src", "unknown");
    audit.put("dhost", DHOST_DEFAULT);
    audit.put("dhostname", DHOSTNAME_DEFAULT);
    audit.put("app", APP_DEFAULT);
    audit.put("start", Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC));

    if ("/".equals(uri)) {
      audit.put("eventclass", EVENTCLASS_WEB_ACCESS);
      audit.put("name", "Web UI accessed via " + method);
    } else if (uri.startsWith("/api/v1/")) {
      audit.put("eventclass", EVENTCLASS_API_CALL);
      audit.put("name", method + " " + uri);
    } else {
      audit.put("eventclass", "http_request");
      audit.put("name", method + " " + uri);
    }

    if (status >= 500) {
      audit.put("eventtype", "error");
    } else if (status >= 400) {
      audit.put("eventtype", "warning");
    } else {
      audit.put("eventtype", "info");
    }

    return audit;
  }

  private static int extractStatusFromMessage(String message) {
    try {
      return Integer.parseInt(message.replaceAll("\\D+", ""));
    } catch (Exception e) {
      return 200;
    }
  }
}
