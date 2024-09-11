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

import com.google.common.collect.ImmutableList;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.RestDispatcher;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryTemplateResourceTest {

  @Mock
  private RepositoryTemplateCollector collector;
  @Mock
  private RepositoryTemplateCollectionMapper collectionMapper;

  @InjectMocks
  private RepositoryTemplateResource resource;

  private RestDispatcher dispatcher;

  @BeforeEach
  void init() {
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
  }

  @Test
  void shouldReturnRepositoryTemplates() throws URISyntaxException, UnsupportedEncodingException {
    Repository repository = RepositoryTestData.createHeartOfGold();
    String namespaceAndName = repository.getNamespaceAndName().toString();
    when(collectionMapper.map(anyCollection())).thenReturn(
      new HalRepresentation(
        Links.emptyLinks(),
        Embedded.embedded("templates", ImmutableList.of(new RepositoryTemplateDto(namespaceAndName))))
    );
    MockHttpRequest request = MockHttpRequest.get("/v2/repos/templates");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(collector).collect();
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString()).contains("\"templateRepository\":\"" + namespaceAndName+ "\"");
  }
}
