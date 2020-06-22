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

import ChartFactories from "./chart-factories";
import AgentCharts from "js/charts/agents";

export default new ChartFactories({
  AgentsMostUtilized: {
    id: "agents_with_the_highest_utilization",

    config: (data) => {
      return AgentCharts.mostUtilized(data);
    },

    params: function () {
      return {
        "type":   "dashboard",
        "metric": this.id
      };
    }
  }
});
