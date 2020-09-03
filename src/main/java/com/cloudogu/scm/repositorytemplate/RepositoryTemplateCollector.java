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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.legman.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.security.AdministrationContext;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class RepositoryTemplateCollector {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryTemplateCollector.class);

  private static final String TEMPLATE_YML = "template.yml";
  private static final String TEMPLATE_YAML = "template.yaml";
  private static final String CACHE_NAME = "sonia.cache.repository.templates";

  private final AdministrationContext administrationContext;
  private final RepositoryServiceFactory serviceFactory;
  private final RepositoryManager repositoryManager;
  private final Cache<String, Collection<RepositoryTemplate>> cache;


  private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

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
    boolean clearCache;
    try (RepositoryService repositoryService = serviceFactory.create(event.getRepository())) {
      clearCache = wasTemplateFileEffected(event, repositoryService);
      if (clearCache) {
        cache.clear();
      }
    } catch (IOException e) {
      LOG.error("could not clear repository template cache", e);
    }
  }

  private boolean wasTemplateFileEffected(PostReceiveRepositoryHookEvent event, RepositoryService repositoryService) throws IOException {
    boolean clearCache = false;
    for (Changeset changeset : event.getContext().getChangesetProvider().getChangesets()) {
      List<String> changedFiles = repositoryService.getModificationsCommand().revision(changeset.getId()).getModifications().getEffectedPaths();
      clearCache = changedFiles.contains(TEMPLATE_YML) || changedFiles.contains(TEMPLATE_YAML);
      if (clearCache) {
        break;
      }
    }
    return clearCache;
  }

  private void filterTemplatesByUserPermission(Collection<RepositoryTemplate> repositoryTemplates) {
    repositoryTemplates.removeIf(repositoryTemplate -> {
      String[] splittedNamespaceAndName = repositoryTemplate.getNamespaceAndName().split("/");
      Repository repository = repositoryManager.get(new NamespaceAndName(splittedNamespaceAndName[0], splittedNamespaceAndName[1]));
      return !RepositoryPermissions.read(repository).isPermitted();
    });
  }

  private void filterAllRepositoriesForTemplateFile(Collection<RepositoryTemplate> repositoryTemplates) {
    for (Repository repository : repositoryManager.getAll()) {
      try (RepositoryService repositoryService = serviceFactory.create(repository)) {
        Optional<String> templateFile = templateFileExists(repositoryService);
        if (templateFile.isPresent()) {
          repositoryTemplates.add(mapRepositoryTemplateFromFile(repositoryService, templateFile.get()));
        }

      } catch (IOException e) {
        LOG.error("could not read template file in repository", e);
      }
    }
  }

  private RepositoryTemplate mapRepositoryTemplateFromFile(RepositoryService repositoryService, String existingTemplateFile) throws IOException {
    InputStream templateYml = repositoryService.getCatCommand().getStream(existingTemplateFile);
    RepositoryTemplate repositoryTemplate = mapper.readValue(templateYml, RepositoryTemplate.class);
    repositoryTemplate.setNamespaceAndName(repositoryService.getRepository().getNamespaceAndName().toString());
    return repositoryTemplate;
  }

  private Optional<String> templateFileExists(RepositoryService repositoryService) throws IOException {
    if (fileExists(repositoryService, TEMPLATE_YML)) {
      return Optional.of(TEMPLATE_YML);
    } else if (fileExists(repositoryService, TEMPLATE_YAML)) {
      return Optional.of(TEMPLATE_YAML);
    } else {
      return Optional.empty();
    }
  }

  private boolean fileExists(RepositoryService repositoryService, String templateYml) throws IOException {
    return repositoryService.getBrowseCommand().setPath(templateYml).getBrowserResult() != null;
  }
}
