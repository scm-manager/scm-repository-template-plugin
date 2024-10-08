# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 3.0.0 - 2024-09-11
### Changed
- Changeover to AGPLv3 license

## 1.2.2 - 2024-05-23
### Fixed
- When using a repository template to initialize a SVN repository, the content is now placed in the correct directories and not just under the `/trunk` path

## 1.2.1 - 2024-05-23
## 1.2.0 - 2024-01-17
### Changed
- Update snakeyaml to 2.2 to prepare for SCM-Manager 3.x

## 1.1.1 - 2022-01-25
### Fixed
- Repos created from templates should not immediately become templates themselves ([#12](https://github.com/scm-manager/scm-repository-template-plugin/pull/12))

## 1.1.0 - 2021-10-21
### Added
- Add option to create template from repository ([#9](https://github.com/scm-manager/scm-repository-template-plugin/pull/9))

## 1.0.2 - 2020-12-10
### Fixed
- Ignore templates if user may not read repository ([#2](https://github.com/scm-manager/scm-repository-template-plugin/pull/2))

## 1.0.1 - 2020-09-11
### Fixed
- Check if template file is not a directory

## 1.0.0 - 2020-09-10
### Added
- Initial templating mechanism when creating a new repository ([#1](https://github.com/scm-manager/scm-repository-template-plugin/pull/1))

