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
