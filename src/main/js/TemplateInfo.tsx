import React, { FC } from "react";
import { Notification } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  path?: string;
};

const TemplateInfo: FC<Props> = ({ path }) => {
  const [t, i18n] = useTranslation("plugins");

  const language = i18n.language === "de" ? "de" : "en";
  const documentationUrl = `https://www.scm-manager.org/plugins/scm-repository-template-plugin/docs/1.0.x/${language}/usage/`;

  if (path?.includes("template.yml") || path?.includes("template.yaml")) {
    return (
      <Notification type="info">
        {t("scm-repository-template-plugin.info.helpText")}{" "}
        <a href={documentationUrl} target="_blank">www.scm-manager.org/</a>
      </Notification>
    );
  }
  return null;
};

export default TemplateInfo;
