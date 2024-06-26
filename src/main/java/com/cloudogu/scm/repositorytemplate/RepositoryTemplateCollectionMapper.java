/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
