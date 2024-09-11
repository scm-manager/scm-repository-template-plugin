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

import React, { FC, useEffect, useState } from "react";
import { apiClient, ErrorNotification, Loading, Select, SelectItem } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  setCreationContextEntry: (key: string, value: any) => void;
  indexResources: any;
};

type RepositoryTemplate = {
  templateRepository: string;
}

const TemplateSelect: FC<Props> = ({ setCreationContextEntry, indexResources }) => {
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | undefined>(undefined);
  const [selectedOption, setSelectedOption] = useState("");
  const [t] = useTranslation("plugins");

  const onSelectionChange = (value: string) => {
    setSelectedOption(value);
    setCreationContextEntry("repository-template", {
      templateId: value
    });
  };

  useEffect(() => {
    const name = "repositoryTemplates";
    let repositoryTemplatesLink = "";
    if (indexResources?.links && indexResources.links[name]) {
      repositoryTemplatesLink = indexResources.links[name].href;
    }
    if (repositoryTemplatesLink) {
      setLoading(true);
      apiClient
        .get(repositoryTemplatesLink)
        .then(response => response.json())
        .then(response =>
          response?._embedded?.templates?.map((template: RepositoryTemplate) => template.templateRepository)
        )
        .then(setOptions)
        .catch(setError)
        .finally(() => setLoading(false));
    }
  }, [setCreationContextEntry]);


  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (loading) {
    return <Loading />;
  }

  if (!options || !options.length) {
    return null;
  }

  const optionElements: SelectItem[] = options.map(option => ({ value: option, label: option }));
  optionElements.unshift({ value: "", label: t("scm-repository-template-plugin.template.default") });

  return (
    <Select
      label={t("scm-repository-template-plugin.templates")}
      onChange={onSelectionChange}
      options={optionElements}
      value={selectedOption}
      helpText={t("scm-repository-template-plugin.templateHelpText")}
    />
  );
};

export default TemplateSelect;
