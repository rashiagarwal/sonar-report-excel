package org.sonarqube;

import dagger.ObjectGraph;
import org.sonarqube.server.SonarServer;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

  public static void main(String[] args) throws SQLException, IOException {
    ObjectGraph objectGraph = ObjectGraph.create(new SonarReportModule());
    SonarServer server = objectGraph.get(SonarServer.class);

    server.downloadExcel();
  }
}