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
import RequestMaster from "../../../RequestMaster";
import Header from "../../../santosh/defination/stage-timeline/header";
import Footer from "../../../santosh/defination/stage-timeline/footer";
import {
  getFirstDayOfTheCurrentMonth,
  getTodaysDateInDBFormat
} from "../../../santosh/utils";

let graphManager = null;
let requestMaster = null;

let header = null;
let footer = null;

const c = new Console('pipeline-state-summary-chart.js', 'dev');

let settings = {
  startDate: getFirstDayOfTheCurrentMonth(),
  endDate: getTodaysDateInDBFormat(),
};

AnalyticsEndpoint.onInit(function (initialData, transport) {
  const data = JSON.parse(initialData);

  requestMaster = new RequestMaster(transport);
  header = new Header(requestMaster);
  footer = new Footer();

  graphManager = new GraphManager("standalone", transport, null,
      footer);

  init(data);

  console.log("*********** pipeline-state-summary graph loaded");
});

async function init(data) {
  settings = await header.getPipelineStateSummaryHeader(
      graphOneHeaderChangeHandler, settings);

  if (data.length === 0) {
    footer.showMessage("No data to display", "INFO", true);
  } else {
    graphManager.initStandalone("pipeline-state-summary", data);
  }

}

async function graphOneHeaderChangeHandler(changedSettings) {
  c.logs('📞 I am informed of changedSettings ', changedSettings);

  console.log("📞 I am informed of changedSettings", changedSettings);

  const pipelines = await requestMaster.getPipelineStateSummary(changedSettings);

  graphManager.clear();

  if (pipelines.length === 0) {
    footer.showMessage("No data to display", "Error", true);
  } else {
    graphManager.call_initStandaloneWithNewData(pipelines);
    footer.clear()
  }

}

AnalyticsEndpoint.ensure("v1");
