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

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.io.IOException;
import java.util.Optional;

@Extension
@Enrich(Repository.class)
public class RepositoryLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;
  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public RepositoryLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStore, RepositoryServiceFactory serviceFactory) {
    this.scmPathInfoStore = scmPathInfoStore;
    this.serviceFactory = serviceFactory;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    if (RepositoryPermissions.push(repository).isPermitted()) {
      try (RepositoryService repositoryService = serviceFactory.create(repository.getId())) {
        appendLinks(appender, repository, repositoryService);
      } catch (IOException e) {
        throw new InternalRepositoryException(repository, "could not check for template file", e);
      }
    }
  }

  private void appendLinks(HalAppender appender, Repository repository, RepositoryService repositoryService) throws IOException {
    Optional<String> templateFile = RepositoryTemplateFinder.templateFileExists(repositoryService);
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), RepositoryTemplateRepositoryResource.class);
    if (templateFile.isPresent()) {
      appender.appendLink("untemplate", linkBuilder.method("untemplateRepository").parameters(repository.getNamespace(), repository.getName()).href());
    } else {
      appender.appendLink("template", linkBuilder.method("templateRepository").parameters(repository.getNamespace(), repository.getName()).href());
    }
  }
}
