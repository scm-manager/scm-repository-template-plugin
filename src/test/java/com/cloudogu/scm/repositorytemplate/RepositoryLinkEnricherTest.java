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

import com.google.inject.util.Providers;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
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

@ExtendWith(MockitoExtension.class)
class RepositoryLinkEnricherTest {

    private static final Repository REPOSITORY = new Repository("id-1", "git", "space", "x");

    @Mock
    private RepositoryServiceFactory serviceFactory;

    @Mock
    private HalAppender appender;
    @Mock
    private Subject subject;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RepositoryService service;

    private HalEnricherContext context;

    @InjectMocks
    private RepositoryLinkEnricher enricher;

    @BeforeEach
    void setUp() {
        ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
        scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
        com.google.inject.Provider<ScmPathInfoStore> scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
        enricher = new RepositoryLinkEnricher(scmPathInfoStoreProvider, serviceFactory);
        context = HalEnricherContext.of(REPOSITORY);
    }

    @BeforeEach
    void prepareRepository() {
        REPOSITORY.setId("id-1");
        REPOSITORY.setNamespace("hitchhiker");
        REPOSITORY.setName("HeartOfGold");
    }

    @BeforeEach
    void initSubject() {
        ThreadContext.bind(subject);
    }

    @AfterEach
    void tearDownSubject() {
        ThreadContext.unbindSubject();
    }

    @Test
    void shoudEnrichTemplateLink() {
        when(subject.isPermitted("repository:push:id-1")).thenReturn(true);
        when(serviceFactory.create(REPOSITORY.getId())).thenReturn(service);

        enricher.enrich(context, appender);

        verify(appender).appendLink("template", "https://scm-manager.org/scm/api/v2/template/hitchhiker/HeartOfGold");
    }

    @Test
    void shoudEnrichUntemplateLink() throws IOException {
        FileObject fileObject = new FileObject();
        fileObject.setName("template.yml");
        BrowserResult browserResult = new BrowserResult("revision", fileObject);

        when(subject.isPermitted("repository:push:id-1")).thenReturn(true);
        when(serviceFactory.create(REPOSITORY.getId())).thenReturn(service);
        when(service.getBrowseCommand().setPath("template.yml").getBrowserResult()).thenReturn(browserResult);

        enricher.enrich(context, appender);

        verify(appender).appendLink("untemplate", "https://scm-manager.org/scm/api/v2/untemplate/hitchhiker/HeartOfGold");
    }

    @Test
    void shouldNotEnrichTemplateLinkWhenNotPermitted() {
        enricher.enrich(context, appender);

        verify(appender, never()).appendLink(anyString(), anyString());
    }
}
