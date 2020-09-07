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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import sonia.scm.ContextEntry;
import sonia.scm.NotFoundException;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

public class RepositoryTemplater {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryTemplater.class);

  private final TemplateEngineFactory engineFactory;
  private final RepositoryServiceFactory repositoryServiceFactory;

  // SnakeYaml cannot find the classes without the right classloader
  private final Yaml yaml = new Yaml(new CustomClassLoaderConstructor(RepositoryTemplateCollector.class.getClassLoader()));

  @Inject
  public RepositoryTemplater(TemplateEngineFactory engineFactory, RepositoryServiceFactory repositoryServiceFactory) {
    this.engineFactory = engineFactory;
    this.repositoryServiceFactory = repositoryServiceFactory;
  }

  public void render(Repository template, Repository target) {
    try (
      RepositoryService templateService = repositoryServiceFactory.create(template);
      RepositoryService targetService = repositoryServiceFactory.create(target)
    ) {
      render(templateService, targetService);
    } catch (IOException e) {
      LOG.error("could not use template on new repository", e);
    }
  }

  private void render(RepositoryService templateService, RepositoryService targetService) throws IOException {
    RepositoryTemplate repositoryTemplate = parseTemplateConfig(templateService);
    TemplateEngine engine = setupTemplateEngine(repositoryTemplate);

    for (RepositoryTemplateFile templateFile : repositoryTemplate.getFiles()) {
      BrowserResult result = templateService.getBrowseCommand().setPath(templateFile.getName()).setRecursive(true).getBrowserResult();
      copyFileRecursively(templateService, targetService, engine, templateFile, result.getFile());
    }
  }

  private void copyFileRecursively(RepositoryService templateService, RepositoryService targetService, TemplateEngine engine, RepositoryTemplateFile templateFile, FileObject file) throws IOException {
    if (file.isDirectory()) {
      for (FileObject child : file.getChildren()) {
        copyFileRecursively(templateService, targetService, engine, templateFile, child);
      }
    } else {
      copyToTargetRepository(templateService, targetService, engine, templateFile, file.getPath());
    }
  }

  private void copyToTargetRepository(RepositoryService templateService, RepositoryService targetService, TemplateEngine engine, RepositoryTemplateFile templateFile, String filePath) throws IOException {
    if (templateFile.isFiltered()) {
      copyFile(templateService, targetService, filePath, Optional.of(new TemplateFilter(targetService, engine, filePath)));
    } else {
      copyFile(templateService, targetService, filePath, Optional.empty());
    }
  }

  private void copyFile(RepositoryService templateRepositoryService, RepositoryService targetRepositoryService, String filepath, Optional<TemplateFilter> templateFilter) throws IOException {
    try (InputStream content = templateRepositoryService.getCatCommand().getStream(filepath)) {
      templateFilter.ifPresent(filter -> filter.filter(content));
      targetRepositoryService.getModifyCommand().useDefaultPath(true).createFile(filepath)
        .setOverwrite(true)
        .withData(content);
    }
  }

  private TemplateEngine setupTemplateEngine(RepositoryTemplate repositoryTemplate) {
    String configEngine = repositoryTemplate.getEngine() != null ? repositoryTemplate.getEngine() : "mustache";
    TemplateEngine engine = engineFactory.getEngine(configEngine);
    if (engine == null) {
      //TODO create new exception?
      throw NotFoundException.notFound(ContextEntry.ContextBuilder.entity(TemplateEngine.class, configEngine));
    }
    return engine;
  }

  private RepositoryTemplate parseTemplateConfig(RepositoryService repositoryService) throws IOException {
    Optional<String> templateFile = RepositoryTemplateFinder.templateFileExists(repositoryService);
    if (!templateFile.isPresent()) {
      throw NotFoundException.notFound(
        ContextEntry.ContextBuilder.entity(RepositoryTemplate.class, "template file").in(repositoryService.getRepository())
      );
    }
    try (InputStream templateYml = repositoryService.getCatCommand().getStream(templateFile.get())) {
      RepositoryTemplate repositoryTemplate = unmarshallRepositoryTemplate(templateFile.get(), templateYml);
      repositoryTemplate.setNamespaceAndName(repositoryService.getRepository().getNamespaceAndName().toString());
      if (repositoryTemplate.getFiles() == null) {
        repositoryTemplate.setFiles(Collections.emptyList());
      }
      return repositoryTemplate;
    }
  }

  private RepositoryTemplate unmarshallRepositoryTemplate(String templateFile, InputStream templateYml) {
    RepositoryTemplate repositoryTemplate = yaml.loadAs(templateYml, RepositoryTemplate.class);
    if (repositoryTemplate == null) {
      throw new YAMLException("repository template invalid -> could not parse " + templateFile);
    }
    return repositoryTemplate;
  }

  static class TemplateFilter {
    private final RepositoryService targetRepositoryService;
    private final TemplateEngine engine;
    private final String filePath;

    private TemplateFilter(RepositoryService targetRepositoryService, TemplateEngine engine, String filePath) {
      this.targetRepositoryService = targetRepositoryService;
      this.engine = engine;
      this.filePath = filePath;
    }

    private void filter(InputStream inputStream) {
      try (Reader content = new InputStreamReader(inputStream)) {
        Template template = engine.getTemplate(filePath, content);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
        template.execute(writer, targetRepositoryService.getRepository());
        writer.flush();
      } catch (IOException e) {
        LOG.error("could not perform templating on file " + filePath, e);
      }
    }
  }
}
