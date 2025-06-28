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
import StageCharts, {DataTransforms} from "js/charts/stages";
import GoCDLinkSupport from "js/lib/gocd-link-support";
import Utils from "js/lib/utils.js";
import RangeSelectorSupport from "js/lib/range-selector-support.js";

export default new ChartFactories({
  StageBuildTime: {
    id: "stage_build_time",

    config: function (data, transport) {
      const config = StageCharts.runs(data),
              self = this,
             parts = data.identifier.split("/");

      GoCDLinkSupport.linkToStageDetailsPage(config, transport);

      Utils.addOnLoad(config, function addBehaviors() {
        RangeSelectorSupport.inject(transport, this, {
          params: self.params({pipeline_name: parts[0], stage_name: parts[1]}),
          transforms: DataTransforms.runs
        });
      });

      return config;
    },

    params: function (point) {
      return {
        "pipeline_name": point.pipeline_name,
        "stage_name":    point.stage_name,
        "type":          "stage",
        "metric":        this.id
      };
    }
  }
});
