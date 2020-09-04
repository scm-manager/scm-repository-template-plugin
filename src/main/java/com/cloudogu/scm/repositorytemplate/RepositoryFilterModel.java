package com.cloudogu.scm.repositorytemplate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RepositoryFilterModel {
  private String namespace;
  private String name;
  private String type;
  private String contact;
  private String description;
}
