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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.cache.MapCacheManager;
import sonia.scm.repository.Added;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.CatCommandBuilder;
import sonia.scm.repository.api.HookChangesetBuilder;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.ModificationsCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryTemplateCollectorTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  private static final String CACHE_NAME = "sonia.cache.repository.templates";

  @Mock
  private Subject subject;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService repositoryService;
  @Mock(answer = Answers.RETURNS_SELF)
  private BrowseCommandBuilder browseCommandBuilder;
  @Mock(answer = Answers.RETURNS_SELF)
  private CatCommandBuilder catCommandBuilder;
  @Mock(answer = Answers.RETURNS_SELF)
  private ModificationsCommandBuilder modificationsCommandBuilder;
  @Mock
  private HookContext hookContext;
  @Mock
  private HookChangesetBuilder changesetBuilder;

  private CacheManager cacheManager;

  private RepositoryTemplateCollector collector;

  @BeforeEach
  void setup() {
    AdministrationContext administrationContext = new AdministrationContext() {

      @Override
      public void runAsAdmin(PrivilegedAction action) {
        action.run();
      }

      @Override
      public void runAsAdmin(Class<? extends PrivilegedAction> actionClass) {
        try {
          runAsAdmin(actionClass.newInstance());
        } catch (IllegalAccessException | InstantiationException ex) {
          throw Throwables.propagate(ex);
        }
      }
    };

    cacheManager = new MapCacheManager();
    collector = new RepositoryTemplateCollector(administrationContext, serviceFactory, repositoryManager, cacheManager);

    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldReturnEmptyCollection() {
    Collection<RepositoryTemplate> templates = collector.collect();

    assertThat(templates).isInstanceOf(Collection.class).isEmpty();
  }

  @Test
  void shouldCollectRepositoryTemplates() throws IOException {
    when(subject.isPermitted("repository:read:" + REPOSITORY.getId())).thenReturn(true);

    mockRepositoryServices();

    Collection<RepositoryTemplate> templates = collector.collect();

    assertThat(templates).hasSize(1);
    RepositoryTemplate template = templates.iterator().next();
    assertThat(template.getNamespaceAndName()).isEqualTo(REPOSITORY.getNamespaceAndName().toString());
  }

  @Test
  void shouldNotCollectRepositoryTemplatesWithoutPermission() throws IOException {
    mockRepositoryServices();

    Collection<RepositoryTemplate> templates = collector.collect();

    assertThat(templates).isEmpty();
  }

  @Test
  void shouldCollectRepositoryTemplatesFromCache() {
    when(subject.isPermitted("repository:read:" + REPOSITORY.getId())).thenReturn(true);
    when(repositoryManager.get(new NamespaceAndName(REPOSITORY.getNamespace(), REPOSITORY.getName()))).thenReturn(REPOSITORY);

    List<RepositoryTemplate> repositoryTemplates = createRepoTemplateList(
      "hitchhiker",
      new RepositoryTemplateFile(".gitignore", false),
      new RepositoryTemplateFile("Jenkinsfile", true)
    );
    cacheManager.getCache(CACHE_NAME).put("templates", repositoryTemplates);

    Collection<RepositoryTemplate> templates = collector.collect();

    assertThat(templates).hasSize(1);
    RepositoryTemplate template = templates.iterator().next();
    assertThat(template.getEngine()).isEqualTo("hitchhiker");
    assertThat(template.getFiles().get(0).getPath()).isEqualTo(".gitignore");
    assertThat(template.getFiles().get(1).getPath()).isEqualTo("Jenkinsfile");
    assertThat(template.getNamespaceAndName()).isEqualTo(REPOSITORY.getNamespaceAndName().toString());
  }

  @Test
  void shouldNotCollectRepositoryTemplatesFromCacheWithoutPermission() {
    List<RepositoryTemplate> repositoryTemplates =
      createRepoTemplateList("hitchhiker", new RepositoryTemplateFile(".gitignore", false));

    cacheManager.getCache(CACHE_NAME).put("templates", repositoryTemplates);
    when(repositoryManager.get(new NamespaceAndName(REPOSITORY.getNamespace(), REPOSITORY.getName()))).thenReturn(REPOSITORY);

    Collection<RepositoryTemplate> templates = collector.collect();

    assertThat(templates).isEmpty();
  }

  @Test
  void shouldClearCacheIfTemplateFileWasEffected() throws IOException {
    Cache<String, List<RepositoryTemplate>> cache = cacheManager.getCache("sonia.cache.repository.templates");

    cache.put("templates", Collections.emptyList());

    when(serviceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    when(hookContext.getChangesetProvider()).thenReturn(changesetBuilder);
    when(changesetBuilder.getChangesets()).thenReturn(Arrays.asList(new Changeset()));
    when(repositoryService.getModificationsCommand()).thenReturn(modificationsCommandBuilder);
    when(modificationsCommandBuilder.getModifications()).thenReturn(new Modifications("1", new Added("template.yml")));

    assertThat(cache.size()).isEqualTo(1);

    collector.onEvent(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.POST_RECEIVE)));

    assertThat(cache.size()).isZero();
  }

  @Test
  void shouldClearCacheIfRepositoryWasDeleted() {
    Cache<String, List<RepositoryTemplate>> cache = cacheManager.getCache("sonia.cache.repository.templates");
    cache.put("templates", Collections.emptyList());

    assertThat(cache.size()).isEqualTo(1);

    collector.onEvent(new RepositoryEvent(HandlerEventType.DELETE, REPOSITORY));

    assertThat(cache.size()).isZero();
  }


  @Test
  void shouldClearCacheIfRepositoryWasRenamed() {
    Cache<String, List<RepositoryTemplate>> cache = cacheManager.getCache("sonia.cache.repository.templates");
    cache.put("templates", Collections.emptyList());

    assertThat(cache.size()).isEqualTo(1);

    collector.onEvent(new RepositoryEvent(HandlerEventType.MODIFY, REPOSITORY, RepositoryTestData.create42Puzzle()));

    assertThat(cache.size()).isZero();
  }

  @Test
  void shouldNotClearCacheIfTemplateFileWasNotEffected() throws IOException {
    Cache<String, List<RepositoryTemplate>> cache = cacheManager.getCache("sonia.cache.repository.templates");

    cache.put("templates", Collections.emptyList());

    when(serviceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    when(hookContext.getChangesetProvider()).thenReturn(changesetBuilder);
    when(changesetBuilder.getChangesets()).thenReturn(Arrays.asList(new Changeset()));
    when(repositoryService.getModificationsCommand()).thenReturn(modificationsCommandBuilder);
    when(modificationsCommandBuilder.getModifications()).thenReturn(new Modifications("1", new Added("Jenkinsfile")));

    assertThat(cache.size()).isEqualTo(1);

    collector.onEvent(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.POST_RECEIVE)));

    assertThat(cache.size()).isEqualTo(1);
  }

  private List<RepositoryTemplate> createRepoTemplateList(String engine, RepositoryTemplateFile... templateFiles) {
    RepositoryTemplate repositoryTemplate = new RepositoryTemplate();
    repositoryTemplate.setNamespaceAndName(REPOSITORY.getNamespaceAndName().toString());
    repositoryTemplate.setEngine(engine);
    repositoryTemplate.setFiles(Arrays.asList(templateFiles.clone()));
    List<RepositoryTemplate> repositoryTemplates = new ArrayList<>();
    repositoryTemplates.add(repositoryTemplate);
    return repositoryTemplates;
  }

  private void mockRepositoryServices() throws IOException {
    BufferedInputStream content = (BufferedInputStream) Resources.getResource("com/cloudogu/scm/repositorytemplate/template.yml").getContent();
    when(repositoryManager.getAll()).thenReturn(ImmutableSet.of(REPOSITORY));
    when(repositoryManager.get(new NamespaceAndName(REPOSITORY.getNamespace(), REPOSITORY.getName()))).thenReturn(REPOSITORY);
    when(serviceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    when(repositoryService.getBrowseCommand()).thenReturn(browseCommandBuilder);
    when(browseCommandBuilder.getBrowserResult()).thenReturn(new BrowserResult());
    lenient().when(catCommandBuilder.getStream(any())).thenReturn(content);
    lenient().when(repositoryService.getCatCommand()).thenReturn(catCommandBuilder);
    lenient().when(repositoryService.getRepository()).thenReturn(REPOSITORY);
  }
}
