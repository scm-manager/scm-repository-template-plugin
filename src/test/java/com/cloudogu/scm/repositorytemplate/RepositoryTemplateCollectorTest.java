package com.cloudogu.scm.repositorytemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.CatCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryTemplateCollectorTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private ObjectMapper objectMapper;
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

  @InjectMocks
  private RepositoryTemplateCollector collector;

  @Test
  void shouldReturnEmptyCollection() {
    Collection<RepositoryTemplate> templates = collector.collect();

    assertThat(templates).isInstanceOf(Collection.class);
    assertThat(templates).hasSize(0);
  }

  @Test
  void shouldCollectRepositoryTemplates() throws IOException {
    when(repositoryManager.getAll()).thenReturn(ImmutableSet.of(REPOSITORY));
    when(serviceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    when(repositoryService.getBrowseCommand()).thenReturn(browseCommandBuilder);
    when(browseCommandBuilder.getBrowserResult()).thenReturn(new BrowserResult());
    when(repositoryService.getCatCommand()).thenReturn(catCommandBuilder);
    when(repositoryService.getRepository()).thenReturn(REPOSITORY);

    BufferedInputStream content = (BufferedInputStream) Resources.getResource("com/cloudogu/scm/repositorytemplate/template.yml").getContent();
    when(catCommandBuilder.getStream(any())).thenReturn(content);

    Collection<RepositoryTemplate> templates = collector.collect();

    assertThat(templates).hasSize(1);
    RepositoryTemplate template = templates.iterator().next();
    assertThat(template.getEngine()).isEqualTo("mustache");
    assertThat(template.getTemplatedFiles().get(0).getName()).isEqualTo("README.md");
    assertThat(template.getNamespaceAndName()).isEqualTo(REPOSITORY.getNamespaceAndName());
  }
}
