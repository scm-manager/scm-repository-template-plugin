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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Links.linkingTo;

public class RepositoryTemplateCollectionMapper {

  private final RepositoryTemplateMapper mapper;
  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public RepositoryTemplateCollectionMapper(Provider<ScmPathInfoStore> scmPathInfoStore, RepositoryTemplateMapper mapper) {
    this.scmPathInfoStore = scmPathInfoStore;
    this.mapper = mapper;
  }

  public HalRepresentation map(Collection<RepositoryTemplate> templates) {
    List<RepositoryTemplateDto> dtos = templates.stream().map(mapper::map).collect(Collectors.toList());

    Links.Builder builder = linkingTo();

    builder.self(selfLink());
    return new HalRepresentation(builder.build(), Embedded.embedded("templates", dtos));
  }

  private String selfLink() {
    return new LinkBuilder(scmPathInfoStore.get().get(), RepositoryTemplateResource.class)
      .method("getRepositoryTemplates")
      .parameters()
      .href();
  }
}
