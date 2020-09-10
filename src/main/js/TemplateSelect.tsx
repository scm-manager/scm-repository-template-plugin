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
  optionElements.unshift({ value: "", label: t("scm-repository-template-plugin.template.readme") });

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
