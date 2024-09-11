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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.ModifyCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryTemplateRepositoryServiceTest {

  private final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  @InjectMocks
  private RepositoryTemplateRepositoryService templateService;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService repositoryService;
  @Mock(answer = Answers.RETURNS_SELF)
  private ModifyCommandBuilder modifyCommand;
  @Mock
  private ModifyCommandBuilder.WithOverwriteFlagContentLoader withOverwriteFlagContentLoader;

  @BeforeEach
  void setUpModifyCommand() {
    when(serviceFactory.create(REPOSITORY.getNamespaceAndName())).thenReturn(repositoryService);
    when(repositoryService.getModifyCommand()).thenReturn(modifyCommand);
  }

  @Test
  void shouldCreateTemplateFile() throws IOException {
    when(modifyCommand.createFile(any())).thenReturn(withOverwriteFlagContentLoader);
    when(withOverwriteFlagContentLoader.withData(any(InputStream.class))).thenReturn(modifyCommand);

    templateService.templateRepository(REPOSITORY.getNamespaceAndName());

    verify(modifyCommand).setCommitMessage("Create template from repository");
    verify(modifyCommand).createFile("template.yml");
    verify(modifyCommand).execute();
    verify(repositoryService).close();
  }

  @Test
  void shouldRemoveTemplateFile() {
    templateService.untemplateRepository(REPOSITORY.getNamespaceAndName());

    verify(modifyCommand).setCommitMessage("Delete template from repository");
    verify(modifyCommand, never()).createFile(any());
    verify(modifyCommand).execute();
    verify(repositoryService).close();
  }
}
