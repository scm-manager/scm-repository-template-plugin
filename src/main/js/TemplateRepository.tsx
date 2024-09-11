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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { Button, ErrorNotification, Level, Subtitle } from "@scm-manager/ui-components";
import { useRepositoryTemplate } from "./useRepositoryTemplate";

type Props = {
  repository: Repository;
};

const TemplateRepository: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("plugins");
  const { isLoading, error, template, untemplate } = useRepositoryTemplate(repository);

  if (!(repository._links.template || repository._links.untemplate)) {
    return null;
  }

  let button;
  if (repository._links.template) {
    button = (
      <Button
        className="is-primary"
        icon="clone"
        label={t("scm-repository-template-plugin.templateRepository.template")}
        action={template}
        loading={isLoading}
      />
    );
  } else {
    button = (
      <Button
        className="is-warning"
        icon="times"
        label={t("scm-repository-template-plugin.templateRepository.untemplate")}
        action={untemplate}
        loading={isLoading}
      />
    );
  }

  const description = repository._links.template
    ? t("scm-repository-template-plugin.templateRepository.description.template")
    : t("scm-repository-template-plugin.templateRepository.description.untemplate");

  const subtitle = repository._links.template
    ? t("scm-repository-template-plugin.templateRepository.title.template")
    : t("scm-repository-template-plugin.templateRepository.title.untemplate");

  return (
    <>
      <hr />
      <Subtitle subtitle={subtitle} />
      <p className="pb-4">{description}</p>
      {error && <ErrorNotification error={error} />}
      <Level right={button} />
    </>
  );
};

export default TemplateRepository;
