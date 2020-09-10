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
