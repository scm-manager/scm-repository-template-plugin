package com.cloudogu.scm.repositorytemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class RepositoryTemplateCollector {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryTemplateCollector.class);

  public static final String TEMPLATE_YML = "template.yml";
  public static final String TEMPLATE_YAML = "template.yaml";

  private final RepositoryServiceFactory serviceFactory;
  private final RepositoryManager repositoryManager;

  private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

  @Inject
  public RepositoryTemplateCollector(RepositoryServiceFactory serviceFactory, RepositoryManager repositoryManager) {
    this.serviceFactory = serviceFactory;
    this.repositoryManager = repositoryManager;
  }

  public Collection<RepositoryTemplate> collect() {
    Collection<RepositoryTemplate> repositoryTemplates = new ArrayList<>();

    Collection<Repository> repositories = repositoryManager.getAll();
    for (Repository repository : repositories) {
      try (RepositoryService repositoryService = serviceFactory.create(repository)) {
        Optional<String> existingTemplateFile = templateFileExists(repositoryService);
        if (existingTemplateFile.isPresent()) {
          repositoryTemplates.add(mapRepositoryTemplateFromFile(repositoryService, existingTemplateFile));
        }

      } catch (IOException e) {
        LOG.error("could not read template file in repository", e);
      }
    }

    return repositoryTemplates;
  }

  private RepositoryTemplate mapRepositoryTemplateFromFile(RepositoryService repositoryService, Optional<String> existingTemplateFile) throws IOException {
    InputStream templateYml = repositoryService.getCatCommand().getStream(existingTemplateFile.get());
    RepositoryTemplate repositoryTemplate = mapper.readValue(templateYml, RepositoryTemplate.class);
    repositoryTemplate.setNamespaceAndName(repositoryService.getRepository().getNamespaceAndName());
    return repositoryTemplate;
  }

  private Optional<String> templateFileExists(RepositoryService repositoryService) throws IOException {
    if (fileExists(repositoryService, TEMPLATE_YML)) {
      return Optional.of(TEMPLATE_YML);
    } else if (fileExists(repositoryService, TEMPLATE_YAML)) {
      return Optional.of(TEMPLATE_YAML);
    } else {
      return Optional.empty();
    }
  }

  private boolean fileExists(RepositoryService repositoryService, String templateYml) throws IOException {
    return repositoryService.getBrowseCommand().setPath(templateYml).getBrowserResult() != null;
  }
}
