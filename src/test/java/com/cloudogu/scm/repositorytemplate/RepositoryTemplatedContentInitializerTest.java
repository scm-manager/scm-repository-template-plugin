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

import java.util.Optional;

import static com.cloudogu.scm.repositorytemplate.RepositoryTemplatedContentInitializer.TemplateContext;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryTemplatedContentInitializerTest {

  private static final Repository TEMPLATE_REPOSITORY = RepositoryTestData.createHeartOfGold();

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
    TemplateContext templateContext = new TemplateContext();
    templateContext.setTemplateId("hitchhiker/heartOfGold");
    when(context.getEntry("repository-template", TemplateContext.class)).thenReturn(Optional.of(templateContext));
    when(repositoryManager.get(new NamespaceAndName("hitchhiker", "heartOfGold"))).thenReturn(TEMPLATE_REPOSITORY);

    initializer.initialize(context);

    verify(templater).render(TEMPLATE_REPOSITORY, context);
  }
}
