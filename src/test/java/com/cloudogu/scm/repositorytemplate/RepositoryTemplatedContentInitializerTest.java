package com.cloudogu.scm.repositorytemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryContentInitializer;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryTemplatedContentInitializerTest {

  private static final Repository TEMPLATE_REPOSITORY = RepositoryTestData.createHeartOfGold();
  private static final Repository TARGET_REPOSITORY = RepositoryTestData.create42Puzzle();

  private static final ObjectMapper mapper = new ObjectMapper();

  @Mock
  private RepositoryTemplater templater;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryContentInitializer.InitializerContext context;


  @InjectMocks
  private RepositoryTemplatedContentInitializer initializer;

  @Test
  void shouldInitializeRepository() throws JsonProcessingException {
    Map<String, JsonNode> creationContext = new HashMap<>();
    JsonNode templateId = mapper.readTree("{\"templateId\":\"hitchhiker/heartOfGold\"}");
    creationContext.put("templateId", templateId);
    when(context.getCreationContext()).thenReturn(creationContext);
    when(repositoryManager.get(new NamespaceAndName("hitchhiker", "heartOfGold"))).thenReturn(TEMPLATE_REPOSITORY);
    when(context.getRepository()).thenReturn(TARGET_REPOSITORY);

    initializer.initialize(context);

    verify(templater).render(TEMPLATE_REPOSITORY, TARGET_REPOSITORY);
  }
}
