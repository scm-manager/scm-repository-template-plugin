/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cloudogu.scm.repositorytemplate;

import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
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


