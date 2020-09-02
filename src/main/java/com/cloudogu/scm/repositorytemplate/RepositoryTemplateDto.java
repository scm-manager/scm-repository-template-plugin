package com.cloudogu.scm.repositorytemplate;

import de.otto.edison.hal.HalRepresentation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.repository.NamespaceAndName;

@Getter
@Setter
@NoArgsConstructor
public class RepositoryTemplateDto extends HalRepresentation {
  private NamespaceAndName namespaceAndName;
}
