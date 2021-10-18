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
import { useTranslation } from "react-i18next";
import { Link, Repository } from "@scm-manager/ui-types";
import { apiClient, Subtitle, Level, Button, ErrorNotification } from "@scm-manager/ui-components";

type Props = {
  repository: Repository;
};

const TemplateRepository: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("plugins");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [link, setLink] = useState("");
  const [template, setTemplate] = useState(false);

  useEffect(() => {
    setLoading(true);
    setLink(getLink(repository));
    setTemplate(!!repository?._links?.untemplate);
    setLoading(false);
  }, [repository]);

  const getLink: (repository: Repository) => string = (repository: Repository) => {
    const currentLink = (repository?._links?.untemplate || repository?._links?.template) as Link;
    return currentLink.href;
  };

  const toggleTemplate = () => {
    setLoading(true);
    apiClient
      .post(link)
      .then(() => apiClient.get((repository._links.self as Link).href))
      .then(response => response.json())
      .then(updatedRepository => {
        setTemplate(!template);
        setLink(getLink(updatedRepository));
      })
      .catch(setError)
      .finally(() => setLoading(false));
  };

  return (
    <>
      <hr />
      <Subtitle subtitle={t("scm-repository-template-plugin.templateRepository.title")} />
      {error && <ErrorNotification error={error} />}
      <Level
        right={
          <Button
            label={
              template
                ? t("scm-repository-template-plugin.templateRepository.untemplate")
                : t("scm-repository-template-plugin.templateRepository.template")
            }
            action={toggleTemplate}
            loading={loading}
          />
        }
      />
    </>
  );
};

export default TemplateRepository;
