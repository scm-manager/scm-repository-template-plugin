package com.cloudogu.scm.repositorytemplate;

import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.web.RestDispatcher;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RepositoryTemplateResourceTest {

  @Mock
  private RepositoryTemplateCollector collector;

  @InjectMocks
  private RepositoryTemplateResource resource;

  private RestDispatcher dispatcher;

  @BeforeEach
  void init() {
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
  }

  @Test
  void shouldReturnRepositoryTemplates() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/v2/repos/templates");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(collector).collect();
    assertThat(response.getStatus()).isEqualTo(200);
  }
}
