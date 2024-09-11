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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import java.io.IOException;

@Path("v2/template/repo/")
public class RepositoryTemplateRepositoryResource {

  private final RepositoryTemplateRepositoryService service;

  @Inject
  public RepositoryTemplateRepositoryResource(RepositoryTemplateRepositoryService service) {
    this.service = service;
  }

  @POST
  @Path("{namespace}/{name}/template")
  @Operation(
    summary = "Mark repository as template",
    description = "Mark the repository as a repository template",
    tags = "Repository Templates",
    operationId = "repository_template_use_repository"
  )
  @ApiResponse(
    responseCode = "204",
    description = "no content"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void templateRepository(@PathParam("namespace") String namespace,
                                 @PathParam("name") String name) throws IOException {
    service.templateRepository(new NamespaceAndName(namespace, name));
  }

  @POST
  @Path("{namespace}/{name}/untemplate")
  @Operation(
    summary = "Unmark repository as template",
    description = "Unmark the repository as a template",
    tags = "Repository Templates",
    operationId = "repository_template_unuse_repository"
  )
  @ApiResponse(
    responseCode = "204",
    description = "no content"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void untemplateRepository(@PathParam("namespace") String namespace,
                                   @PathParam("name") String name) {
    service.untemplateRepository(new NamespaceAndName(namespace, name));
  }
}
