/*
 * Copyright 2025 ThoughtWorks, Inc.
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

const globals = require("globals");
const js = require("@eslint/js");

module.exports = [
  js.configs.recommended,
  {
    languageOptions: {
      globals: {
        ...globals.browser,
      },
    },

    rules: {
      "linebreak-style": ["error", "unix"],
      "no-console": "error",
      "no-unused-vars": ["error", {"args": "after-used"}],
      "quotes": ["error", "double"],
      "semi": ["error", "always"],
      "space-infix-ops": ["error", {"int32Hint": true}],
      "yoda": ["error", "always", {"onlyEquality": true}]
    },
  }
];
