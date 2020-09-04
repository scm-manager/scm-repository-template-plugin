package com.cloudogu.scm.repositorytemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryContentInitializer;
import sonia.scm.repository.RepositoryManager;

import javax.inject.Inject;
import java.util.Optional;

@Extension
public class RepositoryTemplatedContentInitializer implements RepositoryContentInitializer {

  private final RepositoryManager repositoryManager;
  private final RepositoryTemplater templater;

  private final ObjectMapper mapper = new ObjectMapper();

  @Inject
  public RepositoryTemplatedContentInitializer(RepositoryManager repositoryManager, RepositoryTemplater templater) {
    this.repositoryManager = repositoryManager;
    this.templater = templater;
  }

  @Override
  public void initialize(InitializerContext context) {
    JsonNode repositoryModel = context.getCreationContext().get("templateId");
    TemplateRepository model = mapper.convertValue(repositoryModel, TemplateRepository.class);
    String[] splitRepository = model.getTemplateId().split("/");
    Repository templateRepository = repositoryManager.get(new NamespaceAndName(splitRepository[0], splitRepository[1]));
    Repository targetRepository = context.getRepository();
    templater.render(templateRepository, targetRepository);
  }

  @Override
  public Optional<Class<?>> getType() {
    return Optional.of(RepositoryFilterModel.class);
  }

  @Getter
  private static class TemplateRepository {
    @JsonProperty
    private String templateId;
  }
}
