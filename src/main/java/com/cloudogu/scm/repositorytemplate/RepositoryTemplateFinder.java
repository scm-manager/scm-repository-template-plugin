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

import sonia.scm.NotFoundException;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.util.Optional;

public class RepositoryTemplateFinder {

  private RepositoryTemplateFinder() {
  }

  public static final String TEMPLATE_YML = "template.yml";
  public static final String TEMPLATE_YAML = "template.yaml";

  public static Optional<String> templateFileExists(RepositoryService repositoryService) throws IOException {
    if (fileExists(repositoryService, TEMPLATE_YML)) {
      return Optional.of(TEMPLATE_YML);
    } else if (fileExists(repositoryService, TEMPLATE_YAML)) {
      return Optional.of(TEMPLATE_YAML);
    }
    return Optional.empty();

  }

  private static boolean fileExists(RepositoryService repositoryService, String templateYml) throws IOException {
    try {
      BrowserResult browserResult = repositoryService.getBrowseCommand().setPath(templateYml).getBrowserResult();
      return browserResult != null && browserResult.getFile() != null && templateYml.equals(browserResult.getFile().getName());
    } catch (NotFoundException e) {
      return false;
    }
  }
}
