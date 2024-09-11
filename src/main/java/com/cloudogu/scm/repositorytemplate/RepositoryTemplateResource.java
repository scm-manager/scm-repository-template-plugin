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

import de.otto.edison.hal.HalRepresentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import java.util.Collection;

@OpenAPIDefinition(tags = {
  @Tag(name = "Repository Templates", description = "Repository template plugin related endpoints")
})
@Path("v2/repos/templates")
public class RepositoryTemplateResource {

  private static final String MEDIA_TYPE = VndMediaType.PREFIX + "repository-templates" + VndMediaType.SUFFIX;

  private final RepositoryTemplateCollector collector;
  private final RepositoryTemplateCollectionMapper collectionMapper;

  @Inject
  RepositoryTemplateResource(RepositoryTemplateCollector collector, RepositoryTemplateCollectionMapper collectionMapper) {
    this.collector = collector;
    this.collectionMapper = collectionMapper;
  }

  @GET
  @Path("")
  @Produces(MEDIA_TYPE)
  @Operation(
    summary = "Get repository templates",
    description = "Returns a collection of repository templates.",
    tags = "Repository Templates",
    operationId = "get_repository_templates"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MEDIA_TYPE,
      schema = @Schema(implementation = HalRepresentation.class)
    )
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
  public Response getRepositoryTemplates() {
    Collection<RepositoryTemplate> repositoryTemplates = collector.collect();
    HalRepresentation repoTemplateCollectionDto = collectionMapper.map(repositoryTemplates);
    return Response.ok(repoTemplateCollectionDto).build();
  }
}
