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
import PipelineCharts, {DataTransforms} from "js/charts/pipelines";
import GoCDLinkSupport from "js/lib/gocd-link-support";
import Utils from "js/lib/utils.js";
import RangeSelectorSupport from "js/lib/range-selector-support.js";
import Tooltip from "js/lib/tooltip-util.js";

function initMttrTooltip(transport) {
    Tooltip.addTooltip(".highcharts-subtitle .mttr",
        {of: ".highcharts-subtitle .mttr", my: "left center", at: "right-12 top+11"},
        "Average time to recover from a failure. ",
        transport, "https://www.gocd.org/2018/01/31/continuous-delivery-metrics/");
}

export default new ChartFactories({
    PipelineBuildTime: {
        id: "pipeline_build_time",

        config: function (data, transport) {
            const config = PipelineCharts.runs(data),
                factory = this;

            GoCDLinkSupport.linkToPipelineInstance(config, transport);

            Utils.addOnLoad(config, function addBehaviors() {
                const chart = this;

                initMttrTooltip(transport);

                RangeSelectorSupport.inject(transport, chart, {
                    params: factory.params({pipeline_name: data.name}), // eslint-disable-line camelcase
                    callback: initMttrTooltip,
                    transforms: DataTransforms.runs
                });
            });

            return config;
        },

        params: function (point) {
            return {
                "pipeline_name": point.pipeline_name,
                "type": "pipeline",
                "metric": this.id
            };
        }
    },

    LongestWaitingPipelines: {
        id: "pipelines_with_the_highest_wait_time",

        config: (data) => {
            return PipelineCharts.longestWaiting(data);
        },

        params: function () {
            return {
                "type": "dashboard",
                "metric": this.id
            };
        }
    },

    PipelinesTimeline: {
        id: "pipelines-runtime-across-timeline",

        config: (data) => {
            return PipelineCharts.pipelineTimeline(data);
        },

        params: function () {
            return {
                "type": "dashboard",
                "metric": this.id
            };
        }
    }
});
