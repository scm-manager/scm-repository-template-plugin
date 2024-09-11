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

import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.net.URI;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SubjectAware(value = "trillian")
@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class RepositoryLinkEnricherTest {

  private static final Repository REPOSITORY = new Repository("id-1", "git", "hitchhiker", "HeartOfGold");

  @Mock
  private RepositoryServiceFactory serviceFactory;

  @Mock
  private HalAppender appender;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryService service;

  private HalEnricherContext context;

  @InjectMocks
  private RepositoryLinkEnricher enricher;

  @BeforeEach
  void setUp() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    Provider<ScmPathInfoStore> scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
    enricher = new RepositoryLinkEnricher(scmPathInfoStoreProvider, serviceFactory);
    context = HalEnricherContext.of(REPOSITORY);
  }

  @Test
  @SubjectAware(permissions = "repository:push:id-1")
  void shouldEnrichTemplateLink() {
    when(serviceFactory.create(REPOSITORY.getId())).thenReturn(service);

    enricher.enrich(context, appender);

    verify(appender).appendLink("template", "https://scm-manager.org/scm/api/v2/template/repo/hitchhiker/HeartOfGold/template");
  }

  @Test
  @SubjectAware(permissions = "repository:push:id-1")
  void shouldEnrichUntemplateLink() throws IOException {
    FileObject fileObject = new FileObject();
    fileObject.setName("template.yml");
    BrowserResult browserResult = new BrowserResult("revision", fileObject);

    when(serviceFactory.create(REPOSITORY.getId())).thenReturn(service);
    when(service.getBrowseCommand().setPath("template.yml").getBrowserResult()).thenReturn(browserResult);

    enricher.enrich(context, appender);

    verify(appender).appendLink("untemplate", "https://scm-manager.org/scm/api/v2/template/repo/hitchhiker/HeartOfGold/untemplate");
  }

  @Test
  void shouldNotEnrichTemplateLinkWhenNotPermitted() {
    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(anyString(), anyString());
  }
}
