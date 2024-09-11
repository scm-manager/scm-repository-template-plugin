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

import { useMutation, useQueryClient } from "react-query";
import { Link, Repository } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";

export const useRepositoryTemplate = (repository: Repository) => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error } = useMutation<unknown, Error, string>(
    (link: string) => {
      return apiClient.post(link, {});
    },
    {
      onSuccess: () => {
        return queryClient.invalidateQueries(["repository", repository.namespace, repository.name]);
      }
    }
  );
  return {
    template: () => {
      mutate((repository._links.template as Link).href);
    },
    untemplate: () => {
      mutate((repository._links.untemplate as Link).href);
    },
    isLoading,
    error
  };
};
