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

import {group} from "tape-plus";
import $ from "jquery";
import H from "js/lib/load-highcharts.js";
import PipelineCharts from "js/charts/pipelines.js";

function contains(array, entry) {
  return array.indexOf(entry) > -1;
}

function allTextNodes(dom) {
  return $.map(dom.querySelectorAll("text,span,tspan"), (el) => { return $.trim($(el).text()); });
}

group("Pipelines Build Time", (test) => {
  test("should load chart", (t, done) => {
    const date = "2018-05-09",
           dom = document.createElement("div");

    H.chart(dom, PipelineCharts.runs({
      instances: [{
        "name": "pip1",
        "counter": "1",
        "stage_name": "stage1",
        "result": "passed",
        "job_name": "job1",
        "time_waiting_secs": 12,
        "time_building_secs": 24,
        "scheduled_at": date
      }],
      name: "pip1",
      start: date, end: date
    }), () => {
      const text = allTextNodes(dom);

      t.true(contains(text, "Pipeline Build Time"), "should contain chart title");
      done();
    });
  });
});

group("Pipelines Longest Waiting", (test) => {

  test("should load chart", (t, done) => {
    const dom = document.createElement("div");

    H.chart(dom, PipelineCharts.longestWaiting([{
      avg_build_time_secs: 24,
      avg_wait_time_secs: 12,
      name: "pip1"
    }]), () => {
      const text = allTextNodes(dom),
        expected = "Pipelines with the Highest Wait Time (Average over the last 7 days)";

      t.true(contains(text, expected), "should contain chart title");
      t.true(contains(text, "pip1"), "should contain pipeline name");
      done();
    });
  });
});
