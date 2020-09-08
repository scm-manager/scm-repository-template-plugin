---
title: Usage
---
To use a repository as a template you must simply add a `template.yml` file to the root directory on the main branch. 
This `template.yml` defines which files should be copied from the template to the target repository 
and if their content should be templated using a template engine. 

The default template engine is [mustache](https://mustache.github.io/) if no engine is defined.

![Template](assets/template.png)

After a repository got marked as template it is available on the repository creation if you check `initialize repository`.

![Create repository](assets/create-repo.png)
