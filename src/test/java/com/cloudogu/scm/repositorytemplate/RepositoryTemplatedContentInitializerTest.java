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

import com.fasterxml.jackson.databind.JsonNode;
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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryTemplatedContentInitializerTest {

  private static final Repository TEMPLATE_REPOSITORY = RepositoryTestData.createHeartOfGold();
  private static final Repository TARGET_REPOSITORY = RepositoryTestData.create42Puzzle();

  @Mock
  private RepositoryTemplater templater;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryContentInitializer.InitializerContext context;

  @InjectMocks
  private RepositoryTemplatedContentInitializer initializer;

  @Test
  void shouldInitializeRepository() {
    Map<String, JsonNode> creationContext = new HashMap<>();
    TextNode templateId = new TextNode("hitchhiker/heartOfGold");
    creationContext.put("templateId", templateId);
    when(context.getCreationContext()).thenReturn(creationContext);
    when(repositoryManager.get(new NamespaceAndName("hitchhiker", "heartOfGold"))).thenReturn(TEMPLATE_REPOSITORY);
    when(context.getRepository()).thenReturn(TARGET_REPOSITORY);

    initializer.initialize(context);

    verify(templater).render(TEMPLATE_REPOSITORY, context);
  }
}
