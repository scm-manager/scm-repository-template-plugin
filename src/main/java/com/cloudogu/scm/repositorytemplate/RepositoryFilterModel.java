package com.cloudogu.scm.repositorytemplate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.repository.Repository;

@Getter
@Setter
@NoArgsConstructor
public class RepositoryFilterModel {
  private Repository repository;
}
