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

/* A stub for the plugin endpoint which will be available at runtime. */

console.log("#1 Santosh from gocd-server-comms.js");

const AnalyticsEndpoint = function () {
  console.log("#1 Santosh someone calling AnalyticsEndpoint stub");
  return {
    ensure: () => {},
    on: () => {},
    define: () => {},
    init: () => {},
    onInit: () => {}
  };
};

export default new AnalyticsEndpoint();
