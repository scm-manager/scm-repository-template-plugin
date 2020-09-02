package com.cloudogu.scm.repositorytemplate;

import de.otto.edison.hal.HalRepresentation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RepositoryTemplatesDto extends HalRepresentation {
  private List<RepositoryTemplateDto> repositoryTemplates;
}
