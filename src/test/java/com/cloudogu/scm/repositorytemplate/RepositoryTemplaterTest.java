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

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yaml.snakeyaml.error.YAMLException;
import sonia.scm.NotFoundException;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryContentInitializer;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.CatCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.template.TemplateType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryTemplaterTest {

  private static final Repository TEMPLATE_REPOSITORY = RepositoryTestData.createHeartOfGold();
  private static final Repository TARGET_REPOSITORY = RepositoryTestData.create42Puzzle();

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService templateRepositoryService;
  @Mock(answer = Answers.RETURNS_SELF)
  private BrowseCommandBuilder browseCommandBuilder;
  @Mock(answer = Answers.RETURNS_SELF)
  private CatCommandBuilder catCommandBuilder;
  @Mock
  private RepositoryService targetRepositoryService;
  @Mock
  private TemplateEngineFactory engineFactory;
  @Mock
  private BrowserResult browserResult;
  @Mock
  private RepositoryContentInitializer.InitializerContext context;
  @Mock
  private RepositoryContentInitializer.CreateFile createFile;

  @InjectMocks
  private RepositoryTemplater repositoryTemplater;


  @Test
  void shouldThrowNotFoundExceptionIfTemplateConfigIsMissing() throws IOException {
    when(serviceFactory.create(TEMPLATE_REPOSITORY)).thenReturn(templateRepositoryService);
    when(templateRepositoryService.getBrowseCommand()).thenReturn(browseCommandBuilder);
    when(browseCommandBuilder.getBrowserResult()).thenReturn(null);
    when(templateRepositoryService.getRepository()).thenReturn(TEMPLATE_REPOSITORY);

    assertThrows(NotFoundException.class, () -> repositoryTemplater.render(TEMPLATE_REPOSITORY, context));
  }

  @Test
  void shouldThrowTemplateParsingExceptionIfTemplateFileCouldNotBeParsed() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/empty_template.yml");

    assertThrows(TemplateParsingException.class, () -> repositoryTemplater.render(TEMPLATE_REPOSITORY, context));
  }

  @Test
  void shouldNotModifyTargetRepositoryIfNoFilesDefined() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_without_files.yml");

    repositoryTemplater.render(TEMPLATE_REPOSITORY, context);

    verify(context, never()).create(any());
  }

  @Test
  void shouldUseDefaultEngineIfNotDefined() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_without_engine.yml");

    repositoryTemplater.render(TEMPLATE_REPOSITORY, context);

    verify(engineFactory).getEngine("mustache");
  }

  @Test
  void shouldUseDefinedEngine() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_without_files.yml");

    repositoryTemplater.render(TEMPLATE_REPOSITORY, context);

    verify(engineFactory).getEngine("jexl");
  }

  @Test
  void shouldThrowNotFoundExceptionIfEngineNotSupported() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_without_engine.yml");
    when(engineFactory.getEngine(anyString())).thenReturn(null);

    assertThrows(NotFoundException.class, () -> repositoryTemplater.render(TEMPLATE_REPOSITORY, context));
  }

  @Test
  void shouldOnlyCopyFilesWithoutFilter() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_files_without_filter.yml");
    FileObject fileObject = createFileObject("README.md");
    when(browserResult.getFile()).thenReturn(fileObject);
    when(context.createWithDefaultPath(anyString(), eq(true))).thenReturn(createFile);

    repositoryTemplater.render(TEMPLATE_REPOSITORY, context);

    verify(context, times(1)).createWithDefaultPath("README.md", true);
  }

  @Test
  void shouldCopyAndTemplateFiles() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template.yml");
    FileObject fileObject = createFileObject("README.md");
    when(browserResult.getFile()).thenReturn(fileObject);
    when(context.createWithDefaultPath(anyString(), eq(true))).thenReturn(createFile);

    repositoryTemplater.render(TEMPLATE_REPOSITORY, context);

    verify(context, times(3)).createWithDefaultPath(anyString(), eq(true));
  }

  @Test
  void shouldNotCopyTemplateYaml() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_with_template_yaml.yml");

    FileObject templateYml = createFileObject("template.yml");
    FileObject templateYaml = createFileObject("template.yaml");
    FileObject docsMd = createFileObject("src/main/docs.md");
    FileObject buildMd = createFileObject("src/main/template.yml");
    FileObject packageMd = createFileObject("src/main/packageMd.md");
    FileObject main = createFileObject("src/main", ImmutableList.of(docsMd, buildMd, packageMd));
    FileObject src = createFileObject("src", ImmutableList.of(main));
    createFileObject("", ImmutableList.of(templateYml, templateYaml, src));

    when(context.createWithDefaultPath(anyString(), eq(true))).thenReturn(createFile);

    repositoryTemplater.render(TEMPLATE_REPOSITORY, context);

    verify(context, times(3)).createWithDefaultPath(anyString(), eq(true));
  }

  @Test
  void shouldCopyAndTemplateDirectoryRecursively() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_only_dir.yml");

    FileObject testMd = createFileObject("src/test.md");
    FileObject docsMd = createFileObject("src/docs.md");
    createFileObject("src", ImmutableList.of(testMd, docsMd));

    when(context.createWithDefaultPath(anyString(), eq(true))).thenReturn(createFile);

    repositoryTemplater.render(TEMPLATE_REPOSITORY, context);

    verify(context, times(2)).createWithDefaultPath(anyString(), eq(true));
  }

  @Test
  void shouldCopyAndTemplateNestedDirectoriesRecursively() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_only_dir.yml");

    FileObject testMd = createFileObject("test.md");
    FileObject templateYml = createFileObject("template.yml");
    FileObject docsMd = createFileObject("src/main/docs.md");
    FileObject buildMd = createFileObject("src/main/build.md");
    FileObject packageMd = createFileObject("src/main/packageMd.md");
    FileObject main = createFileObject("src/main", ImmutableList.of(docsMd, buildMd, packageMd));
    FileObject src = createFileObject("src", ImmutableList.of(main));
    createFileObject("", ImmutableList.of(testMd, templateYml, src));

    when(context.createWithDefaultPath(anyString(), eq(true))).thenReturn(createFile);

    repositoryTemplater.render(TEMPLATE_REPOSITORY, context);

    verify(context, times(3)).createWithDefaultPath(anyString(), eq(true));
  }

  @ParameterizedTest
  @CsvSource({
    "git,git,true",
    "git,hg,true",
    "git,svn,true",
    "hg,git,true",
    "hg,hg,true",
    "hg,svn,true",
    "svn,git,true",
    "svn,hg,true",
    "svn,svn,false",
  })
  void shouldUseDefaultPathCorrectly(String template, String target, String useDefaultPath) throws IOException {
    Repository templateRepo = RepositoryTestData.createHeartOfGold(template);
    Repository targetRepo = RepositoryTestData.createHeartOfGold(target);
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template.yml", templateRepo, targetRepo);

    FileObject fileObject = createFileObject("README.md");
    when(browserResult.getFile()).thenReturn(fileObject);
    when(context.createWithDefaultPath(anyString(), eq(Boolean.parseBoolean(useDefaultPath)))).thenReturn(createFile);
    when(context.getRepository()).thenReturn(targetRepo);

    repositoryTemplater.render(templateRepo, context);

    verify(context, times(3))
      .createWithDefaultPath(anyString(), eq(Boolean.parseBoolean(useDefaultPath)));
  }


  private void mockNestedBrowseCommandBuilder(String path, FileObject object) throws IOException {
    BrowseCommandBuilder mockedBrowseCommandBuilder = mock(BrowseCommandBuilder.class, Answers.RETURNS_SELF);
    lenient().doReturn(mockedBrowseCommandBuilder).when(browseCommandBuilder).setPath(path);
    object.setName("template.yml");
    lenient().doReturn(new BrowserResult("1", object)).when(mockedBrowseCommandBuilder).getBrowserResult();
  }

  private FileObject createFileObject(String path) throws IOException {
    FileObject fileObject = new FileObject();
    fileObject.setPath(path);
    mockNestedBrowseCommandBuilder(path, fileObject);
    return fileObject;
  }

  private FileObject createFileObject(String path, List<FileObject> children) throws IOException {
    FileObject fileObject = createFileObject(path);
    fileObject.setDirectory(true);
    fileObject.setChildren(children);
    mockNestedBrowseCommandBuilder(path, fileObject);

    return fileObject;
  }

  private void mockTemplateService(String resourceFile) throws IOException {
    mockTemplateService(resourceFile, TEMPLATE_REPOSITORY, TARGET_REPOSITORY);
  }

  private void mockTemplateService(String resourceFile, Repository template, Repository target) throws IOException {
    BufferedInputStream content = (BufferedInputStream) Resources.getResource(resourceFile).getContent();
    lenient().when(serviceFactory.create(template)).thenReturn(templateRepositoryService);
    when(templateRepositoryService.getBrowseCommand()).thenReturn(browseCommandBuilder);
    lenient().when(browseCommandBuilder.getBrowserResult()).thenReturn(browserResult);
    FileObject file = new FileObject();
    file.setName("template.yml");
    lenient().when(browserResult.getFile()).thenReturn(file);
    lenient().when(serviceFactory.create(target)).thenReturn(targetRepositoryService);
    lenient().when(catCommandBuilder.getStream(any())).thenReturn(content);
    lenient().when(templateRepositoryService.getCatCommand()).thenReturn(catCommandBuilder);
    lenient().when(templateRepositoryService.getRepository()).thenReturn(template);
    lenient().when(engineFactory.getEngine(anyString())).thenReturn(new TestTemplateEngine());
  }
}

class TestTemplateEngine implements TemplateEngine {

  @Override
  public Template getTemplate(String templatePath) {
    return null;
  }

  @Override
  public Template getTemplate(String templateIdentifier, Reader reader) {
    return (writer, model) -> {
    };
  }

  @Override
  public TemplateType getType() {
    return null;
  }
}
