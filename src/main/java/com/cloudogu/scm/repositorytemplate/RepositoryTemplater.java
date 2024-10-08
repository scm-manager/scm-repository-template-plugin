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

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import sonia.scm.ContextEntry;
import sonia.scm.NotFoundException;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryContentInitializer.InitializerContext;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Optional;

public class RepositoryTemplater {

  private final TemplateEngineFactory engineFactory;
  private final RepositoryServiceFactory repositoryServiceFactory;

  // SnakeYaml cannot find the classes without the right classloader
  private final Yaml yaml = new Yaml(new CustomClassLoaderConstructor(RepositoryTemplateCollector.class.getClassLoader(), new LoaderOptions()));
  private InitializerContext context;

  @Inject
  public RepositoryTemplater(TemplateEngineFactory engineFactory, RepositoryServiceFactory repositoryServiceFactory) {
    this.engineFactory = engineFactory;
    this.repositoryServiceFactory = repositoryServiceFactory;
  }

  public void render(Repository template, InitializerContext context) {
    this.context = context;
    try (RepositoryService templateService = repositoryServiceFactory.create(template)) {
      render(templateService);
    } catch (IOException e) {
      throw new InternalRepositoryException(
        ContextEntry.ContextBuilder.entity(Repository.class, template.getId()),
        "could not read files from template repository",
        e
      );
    }
  }

  private void render(RepositoryService templateService) throws IOException {
    RepositoryTemplate repositoryTemplate = parseTemplateConfig(templateService);
    TemplateEngine engine = setupTemplateEngine(repositoryTemplate);
    String encoding = !Strings.isNullOrEmpty(repositoryTemplate.getEncoding()) ? repositoryTemplate.getEncoding() : "UTF-8";
    TemplateFilter templateFilter = new TemplateFilter(new TemplateFilterModel(context.getRepository()), engine, encoding);

    for (RepositoryTemplateFile templateFile : repositoryTemplate.getFiles()) {
      String filePath = removeLeadingSlashOnFilepath(templateFile);
      BrowserResult result = templateService.getBrowseCommand().setPath(filePath).setRecursive(true).setLimit(10000000).getBrowserResult();
      copyFileRecursively(templateService, templateFilter, templateFile, result.getFile());
    }
  }

  private String removeLeadingSlashOnFilepath(RepositoryTemplateFile templateFile) {
    String filePath = templateFile.getPath();
    if (templateFile.getPath().startsWith("/")) {
      filePath = templateFile.getPath().substring(1);
    }
    return filePath;
  }

  private void copyFileRecursively(RepositoryService templateService, TemplateFilter filter, RepositoryTemplateFile templateFile, FileObject file) throws IOException {
    if (file.isDirectory()) {
      for (FileObject child : file.getChildren()) {
        BrowserResult result = templateService.getBrowseCommand().setPath(child.getPath()).setRecursive(true).setLimit(10000000).getBrowserResult();
        copyFileRecursively(templateService, filter, templateFile, result.getFile());
      }
    } else {
      copyToTargetRepository(templateService, filter, templateFile, file.getPath());
    }
  }

  private void copyToTargetRepository(RepositoryService templateService, TemplateFilter filter, RepositoryTemplateFile templateFile, String filePath) throws IOException {
    if (filePath.equals(RepositoryTemplateFinder.TEMPLATE_YML) || filePath.equals(RepositoryTemplateFinder.TEMPLATE_YAML)) {
      return;
    }
    try (InputStream content = templateService.getCatCommand().getStream(filePath)) {
      if (templateFile.isFiltered()) {
        copyFileTemplated(templateService, filter, content, filePath);
      } else {
        copyFile(templateService, content, filePath);
      }
    }
  }

  private void copyFileTemplated(RepositoryService templateService, TemplateFilter templateFilter, InputStream content, String filepath) throws IOException {
    InputStream templatedContent = new ByteArrayInputStream(templateFilter.filter(content, filepath).toByteArray());
    copyFile(templateService, templatedContent, filepath);
  }

  private void copyFile(RepositoryService templateService, InputStream content, String filepath) throws IOException {
    context.createWithDefaultPath(
        filepath,
        shouldUseDefaultPath(templateService.getRepository(), context.getRepository())
      ).from(content);
  }

  private boolean shouldUseDefaultPath(Repository template, Repository target) {
    return !(template.getType().equals("svn") && target.getType().equals("svn"));
  }

  private TemplateEngine setupTemplateEngine(RepositoryTemplate repositoryTemplate) {
    String configEngine = repositoryTemplate.getEngine() != null ? repositoryTemplate.getEngine() : "mustache";
    TemplateEngine engine = engineFactory.getEngine(configEngine);
    if (engine == null) {
      throw NotFoundException.notFound(ContextEntry.ContextBuilder.entity(TemplateEngine.class, configEngine));
    }
    return engine;
  }

  private RepositoryTemplate parseTemplateConfig(RepositoryService repositoryService) throws IOException {
    Optional<String> templateFile = RepositoryTemplateFinder.templateFileExists(repositoryService);
    Repository repository = repositoryService.getRepository();
    if (!templateFile.isPresent()) {
      throw NotFoundException.notFound(
        ContextEntry.ContextBuilder.entity(RepositoryTemplate.class, "template file").in(repository)
      );
    }
    try (InputStream templateYml = repositoryService.getCatCommand().getStream(templateFile.get())) {
      RepositoryTemplate repositoryTemplate = unmarshallRepositoryTemplate(templateFile.get(), templateYml);
      repositoryTemplate.setRepository(repository);
      if (repositoryTemplate.getFiles() == null) {
        repositoryTemplate.setFiles(Collections.emptyList());
      }
      return repositoryTemplate;
    }
  }

  private RepositoryTemplate unmarshallRepositoryTemplate(String templateFile, InputStream templateYml) {

    try {
      RepositoryTemplate repositoryTemplate = yaml.loadAs(templateYml, RepositoryTemplate.class);
      if (repositoryTemplate == null) {
        throw new TemplateParsingException(templateFile);
      }
      return repositoryTemplate;
    } catch (YAMLException ex) {
      throw new TemplateParsingException(templateFile, ex);
    }
  }

  static class TemplateFilter {
    private final TemplateFilterModel model;
    private final TemplateEngine engine;
    private final String encoding;

    private TemplateFilter(TemplateFilterModel model, TemplateEngine engine, String encoding) {
      this.model = model;
      this.engine = engine;
      this.encoding = encoding;
    }

    private ByteArrayOutputStream filter(InputStream inputStream, String filePath) {
      try (Reader content = new InputStreamReader(inputStream)) {
        Template template = engine.getTemplate(filePath, content);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, Charset.forName(encoding));
        template.execute(writer, model);
        writer.flush();
        return baos;
      } catch (IOException ex) {
        throw new TemplateRenderingException(filePath, ex);
      }
    }
  }

  @Getter
  @AllArgsConstructor
  static class TemplateFilterModel {
    private final Repository repository;
  }
}
