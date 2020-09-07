import React, {FC, useEffect, useState} from "react";
import {apiClient, ErrorNotification, Loading, Select} from "@scm-manager/ui-components";
import {useTranslation} from "react-i18next";
import {SelectItem} from "@scm-manager/ui-components/src/forms/Select";
import {connect} from "react-redux";

function getTemplatesLink(state: any) {
  const name = "repositoryTemplates";
  if (state.indexResources.links && state.indexResources.links[name]) {
    return state.indexResources.links[name].href;
  }
}

type ReduxProps = {
  repositoryTemplatesLink: string;
}

type Props = {
  setCreationContextEntry: (key: string, value: any) => void;
} & ReduxProps;

const TemplateSelect: FC<Props> = ({setCreationContextEntry, repositoryTemplatesLink}) => {

  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | undefined>(undefined);
  const [selectedOption, setSelectedOption] = useState("");
  const [t] = useTranslation('plugins');

  const onSelectionChange = (value: string) => {
    setSelectedOption(value);
    setCreationContextEntry("templateId", value);
  }

  useEffect(() => {
    setLoading(true);
    apiClient
      .get(repositoryTemplatesLink)
      .then(response => response.json())
      .then(setOptions)
      .catch(setError)
      .finally(() => setLoading(false));
  }, [setCreationContextEntry]);

  if (loading) {
    return <Loading/>
  } else if (error) {
    return <ErrorNotification error={error}/>
  } else if (!options || !options.length) {
    return null;
  }

  const optionElements: SelectItem[] = options.map(option => ({value: option, label: option}));
  optionElements.unshift({value: "", label: "README.md"});

  return <Select
    label={t("scm-repository-template-plugin.templates")}
    onChange={onSelectionChange}
    options={optionElements}
    value={selectedOption}
    helpText={t("scm-repository-template-plugin.templateHelpText")}
  />;
};

const mapStateToProps = (state: object): ReduxProps => {
  return {
    repositoryTemplatesLink: getTemplatesLink(state)
  }
}

export default connect(mapStateToProps)(TemplateSelect);
