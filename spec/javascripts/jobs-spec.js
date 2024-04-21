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
// import H from "assets/js/lib/load-highcharts.js";
import JobCharts from "js/charts/jobs.js";


function contains(array, entry) {
  return array.indexOf(entry) > -1;
}

function allTextNodes(dom) {
  return $.map(dom.querySelectorAll("text,span,tspan"), (el) => { return $.trim($(el).text()); });
}

group("Jobs with the Highest Wait Time", (test) => {
  test("should load chart", (t, done) => {
    const date = "2018-05-09",
           dom = document.createElement("div");

    // H.chart(dom, JobCharts.longestWaiting({
    //   jobs: [{
    //     "pipeline_name": "pip1",
    //     "stage_name": "stage1",
    //     "job_name": "job1",
    //     "time_waiting_secs": 12,
    //     "time_building_secs": 24,
    //     "scheduled_at": date,
    //     "duration_secs": 36
    //   }],
    //   pipeline_name: "pip1",
    //   start: date, end: date
    // }), () => {
    //   const text = allTextNodes(dom);
    //
    //   t.true(contains(text, "Jobs with the Highest Wait Time (Average over the last 7 days)"), "should contain chart title");
    //   t.true(contains(text, "job1"), "should contain job label");
    //   done();
    // });
  });
});

group("Jobs with the Highest Wait Time on an Agent", (test) => {
  test("should load chart", (t, done) => {
    const date = "2018-05-09",
           dom = document.createElement("div");

    // H.chart(dom, JobCharts.longestWaitingForAnAgent({
    //   jobs: [{
    //     "pipeline_name": "pip1",
    //     "stage_name": "stage1",
    //     "job_name": "job1",
    //     "time_waiting_secs": 12,
    //     "time_building_secs": 24,
    //     "scheduled_at": date,
    //     "duration_secs": 36
    //   }],
    //   agent_uuid: "11111",
    //   agent_host_name: "eek"
    // }), () => {
    //   const text = allTextNodes(dom),
    //     expected = "Jobs with the Highest Wait Time on an Agent (Average over the last 7 days)";
    //
    //   t.true(contains(text, expected), "should contain chart title");
    //   t.true(contains(text, "job1"), "should contain job label");
    //   done();
    // });
  });
});

group("Job Build Time", (test) => {
  test("should load chart", (t, done) => {
    const date = "2018-05-09",
           dom = document.createElement("div");

    // H.chart(dom, JobCharts.runs({
    //   identifier: "pip1/stage1/job1",
    //   jobs: [
    //     {
    //       "pipeline_name": "pip1",
    //       "stage_name": "stage1",
    //       "job_name": "job1",
    //       "pipeline_counter": 1,
    //       "stage_counter": 1,
    //       "result": "Passed",
    //       "scheduled_at": date,
    //       "time_building_secs": 24,
    //       "time_waiting_secs": 12
    //     }
    //   ]
    // }), () => {
    //   const text = allTextNodes(dom),
    //     expected = "Job Build Time";
    //
    //   t.true(contains(text, expected), "should contain chart title");
    //   t.true(contains(text, "job1"), "should contain job label");
    //   done();
    // });
  });
});
