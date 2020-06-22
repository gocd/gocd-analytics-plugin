/*
 * Copyright 2020 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This file will bundle all tests and dependencies into a single file
 * to be loaded into a browser for karma runs.
 *
 * Both of the following variables are defined in karma.config.js:
 *
 *   __TEST_DIR__ is the base directory to traverse
 *   __FILE_RGX__ is the fs path regex to match tests
 */
const context = require.context(__TEST_DIR__, true, __FILE_RGX__); // eslint-disable-line no-undef
context.keys().forEach(context);
