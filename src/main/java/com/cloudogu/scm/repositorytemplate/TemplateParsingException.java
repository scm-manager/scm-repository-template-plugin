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

import sonia.scm.ContextEntry;
import sonia.scm.ExceptionWithContext;

import java.util.Optional;

public class TemplateParsingException extends ExceptionWithContext {

  private static final String CODE = "ErSA4q8eJ1";

  public TemplateParsingException(String file) {
    super(
      ContextEntry.ContextBuilder.entity(RepositoryTemplate.class, file).build(),
      "repository template invalid -> could not parse " + file
    );
  }

  public TemplateParsingException(String file, Exception exception) {
    super(
      ContextEntry.ContextBuilder.entity(RepositoryTemplate.class, file).build(),
      "repository template invalid -> could not parse " + file,
      exception
    );
  }

  @Override
  public Optional<String> getUrl() {
    return Optional.of("https://scm-manager.org/plugins/scm-repository-template-plugin/docs/1.0.x/en/usage/");
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
