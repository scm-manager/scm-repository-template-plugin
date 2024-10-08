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

import com.github.legman.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.HandlerEventType;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.security.AdministrationContext;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.cloudogu.scm.repositorytemplate.RepositoryTemplateFinder.TEMPLATE_YAML;
import static com.cloudogu.scm.repositorytemplate.RepositoryTemplateFinder.TEMPLATE_YML;

@Singleton
public class RepositoryTemplateCollector {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryTemplateCollector.class);

  private static final String CACHE_NAME = "sonia.cache.repository.templates";

  private final AdministrationContext administrationContext;
  private final RepositoryServiceFactory serviceFactory;
  private final RepositoryManager repositoryManager;
  private final Cache<String, Collection<RepositoryTemplate>> cache;

  @Inject
  public RepositoryTemplateCollector(AdministrationContext administrationContext, RepositoryServiceFactory serviceFactory, RepositoryManager repositoryManager, CacheManager cacheManager) {
    this.administrationContext = administrationContext;
    this.serviceFactory = serviceFactory;
    this.repositoryManager = repositoryManager;
    this.cache = cacheManager.getCache(CACHE_NAME);
  }

  public Collection<RepositoryTemplate> collect() {
    final Collection<RepositoryTemplate> repositoryTemplates = new ArrayList<>();
    String cacheKey = "templates";

    Collection<RepositoryTemplate> cachedRepoTemplates = cache.get(cacheKey);
    if (cachedRepoTemplates != null) {
      repositoryTemplates.addAll(cachedRepoTemplates);
    } else {
      administrationContext.runAsAdmin(() -> filterAllRepositoriesForTemplateFile(repositoryTemplates));
      cache.put(cacheKey, repositoryTemplates);
    }

    filterTemplatesByUserPermission(repositoryTemplates);

    return repositoryTemplates;
  }

  @Subscribe
  public void onEvent(PostReceiveRepositoryHookEvent event) {
    try (RepositoryService repositoryService = serviceFactory.create(event.getRepository())) {
      if (wasTemplateFileEffected(event, repositoryService)) {
        cache.clear();
      }
    } catch (IOException e) {
      LOG.error("could not clear cached repository templates", e);
    }
  }

  @Subscribe
  public void onEvent(RepositoryEvent event) {
    if (wasRepositoryDeleted(event) || wasRepositoryRenamed(event)) {
      cache.clear();
    }
  }

  private boolean wasRepositoryRenamed(RepositoryEvent event) {
    return event.getEventType() == HandlerEventType.MODIFY
      && !event.getItem().getNamespaceAndName().toString().equals(event.getOldItem().getNamespaceAndName().toString());
  }

  private boolean wasRepositoryDeleted(RepositoryEvent event) {
    return event.getEventType() == HandlerEventType.DELETE;
  }

  private boolean wasTemplateFileEffected(PostReceiveRepositoryHookEvent event, RepositoryService repositoryService) throws IOException {
    for (Changeset changeset : event.getContext().getChangesetProvider().getChangesets()) {
      List<String> changedFiles = repositoryService.getModificationsCommand().revision(changeset.getId()).getModifications().getEffectedPaths();
      if (changedFiles.contains(TEMPLATE_YML) || changedFiles.contains(TEMPLATE_YAML)) {
        return true;
      }
    }
    return false;
  }

  private void filterTemplatesByUserPermission(Collection<RepositoryTemplate> repositoryTemplates) {
    repositoryTemplates.removeIf(repositoryTemplate -> !RepositoryPermissions.read(repositoryTemplate.getRepositoryId()).isPermitted());
  }

  private void filterAllRepositoriesForTemplateFile(Collection<RepositoryTemplate> repositoryTemplates) {
    for (Repository repository : repositoryManager.getAll()) {
      try (RepositoryService repositoryService = serviceFactory.create(repository)) {
        Optional<String> templateFile = RepositoryTemplateFinder.templateFileExists(repositoryService);
        if (templateFile.isPresent()) {
          repositoryTemplates.add(new RepositoryTemplate(repository));
        }
      } catch (IOException e) {
        LOG.error("could not read template file in repository" + repository.getNamespaceAndName().toString(), e);
      }
    }
  }
}
