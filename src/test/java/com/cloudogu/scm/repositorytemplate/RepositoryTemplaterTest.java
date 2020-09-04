package com.cloudogu.scm.repositorytemplate;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yaml.snakeyaml.error.YAMLException;
import sonia.scm.NotFoundException;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.CatCommandBuilder;
import sonia.scm.repository.api.ModifyCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.template.TemplateType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
  @Mock(answer = Answers.RETURNS_SELF)
  private ModifyCommandBuilder modifyCommandBuilder;
  @Mock
  private TemplateEngineFactory engineFactory;
  @Mock
  private BrowserResult browserResult;
  @Mock(answer = Answers.RETURNS_SELF)
  private ModifyCommandBuilder.WithOverwriteFlagContentLoader contentLoader;

  @InjectMocks
  private RepositoryTemplater repositoryTemplater;


  @Test
  void shouldThrowNotFoundExceptionIfTemplateConfigIsMissing() throws IOException {
    when(serviceFactory.create(TEMPLATE_REPOSITORY)).thenReturn(templateRepositoryService);
    when(templateRepositoryService.getBrowseCommand()).thenReturn(browseCommandBuilder);
    when(browseCommandBuilder.getBrowserResult()).thenReturn(null);
    when(templateRepositoryService.getRepository()).thenReturn(TEMPLATE_REPOSITORY);

    assertThrows(NotFoundException.class, () -> repositoryTemplater.render(TEMPLATE_REPOSITORY, TARGET_REPOSITORY));
  }

  @Test
  void shouldThrowYamlExceptionIfTemplateFileCouldNotBeParsed() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/empty_template.yml");

    assertThrows(YAMLException.class, () -> repositoryTemplater.render(TEMPLATE_REPOSITORY, TARGET_REPOSITORY));
  }

  @Test
  void shouldNotModifyTargetRepositoryIfNoFilesDefined() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_without_files.yml");

    repositoryTemplater.render(TEMPLATE_REPOSITORY, TARGET_REPOSITORY);

    verify(modifyCommandBuilder, never()).createFile(any());
  }

  @Test
  void shouldUseDefaultEngineIfNotDefined() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_without_engine.yml");

    repositoryTemplater.render(TEMPLATE_REPOSITORY, TARGET_REPOSITORY);

    verify(engineFactory).getEngine("mustache");
  }

  @Test
  void shouldUseDefinedEngine() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_without_files.yml");

    repositoryTemplater.render(TEMPLATE_REPOSITORY, TARGET_REPOSITORY);

    verify(engineFactory).getEngine("jexl");
  }

  @Test
  void shouldThrowNotFoundExceptionIfEngineNotSupported() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_without_engine.yml");
    when(engineFactory.getEngine(anyString())).thenReturn(null);

    assertThrows(NotFoundException.class, () -> repositoryTemplater.render(TEMPLATE_REPOSITORY, TARGET_REPOSITORY));
  }

  @Test
  void shouldCopyAndTemplateFilesWithoutFilter() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_files_without_filter.yml");
    FileObject fileObject = createFileObject("README.md");
    when(browserResult.getFile()).thenReturn(fileObject);
    when(targetRepositoryService.getModifyCommand()).thenReturn(modifyCommandBuilder);
    when(modifyCommandBuilder.createFile(anyString())).thenReturn(contentLoader);

    repositoryTemplater.render(TEMPLATE_REPOSITORY, TARGET_REPOSITORY);

    verify(modifyCommandBuilder, times(1)).createFile("README.md");
  }

  @Test
  void shouldCopyAndTemplateFiles() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template.yml");
    FileObject fileObject = createFileObject("README.md");
    when(browserResult.getFile()).thenReturn(fileObject);
    when(targetRepositoryService.getModifyCommand()).thenReturn(modifyCommandBuilder);
    when(targetRepositoryService.getRepository()).thenReturn(TARGET_REPOSITORY);
    when(modifyCommandBuilder.createFile(anyString())).thenReturn(contentLoader);

    repositoryTemplater.render(TEMPLATE_REPOSITORY, TARGET_REPOSITORY);

    verify(modifyCommandBuilder, times(3)).createFile(anyString());
  }


  @Test
  void shouldCopyAndTemplateDirectoryRecursively() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_only_dir.yml");

    FileObject testMd = createFileObject("src/test.md");
    FileObject docsMd = createFileObject("src/docs.md");
    FileObject src = createFileObject("src", true, ImmutableList.of(testMd, docsMd));

    when(browserResult.getFile()).thenReturn(src);
    when(targetRepositoryService.getModifyCommand()).thenReturn(modifyCommandBuilder);
    when(modifyCommandBuilder.createFile(anyString())).thenReturn(contentLoader);

    repositoryTemplater.render(TEMPLATE_REPOSITORY, TARGET_REPOSITORY);

    verify(modifyCommandBuilder, times(2)).createFile(anyString());
  }

  @Test
  void shouldCopyAndTemplateNestedDirectoriesRecursively() throws IOException {
    mockTemplateService("com/cloudogu/scm/repositorytemplate/template_only_dir.yml");

    FileObject testMd = createFileObject("test.md");
    FileObject docsMd = createFileObject("src/main/docs.md");
    FileObject buildMd = createFileObject("src/main/build.md");
    FileObject src = createFileObject("src", true, ImmutableList.of(docsMd, buildMd));
    FileObject root = createFileObject("", true, ImmutableList.of(testMd, src));

    when(browserResult.getFile()).thenReturn(root);
    when(targetRepositoryService.getModifyCommand()).thenReturn(modifyCommandBuilder);
    when(modifyCommandBuilder.createFile(anyString())).thenReturn(contentLoader);

    repositoryTemplater.render(TEMPLATE_REPOSITORY, TARGET_REPOSITORY);

    verify(modifyCommandBuilder, times(3)).createFile(anyString());
  }

  private FileObject createFileObject(String path) {
    FileObject testMd = new FileObject();
    testMd.setPath(path);
    return testMd;
  }

  private FileObject createFileObject(String path, boolean isDir, List<FileObject> children) {
    FileObject fileObject = createFileObject(path);
    fileObject.setDirectory(isDir);
    fileObject.setChildren(children);
    return fileObject;
  }

  private void mockTemplateService(String resourceFile) throws IOException {
    BufferedInputStream content = (BufferedInputStream) Resources.getResource(resourceFile).getContent();
    when(serviceFactory.create(TEMPLATE_REPOSITORY)).thenReturn(templateRepositoryService);
    lenient().when(serviceFactory.create(TARGET_REPOSITORY)).thenReturn(targetRepositoryService);
    when(templateRepositoryService.getBrowseCommand()).thenReturn(browseCommandBuilder);
    when(browseCommandBuilder.getBrowserResult()).thenReturn(browserResult);
    lenient().when(catCommandBuilder.getStream(any())).thenReturn(content);
    lenient().when(templateRepositoryService.getCatCommand()).thenReturn(catCommandBuilder);
    lenient().when(templateRepositoryService.getRepository()).thenReturn(TEMPLATE_REPOSITORY);
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
