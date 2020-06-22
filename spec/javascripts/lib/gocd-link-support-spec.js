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

import test from "tape-plus";
import sinon from "sinon";
import GoCDLinkSupport from"js/lib/gocd-link-support";

function noop() {}

test("GoCD Link Support should add a config point onclick handler", (t) => {
  const config = {series: [{name: "first"}]};

  const transport   = {};
  transport.request = function stub() { return {done: noop}; };

  GoCDLinkSupport.linkToJobDetailsPage(config, transport);

  const pointOnclick = config.series[0].point.events.click;

  t.is(typeof pointOnclick, "function", "events.click should be a function");
});

test("GoCD Link Support should make transport request to job details page", (t) => {
  const config = {series: [{name: "first"}]};

  const transport = {};
  let promise     = {
    done: () => promise,
    fail: () => promise
  };

  transport.request = sinon.spy(function stub() { return promise; });

  GoCDLinkSupport.linkToJobDetailsPage(config, transport);

  const pointOnclick = config.series[0].point.events.click;

  const point = {
    pipeline_name:    "up42",
    pipeline_counter: 1,
    stage_name:       "up42_stage",
    stage_counter:    1,
    job_name:         "up42_job"
  };

  pointOnclick({point});

  t.true(transport.request.calledWith("link-to", {
    link_to: "job_details_page",
    pipeline_name: point.pipeline_name,
    pipeline_counter: point.pipeline_counter,
    stage_name: point.stage_name,
    stage_counter: point.stage_counter,
    job_name: point.job_name
  }), "should have requested and passed the correct params to `link-to`");
});

test("GoCD Link Support should add a config point onclick handler for vsm page", (t) => {
  const config = {series: [{name: "first"}]};

  const transport   = {};
  transport.request = function stub() { return {done: noop}; };

  GoCDLinkSupport.linkToVSMPage(config, transport);

  const pointOnclick = config.series[0].point.events.click;

  t.is(typeof pointOnclick, "function", "events.click should be a function");
});

test("GoCD Link Support should make transport request to vsm page", (t) => {
  const config = {series: [{name: "first"}]};

  const transport = {};
  let promise     = {
    done: () => promise,
    fail: () => promise
  };

  transport.request = sinon.spy(function stub() { return promise; });

  GoCDLinkSupport.linkToVSMPage(config, transport);

  const pointOnclick = config.series[0].point.events.click;

  const point = {
    pipeline_name:    "up42",
    pipeline_counter: 1
  };

  pointOnclick({point});

  t.true(transport.request.calledWith("link-to", {
    link_to: "vsm_page",
    pipeline_name: point.pipeline_name,
    pipeline_counter: point.pipeline_counter
  }), "should have requested and passed the correct params to `link-to`");
});
