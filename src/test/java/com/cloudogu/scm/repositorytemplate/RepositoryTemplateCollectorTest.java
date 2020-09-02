package com.cloudogu.scm.repositorytemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class RepositoryTemplateCollectorTest {

  @InjectMocks
  RepositoryTemplateCollector collector;


  @Test
  void shouldReturnEmptyList() {
    List<RepositoryTemplate> templates = collector.collect();

    assertThat(templates).isInstanceOf(List.class);
    assertThat(templates).hasSize(0);
  }

  @Test
  void shouldCollectRepositoryTemplates() {

  }
}
