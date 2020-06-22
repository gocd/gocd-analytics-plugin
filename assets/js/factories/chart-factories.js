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

import _ from "lodash";

function throwNotImplemented(id, name) {
  return function unimplemented() {
    throw new Error(`${id} must implement ${name}()`);
  };
}

function ChartFactories(instances) {
  this.get = (id) => {
    if (id in instances) {
      const defaults = {
        id:     `UNSPECIFIED (missing from provider: ${id})`,
        config:  throwNotImplemented(id, "config"),
        params:  throwNotImplemented(id, "params")
      };

      return _.assign(defaults, instances[id]);
    }

    throw new Error(`Unknown drilldown route: ${id}`);
  };
}

export default ChartFactories;


