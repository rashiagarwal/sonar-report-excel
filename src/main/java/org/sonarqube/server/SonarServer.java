package org.sonarqube.server;

import org.sonarqube.controller.IssueController;
import org.sonarqube.model.Issue;
import org.sonarqube.utility.Excel;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

public class SonarServer {

  private IssueController issueController;
  private Excel excel;

  @Inject
  public SonarServer(IssueController issueController, Excel excel) {
    this.issueController = issueController;
    this.excel = excel;
  }

  public void downloadExcel() throws SQLException, IOException {
    Set<Issue> issues = fetchIssues();

    if (!issues.isEmpty()) {
      excel.write(issues);
    }
  }

  private Set<Issue> fetchIssues() throws IOException {
    return issueController.fetchIssues();
  }
}
