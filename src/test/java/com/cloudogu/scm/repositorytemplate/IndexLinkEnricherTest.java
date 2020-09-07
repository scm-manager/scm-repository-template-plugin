package com.cloudogu.scm.repositorytemplate;

import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexLinkEnricherTest {

  @Mock
  private HalAppender appender;
  @Mock
  private Subject subject;

  private HalEnricherContext context;

  @InjectMocks
  private IndexLinkEnricher enricher;

  @BeforeEach
  void setUp() {
    ThreadContext.bind(subject);
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    Provider<ScmPathInfoStore> scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
    enricher = new IndexLinkEnricher(scmPathInfoStoreProvider);
    context = HalEnricherContext.of();
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldNotAppendRepositoryTemplatesLink() {
    enricher.enrich(context, appender);

    verify(appender, never()).appendLink("repositoryTemplates", "https://scm-manager.org/scm/api/v2/repos/templates");
  }

  @Test
  void shouldAppendRepositoryTemplatesLink() {
    when(subject.isPermitted("repository:create")).thenReturn(true);

    enricher.enrich(context, appender);

    verify(appender).appendLink("repositoryTemplates", "https://scm-manager.org/scm/api/v2/repos/templates");
  }

}
