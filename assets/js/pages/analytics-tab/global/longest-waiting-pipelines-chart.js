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

const c = new Console('longest-waiting-pipelines-chart.js', 'prod');

AnalyticsEndpoint.onInit(function (initialData, transport) {
    const data = JSON.parse(initialData);

    c.log("data = " + data);

    const graphManager = new GraphManager('series', transport, null, null);
    graphManager.initSeries('LongestWaitingPipelines', data);

    c.log("*********** graph loaded");
});

AnalyticsEndpoint.ensure("v1");
