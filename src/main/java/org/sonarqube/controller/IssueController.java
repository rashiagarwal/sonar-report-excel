package org.sonarqube.controller;

import org.apache.log4j.Logger;
import org.sonarqube.model.*;
import org.sonarqube.service.IssueService;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.getProperty;
import static java.util.stream.Collectors.toList;

public class IssueController {

  private static final Logger logger = Logger.getLogger("Connection Issue");

  private IssueService service;
  private TagController tagController;
  private Set<Issue> issues;

  @Inject
  public IssueController(Retrofit retrofit, TagController tagController) {
    this.service = retrofit.create(IssueService.class);
    this.tagController = tagController;
    issues = new HashSet<>();
  }

  public Set<Issue> fetchIssues() throws IOException {
    List<Severity> severities = getSeverities();

    severities.parallelStream().forEach(this::fetchAndAddIssuesBySeverity);

    return issues;
  }

  private void fetchAndAddIssuesBySeverity(Severity severity) {
    List<Type> types = getTypes();

    types.parallelStream().forEach(type -> {
      try {
        fetchAndAddIssuesByType(severity, type);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  private List<Type> getTypes() {
    return Stream.of(Type.values()).collect(Collectors.toList());
  }

  private void fetchAndAddIssuesByType(Severity severity, Type type) throws IOException {
    Set<String> tags = tagController.getTags();

    tags.parallelStream().forEach(tag -> fetchAndAddIssuesByTag(severity, type, tag));
  }

  private void fetchAndAddIssuesByTag(Severity severity, Type type, String tag) {
    try {
      logger.info("Severity = " + severity.name() + "; Type = " + type.name() + "; Tag = " + tag);
      IssueResource issue = getIssue(severity.name(), type.name(), tag, 1);
      if (issue != null && !issue.getIssues().isEmpty()) {
        int maxPageIndex = getMaxPageIndex(issue);
        for (int pageIndex = 1; pageIndex <= maxPageIndex; pageIndex++) {
          getAndAddIssues(severity, type, tag, pageIndex);
        }
      }
    } catch (IOException e) {
      logger.error(Arrays.toString(e.getStackTrace()));
    }
  }

  private int getMaxPageIndex(IssueResource issue) {
    Paging paging = issue.getPaging();
    int totalNumberOfPages = (int) Math.ceil((double) paging.getTotal() / paging.getPageSize());
    int maxPageIndex;
    maxPageIndex = totalNumberOfPages > 20 ? 20 : totalNumberOfPages;
    return maxPageIndex;
  }

  private void getAndAddIssues(Severity severity, Type type,
                               String tag, int pageIndex) throws IOException {
    IssueResource issue;
    issue = getIssue(severity.name(), type.name(), tag, pageIndex);
    if (issue != null && !issue.getIssues().isEmpty()) {
      issues.addAll(issue.getIssues());
    }
  }

  private IssueResource getIssue(String severity, String type, String tag, int pageIndex) throws
      IOException {
    Response<IssueResource> response = execute(severity, type, tag, pageIndex);
    if (response.isSuccessful()) {
      return response.body();
    }
    return null;
  }

  private Response<IssueResource> execute(String severity, String type, String tag, int pageIndex) throws
      IOException {

    Map<String, String> queryMap = new HashMap<>();

    String key = getProperty("Key");
    if (key != null) {
      queryMap.put("componentRoots", key);
    }
    queryMap.put("resolved", String.valueOf(false));
    queryMap.put("severities", severity);
    queryMap.put("types", type);
    queryMap.put("tags", tag);
    queryMap.put("pageIndex", String.valueOf(pageIndex));
    queryMap.put("pageSize", String.valueOf(500));

    Call<IssueResource> call = service.listIssues(queryMap);

    return call.execute();
  }

  private static List<Severity> getSeverities() {
    return Stream.of(Severity.values()).collect(toList());
  }
}