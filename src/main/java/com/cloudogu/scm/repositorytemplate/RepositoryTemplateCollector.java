package com.cloudogu.scm.repositorytemplate;

import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class RepositoryTemplateCollector {

  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public RepositoryTemplateCollector(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  public List<RepositoryTemplate> collect() {
    return Collections.emptyList();
  }
}
