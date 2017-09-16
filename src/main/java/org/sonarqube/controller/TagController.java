package org.sonarqube.controller;

import org.sonarqube.model.TagResource;
import org.sonarqube.service.TagService;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

import static java.lang.System.getProperty;
import static java.util.Collections.emptySet;

public class TagController {
  private TagService service;

  @Inject
  TagController(Retrofit retrofit) {
    this.service = retrofit.create(TagService.class);
  }

  Set<String> getTags() throws IOException {
    TagResource resource = getResource();
    if (resource != null) {
      return resource.getTags();
    }
    return emptySet();
  }

  private TagResource getResource() throws IOException {
    Response<TagResource> response = execute();
    if (response.isSuccessful()) {
      return response.body();
    }
    return null;
  }

  private Response<TagResource> execute() throws IOException {
    Call<TagResource> call = service.listTags(getProperty("Key"));
    return call.execute();
  }
}