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
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RepositoryTemplateMapperTest {

  private final RepositoryTemplateMapper mapper = new RepositoryTemplateMapperImpl();

  @Test
  void shouldMapRepoTemplateToDto() {
    Repository repository = RepositoryTestData.create42Puzzle();
    RepositoryTemplate repositoryTemplate = new RepositoryTemplate(repository);

    RepositoryTemplateDto dto = mapper.map(repositoryTemplate);

    assertThat(dto.getTemplateRepository()).isEqualTo(repository.getNamespaceAndName().toString());
  }
}
