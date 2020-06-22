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
import VSMCharts from "js/charts/vsm";
import Utils from "js/lib/utils.js";
import GoCDLinkSupport from "js/lib/gocd-link-support";

export default new ChartFactories({
  WorkflowSparkline: {
    id: "vsm_trend_across_multiple_runs",

    config: function (data, pipelineNames, transport) {
      const config = VSMCharts.trends(data, pipelineNames);

      GoCDLinkSupport.linkToVSMPage(config, transport);
      Utils.addOnLoad(config, function addBehaviors() {
      });

      return config;
    }
  },
  WorkflowDetails: {
    id: "vsm_workflow_time_distribution",

    config: function (stages, workflowPipelines, transport) {
      const config = VSMCharts.details(stages, workflowPipelines);

      GoCDLinkSupport.linkToStageDetailsPage(config, transport);
      Utils.addOnLoad(config, function addBehaviors() {
      });

      return config;
    }
  }
});
