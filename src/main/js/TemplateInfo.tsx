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
import { Notification } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  path?: string;
};

const TemplateInfo: FC<Props> = ({ path }) => {
  const [t, i18n] = useTranslation("plugins");

  const language = i18n.language === "de" ? "de" : "en";
  const documentationUrl = `https://scm-manager.org/plugins/scm-repository-template-plugin/docs/1.0.x/${language}/usage/`;

  if (path?.includes("template.yml") || path?.includes("template.yaml")) {
    return (
      <Notification type="info">
        {t("scm-repository-template-plugin.info.helpText")}{" "}
        <a href={documentationUrl} target="_blank">{t("scm-repository-template-plugin.info.documentation")}.</a>
      </Notification>
    );
  }
  return null;
};

export default TemplateInfo;
