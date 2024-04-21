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
import AgentsCharts from "js/charts/agents.js";
import moment from "../../assets/js/lib/moment-humanize-for-gocd";
import D from "js/lib/fast-date-utils";

function contains(array, entry) {
  return array.indexOf(entry) > -1;
}

function allTextNodes(dom) {
  return $.map(dom.querySelectorAll("text,span,tspan"), (el) => {
    return $.trim($(el).text());
  });
}

function noop() {}

group("Agents", () => {
  group("Agents with the Highest Utilization", (test) => {
    test("should load chart", (t, done) => {
      const dom = document.createElement("div");

      // H.chart(dom, AgentsCharts.mostUtilized([{
      //   "agent_host_name": "agent.go.cd",
      //   "uuid": "eek",
      //   "idle_duration_secs": 120,
      //   "building_duration_secs": 120,
      // }]), () => {
      //   const text = allTextNodes(dom),
      //     expected = "Agents with the Highest Utilization (Average over the last 7 days)";
      //
      //   t.true(contains(text, expected), "should contain chart title");
      //   t.true(contains(text, "agent.go.cd"), "should contain agent hostname");
      //   done();
      // });
    });
  });

  group("Agent transitions", (test) => {
    const selector = "body";

    test("should return all agent states", (t) => {
      const result = AgentsCharts.transitions(selector, [], {}, noop);
      t.deepEqual(result.status, ["Idle", "Building", "Cancelled", "Missing", "LostContact", "Unknown"]);
    });

    test("should return the title of the graph", (t) => {
      const result = AgentsCharts.transitions(selector, [], {}, noop);
      t.equal(result.title, "Agent State Transitions");
    });

    group("Transitions", (test) => {
      test("should compute states for the provided agent transitions", (t) => {
        const wentIdleAt = new Date(2018, 4, 21, 11, 0, 0, 0);
        const wentBuildingAt = D.addHours(wentIdleAt, 1, true);
        const result = AgentsCharts.transitions(
          selector,
          [
            {agent_state: "Idle", transition_time: wentIdleAt},
            {agent_state: "Building", transition_time: wentBuildingAt}
          ], {}, noop
        );

        t.equal(result.data.length, 1, "should have 1 datum when transition happens within the same day");
        const firstTransition = result.data[0];
        t.deepEqual(firstTransition.startDate, D.relativeTime(wentIdleAt), "should generate start timestamp relative to beginning of day");
        t.deepEqual(firstTransition.endDate, D.relativeTime(wentBuildingAt), "should generate end timestamp relative to beginning of day");
        t.equal(firstTransition.date, "Mon 21 May", "axis date should be friendly formatted");
        t.equal(firstTransition.status, "Idle", "should match status");
      });

      test("should compute states for the provided agent transitions between two dates", (t) => {
        const wentIdleAt = new Date(2018, 4, 21, 11, 0, 0, 0);
        const wentBuildingAt = D.addDays(wentIdleAt, 1, true);
        const result = AgentsCharts.transitions(
          selector,
          [
            {agent_state: "Idle", transition_time: wentIdleAt},
            {agent_state: "Building", transition_time: wentBuildingAt}
          ], {}, noop
        );

        t.equal(result.data.length, 2, "should have 2 data points when transition spans two days");

        t.equal(result.data[0].date, "Mon 21 May", "first point should be day 1");
        t.deepEqual(result.data[0].startDate, D.relativeTime(wentIdleAt), "should generate start timestamp relative to beginning of day");
        const endOfDay = D.endOfDay(wentIdleAt, true);
        t.deepEqual(result.data[0].endDate, D.relativeTime(endOfDay), "should generate end timestamp relative to beginning of day");
        t.deepEqual(result.data[0].status, "Idle", "should match status");

        t.equal(result.data[1].date, "Tue 22 May", "second point should be day 2");
        const startOfDay = D.startOfDay(wentBuildingAt, true);
        t.deepEqual(result.data[1].startDate, D.relativeTime(startOfDay), "should generate start timestamp relative to beginning of day");
        t.deepEqual(result.data[1].endDate, D.relativeTime(wentBuildingAt), "should generate end timestamp relative to beginning of day");
        t.deepEqual(result.data[1].status, "Idle", "should match status");
      });

      test("should compute states for the provided agent transitions with no events between days", (t) => {
        const wentIdleAt = new Date(2018, 4, 21, 11, 0, 0, 0);
        const wentBuildingAt = D.addDays(wentIdleAt, 2, true);
        const result = AgentsCharts.transitions(
          selector,
          [
            {agent_state: "Idle", transition_time: wentIdleAt},
            {agent_state: "Building", transition_time: wentBuildingAt}
          ], {}, noop
        );

        t.equal(result.data.length, 3);

        t.equal(result.data[0].date, "Mon 21 May", "first point should be day 1");
        t.deepEqual(result.data[0].startDate, D.relativeTime(wentIdleAt), "should generate start timestamp relative to beginning of day");
        let endOfDay = D.endOfDay(wentIdleAt, true);
        t.deepEqual(result.data[0].endDate, D.relativeTime(endOfDay), "should generate end timestamp relative to beginning of day");
        t.deepEqual(result.data[0].status, "Idle", "should match status");

        t.equal(result.data[1].date, "Tue 22 May", "second point should be day 2");
        let startOfDay = D.startOfDay(wentBuildingAt, true);
        t.deepEqual(result.data[1].startDate, D.relativeTime(startOfDay), "should generate start timestamp relative to beginning of day");
        endOfDay = D.endOfDay(wentBuildingAt, true);
        t.deepEqual(result.data[1].endDate, D.relativeTime(endOfDay), "should generate end timestamp relative to beginning of day");
        t.deepEqual(result.data[1].status, "Idle", "should match status");

        t.equal(result.data[2].date, "Wed 23 May", "third point should be day 3");
        startOfDay = D.startOfDay(wentBuildingAt, true);
        t.deepEqual(result.data[2].startDate, D.relativeTime(startOfDay), "should generate start timestamp relative to beginning of day");
        t.deepEqual(result.data[2].endDate, D.relativeTime(wentBuildingAt), "should generate end timestamp relative to beginning of day");
        t.deepEqual(result.data[2].status, "Idle", "should match status");
      });
    });

    group("Percentage", (test) => {
      test("should compute states for the provided transition", (t) => {
        const initial = new Date(2018, 4, 21, 11, 0, 0, 0);

        const result = AgentsCharts.transitions(
          selector,
          [
            {agent_state: "Idle", transition_time: initial},
            {agent_state: "Building", transition_time: D.addHours(initial, 1, true)},
            {agent_state: "Cancelled", transition_time: D.addHours(initial, 3, true)},
            {agent_state: "Missing", transition_time: D.addHours(initial, 4, true)},
            {agent_state: "LostContact", transition_time: D.addHours(initial, 5, true)},
            {agent_state: "Unknown", transition_time: D.addHours(initial, 6, true)},
            {agent_state: "Idle", transition_time: D.addHours(initial, 7, true)},
          ], {}, noop
        );

        t.true(contains(result.subtitle, "<dl class=\"state-metric-item\"> <dt class=\"key\">Idle</dt> <dd class=\"val\">14.3%</dd> </dl>"), "Should contain idle duration percentage");
        t.true(contains(result.subtitle, "<dl class=\"state-metric-item\"> <dt class=\"key\">Building</dt> <dd class=\"val\">28.6%</dd> </dl>"), "Should contain building duration percentage");
        t.true(contains(result.subtitle, "<dl class=\"state-metric-item\"> <dt class=\"key\">Cancelled</dt> <dd class=\"val\">14.3%</dd> </dl>"), "Should contain cancelled duration percentage");
        t.true(contains(result.subtitle, "<dl class=\"state-metric-item\"> <dt class=\"key\">Missing</dt> <dd class=\"val\">14.3%</dd> </dl>"), "Should contain missing duration percentage");
        t.true(contains(result.subtitle, "<dl class=\"state-metric-item\"> <dt class=\"key\">LostContact</dt> <dd class=\"val\">14.3%</dd> </dl>"), "Should contain lost contact duration percentage");
        t.true(contains(result.subtitle, "<dl class=\"state-metric-item\"> <dt class=\"key\">Unknown</dt> <dd class=\"val\">14.3%</dd> </dl>"), "Should contain unknown duration percentage");
      });

      test("should compute states even when few transition states are missing", (t) => {
        const initial = new Date(2018, 4, 21, 11, 0, 0, 0);
        const result = AgentsCharts.transitions(
          selector,
          [
            {agent_state: "Idle", transition_time: initial},
            {agent_state: "Building", transition_time: D.addDays(initial, 1, true)},
          ], {}, noop
        );

        t.true(contains(result.subtitle, "<dl class=\"state-metric-item\"> <dt class=\"key\">Idle</dt> <dd class=\"val\">100.0%</dd> </dl>"), "Should contain idle duration percentage");
        t.true(contains(result.subtitle, "<dl class=\"state-metric-item\"> <dt class=\"key\">Building</dt> <dd class=\"val\">0.0%</dd> </dl>"), "Should contain building duration percentage");
        t.true(contains(result.subtitle, "<dl class=\"state-metric-item\"> <dt class=\"key\">Cancelled</dt> <dd class=\"val\">0.0%</dd> </dl>"), "Should contain cancelled duration percentage");
        t.true(contains(result.subtitle, "<dl class=\"state-metric-item\"> <dt class=\"key\">Missing</dt> <dd class=\"val\">0.0%</dd> </dl>"), "Should contain missing duration percentage");
        t.true(contains(result.subtitle, "<dl class=\"state-metric-item\"> <dt class=\"key\">LostContact</dt> <dd class=\"val\">0.0%</dd> </dl>"), "Should contain lost contact duration percentage");
        t.true(contains(result.subtitle, "<dl class=\"state-metric-item\"> <dt class=\"key\">Unknown</dt> <dd class=\"val\">0.0%</dd> </dl>"), "Should contain unknown duration percentage");
      });
    });

    test("should create range selector buttons for the provided range", (t) => {
      const range = [
        {id: "7", text: "Last 7 Days", start: moment().subtract(7, "days").format("YYYY-MM-DD"), selected: true},
        {id: "15", text: "Last 15 Days", start: moment().subtract(15, "days").format("YYYY-MM-DD"), selected: false},
        {id: "30", text: "Last 30 Days", start: moment().subtract(30, "days").format("YYYY-MM-DD"), selected: false}
      ];
      const result = AgentsCharts.transitions(selector, [], range, noop);

      const expected = ["<button class=\"selected\" onclick=\"redraw('7')\">Last 7 Days</button>",
        "<button onclick=\"redraw('15')\">Last 15 Days</button>",
        "<button onclick=\"redraw('30')\">Last 30 Days</button>"].join("\n");

      t.equal(result.rangeSelector, expected, "should generate the correct range selector buttons");
    });
  });
});
