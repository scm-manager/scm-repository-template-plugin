---
title: Usage
---
To use a repository as a template you must simply add a `template.yml` file to the root directory on the main branch. 
This `template.yml` defines which files should be copied from the template to the target repository 
and if their content should be templated using a template engine.
When copying, the engine replaces placeholders with the corresponding value from the context.

The following placeholders are available:
- repository.namespace
- repository.name
- repository.type
- repository.contact
- repository.description
- repository.id

The default template engine is [mustache](https://mustache.github.io/) and the default encoding is `UTF-8` if nothing else is defined.

The template yaml must contain a field `files` with contains all files as an array.
Each file must have the fields `path` (file path in repository) and `filtered` (should file content be templated)
If you use a directory path all files inside will be copied and templated (if set true).
Additional you may choose a custom template engine or another encoding. 

Example:
```yaml 
engine: mustache
encoding: UTF-8
files: 
    -   path: README.md
        filtered: true
    -   path: src/main
        filtered: false
```

![Template](assets/template.png)

Example of a file that can be templated with the Mustache engine (here a pom.xml):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.example.{{repository.name}}</groupId>
  <artifactId>{{repository.name}}</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <packaging>pom</packaging>

  <name>{{repository.name}}</name>
  <description>{{repository.description}}</description>
    
  <scm>
    <developerConnection>scm:{{repository.type}}:https://example.com/scm/repo/projects/{{repository.name}}</developerConnection>
  </scm>

</project>
```

After a repository got marked as template it is available on the repository creation if you check `initialize repository`.

![Create repository](assets/create-repo.png)
