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
