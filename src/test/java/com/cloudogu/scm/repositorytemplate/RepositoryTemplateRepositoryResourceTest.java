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

import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.RestDispatcher;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RepositoryTemplateRepositoryResourceTest {

  Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private RepositoryTemplateRepositoryService service;

  private RestDispatcher dispatcher;

  @BeforeEach
  void init() {
    RepositoryTemplateRepositoryResource resource = new RepositoryTemplateRepositoryResource(service);

    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
  }


  @Test
  void shouldTemplateRepository() throws URISyntaxException, IOException {
    MockHttpRequest request = MockHttpRequest.post("/v2/template/repo/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/template");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(service).templateRepository(new NamespaceAndName(REPOSITORY.getNamespace(), REPOSITORY.getName()));
    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  void shouldUntemplateRepository() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.post("/v2/template/repo/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName() + "/untemplate");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(service).untemplateRepository(new NamespaceAndName(REPOSITORY.getNamespace(), REPOSITORY.getName()));
    assertThat(response.getStatus()).isEqualTo(204);
  }


}
