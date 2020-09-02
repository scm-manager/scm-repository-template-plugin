package com.cloudogu.scm.repositorytemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.repository.NamespaceAndName;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RepositoryTemplate {
  private NamespaceAndName namespaceAndName;
  private String engine;
  @JsonProperty("files")
  private List<RepositoryTemplateFile> templatedFiles;
}
