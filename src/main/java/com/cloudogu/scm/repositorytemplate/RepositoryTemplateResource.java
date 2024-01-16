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

import de.otto.edison.hal.HalRepresentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
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
