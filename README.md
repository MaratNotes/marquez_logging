## Логика работы лог-парсера

1.  **Dropwizard** при запуске читает конфигурацию `marquez.dev.yml`.
2.  **`LoggingBundle`** регистрирует `LogParserAppenderFactory`.
3.  **`LogParserAppenderFactory`** создается для обработки секции `type: log-parser` и настраивает `LogParserAppender`.
4.  **Marquez приложение** начинает работу и генерирует логи через SLF4J/Logback.
5.  **Logback** передает лог-события в метод `LogParserAppender.append()`.
6.  **`LogParserAppender`** передает события в метод `LogParserProcessor.addLogEvent()`.
7.  **`LogParserProcessor`** ставит события в очередь и обрабатывает их в фоновом потоке.
8.  **`LogParserDao`** сохраняет логи в PostgreSQL через JDBI.
9.  **PostgreSQL** хранит структурированные логи в таблице `parsed_logs`.