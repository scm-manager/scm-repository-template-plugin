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

import { binder } from "@scm-manager/ui-extensions";
import TemplateSelect from "./TemplateSelect";
import TemplateInfo from "./TemplateInfo";
import TemplateRepository from "./TemplateRepository";

binder.bind("repos.create.initialize", TemplateSelect);
binder.bind("editor.file.hints", TemplateInfo);
binder.bind("repo-config.route", TemplateRepository);
