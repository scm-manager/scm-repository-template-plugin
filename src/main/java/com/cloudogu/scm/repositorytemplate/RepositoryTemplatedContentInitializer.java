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

import lombok.Data;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryContentInitializer;
import sonia.scm.repository.RepositoryManager;

import jakarta.inject.Inject;
import java.util.Optional;

@Extension
public class RepositoryTemplatedContentInitializer implements RepositoryContentInitializer {

  private final RepositoryManager repositoryManager;
  private final RepositoryTemplater templater;

  @Inject
  public RepositoryTemplatedContentInitializer(RepositoryManager repositoryManager, RepositoryTemplater templater) {
    this.repositoryManager = repositoryManager;
    this.templater = templater;
  }

  @Override
  public void initialize(InitializerContext context) {
    Optional<TemplateContext> templateContext = context.getEntry("repository-template", TemplateContext.class);
    if (templateContext.isPresent()) {
      String[] splitRepository = templateContext.get().getTemplateId().split("/");
      Repository templateRepository = repositoryManager.get(new NamespaceAndName(splitRepository[0], splitRepository[1]));
      templater.render(templateRepository, context);
    }
  }

  @Data
  static class TemplateContext {
    private String templateId;
  }
}
