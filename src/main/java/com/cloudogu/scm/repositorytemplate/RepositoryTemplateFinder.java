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

import sonia.scm.NotFoundException;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.util.Optional;

public class RepositoryTemplateFinder {

  public static final String TEMPLATE_YML = "template.yml";
  public static final String TEMPLATE_YAML = "template.yaml";
  private RepositoryTemplateFinder() {
  }

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
      return browserResult != null
        && browserResult.getFile() != null
        && !browserResult.getFile().isDirectory() &&
        templateYml.equals(browserResult.getFile().getName());
    } catch (NotFoundException e) {
      return false;
    }
  }
}
