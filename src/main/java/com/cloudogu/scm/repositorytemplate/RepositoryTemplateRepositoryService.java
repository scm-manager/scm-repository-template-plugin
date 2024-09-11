/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.repositorytemplate;

import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RepositoryTemplateRepositoryService {

  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public RepositoryTemplateRepositoryService(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  public void templateRepository(NamespaceAndName namespaceAndName) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      String fileContent = "engine: mustache\n" +
        "encoding: UTF-8\n" +
        "files:\n" +
        "  - path: \"\"\n" +
        "    filtered: false";

      repositoryService.getModifyCommand()
        .setCommitMessage("Create template from repository")
        .createFile("template.yml")
        .withData(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)))
        .execute();
    }
  }

  public void untemplateRepository(NamespaceAndName namespaceAndName) {
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      repositoryService.getModifyCommand()
        .setCommitMessage("Delete template from repository")
        .deleteFile("template.yml")
        .execute();
    }
  }
}


