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
    return Optional.of("https://scm-manager.org/plugins/scm-repository-template-plugin/docs/latest/en/usage/");
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
