import React, { FC, useEffect, useState } from "react";
import { apiClient, ErrorNotification, Loading, Select, SelectItem } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  setCreationContextEntry: (key: string, value: any) => void;
  indexResources: any;
};

type RepositoryTemplate = {
  namespaceAndName: string;
}

const TemplateSelect: FC<Props> = ({ setCreationContextEntry, indexResources }) => {
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | undefined>(undefined);
  const [selectedOption, setSelectedOption] = useState("");
  const [t] = useTranslation("plugins");

  const onSelectionChange = (value: string) => {
    setSelectedOption(value);
    setCreationContextEntry("templateId", value);
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
          response?._embedded?.templates?.map((template: RepositoryTemplate) => template.namespaceAndName)
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
