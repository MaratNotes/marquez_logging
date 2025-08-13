package marquez.logging.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LogParserConfig {
  private String dbUrl;
  private String dbUser;
  private String dbPassword;

  @JsonProperty("dbUrl")
  public String getDbUrl() {
    return dbUrl;
  }

  @JsonProperty("dbUrl")
  public void setDbUrl(String dbUrl) {
    this.dbUrl = dbUrl;
  }

  @JsonProperty("dbUser")
  public String getDbUser() {
    return dbUser;
  }

  @JsonProperty("dbUser")
  public void setDbUser(String dbUser) {
    this.dbUser = dbUser;
  }

  @JsonProperty("dbPassword")
  public String getDbPassword() {
    return dbPassword;
  }

  @JsonProperty("dbPassword")
  public void setDbPassword(String dbPassword) {
    this.dbPassword = dbPassword;
  }
}
