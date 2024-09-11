/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.repositorytemplate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RepositoryTemplate {
  private NamespaceAndName namespaceAndName;
  private String repositoryId;
  private String engine;
  private String encoding;
  private List<RepositoryTemplateFile> files;

  public RepositoryTemplate(Repository repository) {
    this(repository.getNamespaceAndName(), repository.getId());
  }

  private RepositoryTemplate(NamespaceAndName namespaceAndName, String repositoryId) {
    this.namespaceAndName = namespaceAndName;
    this.repositoryId = repositoryId;
  }

  public String getTemplateRepository() {
    return namespaceAndName.toString();
  }

  void setRepository(Repository repository) {
    this.namespaceAndName = repository.getNamespaceAndName();
    this.repositoryId = repository.getId();
  }
}
