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

import "css/global";

import AnalyticsEndpoint from "gocd-server-comms";
import GraphManager from "../../../santosh/GraphManager";
import Console from "../../../santosh/Console";

const c = new Console('jobs-wait-vs-agents-available-chart.js');

async function requestAgentData(transport, jobs_data) {
  transport
    .request("fetch-analytics", {
      metric: "helper_agent_utilization",
    })
    .done((data) => {
      console.log("fetch-analytics worrysome", data);
      // this.initSeries(this.child.getNextGraphName(), JSON.parse(data));
      // return JSON.parse(data);

      const agents_data = JSON.parse(data);

      console.log("jobs_data = ", jobs_data);
      console.log("agents_data = ", agents_data);

      const graphManager = new GraphManager("standalone", null, null, null, c);
      graphManager.initStandalone("worrysome", {
        jobs: jobs_data,
        agents: agents_data,
      });

      console.log("*********** worrysome graph loaded");
    })
    .fail(console.error.toString());
}

AnalyticsEndpoint.onInit(async function (initialData, transport) {
  console.log("onInit called with initial data as ", initialData);

  const jobs_data = JSON.parse(initialData);

  const agents_data = await requestAgentData(transport, jobs_data);
});

AnalyticsEndpoint.ensure("v1");
