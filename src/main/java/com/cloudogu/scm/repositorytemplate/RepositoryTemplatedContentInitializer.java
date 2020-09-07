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

import com.fasterxml.jackson.databind.node.TextNode;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryContentInitializer;
import sonia.scm.repository.RepositoryManager;

import javax.inject.Inject;
import java.util.Optional;

@Extension
public class RepositoryTemplatedContentInitializer implements RepositoryContentInitializer {

  private final RepositoryManager repositoryManager;
  private final RepositoryTemplater templater;

  @Inject
  public RepositoryTemplatedContentInitializer(RepositoryManager repositoryManager, RepositoryTemplater templater) {
    this.repositoryManager = repositoryManager;
    this.templater = templater;
  }

  @Override
  public void initialize(InitializerContext context) {
    TextNode repositoryModel = (TextNode) context.getCreationContext().get("templateId");
    if (repositoryModel != null) {
      String[] splitRepository = repositoryModel.asText().split("/");
      Repository templateRepository = repositoryManager.get(new NamespaceAndName(splitRepository[0], splitRepository[1]));
      Repository targetRepository = context.getRepository();
      templater.render(templateRepository, targetRepository, context);
    }
  }

  @Override
  public Optional<Class<?>> getType() {
    return Optional.of(RepositoryFilterModel.class);
  }
}
