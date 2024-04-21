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
import RequestMaster from "../../../RequestMaster";
import Header from "../../../santosh/defination/stage-timeline/header";
import Console from "../../../santosh/Console";

let graphManager = null;
let requestMaster = null;

let header = null;

let currentPrioritySettings;

const c = new Console('priority-view-chart.js');

AnalyticsEndpoint.onInit(function (initialData, transport) {

  const data = JSON.parse(initialData);

  requestMaster = new RequestMaster(transport);
  header = new Header(requestMaster);

  graphManager = new GraphManager("series", transport, informSeriesMovement, null);
  graphManager.initSeries("priority", data);

  console.log("*********** priority graph loaded");
});

async function informSeriesMovement(graphName, requestParams){
  console.log('📞 I am informed of graphName ', graphName, ' changing the header now.');
  // return header.switchHeader(graphName);
  if(graphName.includes("Details")) {
    return await header.getPriorityDetailsHeader(pdChangeHandler, currentPrioritySettings);
  }
   const settings = await header.getPriorityPipelineHeader(ppChangeHandler, requestParams.result);
  currentPrioritySettings = settings;
  return settings;
}

function pdChangeHandler(settings) {
  graphManager.call_initSeriesWithNewSettings(settings);
}

function ppChangeHandler(settings) {
  currentPrioritySettings = settings;
  doJob(settings);
}

async function doJob(settings) {
  console.log('🛜 requesting priority pipeline with the settings', settings);

  switch (settings.scope) {
    case 'Pipelines':
      const pp = await requestMaster.getPriorityPipeline(settings.result);
      graphManager.initSeries("PipelinePriority", pp, settings);
      break;
    case 'Stages':
      const ps = await requestMaster.getPriorityStage(settings.result);
      graphManager.initSeries("StagePriority", ps, settings);
      break;
    case 'Jobs':
      const pj = await requestMaster.getPriorityJob(settings.result);
      graphManager.initSeries("JobPriority", pj, settings);
      break;
  }

  // graphManager.call_initSeriesWithNewSettings(settings);
}

AnalyticsEndpoint.ensure("v1");
