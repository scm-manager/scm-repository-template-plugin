package com.cloudogu.scm.repositorytemplate;

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
  private List<String> templatedFiles;
}
