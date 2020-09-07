package com.cloudogu.scm.repositorytemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.web.JsonEnricherBase;
import sonia.scm.web.JsonEnricherContext;

import javax.inject.Inject;
import javax.inject.Provider;

import static java.util.Collections.singletonMap;
import static sonia.scm.web.VndMediaType.INDEX;

@Extension
public class RepositoryTemplateIndexLinkEnricher extends JsonEnricherBase {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public RepositoryTemplateIndexLinkEnricher(ObjectMapper objectMapper, Provider<ScmPathInfoStore> scmPathInfoStore) {
    super(objectMapper);
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  public void enrich(JsonEnricherContext context) {
    if (resultHasMediaType(INDEX, context) && ConfigurationPermissions.read(RepositoryPermissions.ACTION_READ).isPermitted()) {
      String repositoryTemplatesUrl = new LinkBuilder(scmPathInfoStore.get().get(), RepositoryTemplateResource.class)
        .method("getRepositoryTemplates")
        .parameters()
        .href();

      JsonNode gitConfigRefNode = createObject(singletonMap("href", value(repositoryTemplatesUrl)));

      addPropertyNode(context.getResponseEntity().get("_links"), "repositoryTemplates", gitConfigRefNode);
    }
  }
}

