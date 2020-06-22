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
import _ from "lodash";
import moment from "js/lib/moment-humanize-for-gocd.js";
import RangeSelectorSupport from "js/lib/range-selector-support";
import {el, els, text, clickOn, chartStub, MockTransport} from "../helpers/test-helpers";
import H from "js/lib/load-highcharts.js";

const testParams = {
  type: "testType",
  metric: "testMetric"
};

group("RangeSelectorSupport", (test) => {

  test("should add elements to container", (t) => {
    const chart = chartStub();
    RangeSelectorSupport.inject(null, chart, {});
    t.ok(chart.ranger, "should create elements and assign to chart ref");
    t.equal(chart.ranger, el(chart.container, ".range-selector"), "range selector should have correct class and be attached to the chart container");
    t.deepEqual(_.map(els(chart.ranger, ".range-button"), (el) => text(el)), ["Last 24 Hours", "Last 7 Days", "Last 30 Days", "All"], "should have the correct button options");
    t.equal(text(chart.ranger, ".selected"), "Last 30 Days", "default button should be selected");
  });

  test("clicking button should invoke request with params", (t, done) => {
    const chart = chartStub(), transport = new MockTransport({data: [1, 2, 3]});
    RangeSelectorSupport.inject(transport, chart, {params: testParams});

    transport.inspectRequest((key, params) => {
      t.equal(els(chart.ranger, ".selected").length, 1, "should only allow one range button selection");
      t.ok(last7days(chart).classList.contains("selected"), "click should result in the current button marked as selected");

      t.equal(key, "fetch-analytics", "request should ask to fetch data");
      t.equal(params.type, "testType", "type param should be passed through");
      t.equal(params.metric, "testMetric", "metric param should be passed through");
      t.equal(moment(params.end).diff(moment(params.start), "days"), 7, "range selection should have appended the correct range params");
      done();
    });

    clickOn(last7days(chart));
  });

  test("shows no data when range returns empty set", (t, done) => {
    let displayedNoDataMsg = false;
    const chart = chartStub({ showNoData: () => { displayedNoDataMsg = true; } }), transport = new MockTransport({data: []});
    RangeSelectorSupport.inject(transport, chart, {});

    transport.afterDone((data) => {
      t.deepEqual(data, [], "should have received an empty data set");
      t.ok(displayedNoDataMsg, "should show the no-data message");
      done();
    });

    clickOn(last7days(chart));
  });

  test("series data is updated upon range selection", (t, done) => {
    const s1 = new AssertionSeries("s1", function onSetData(data) {
      t.deepEqual(data, [ { y: 3 }, { y: 5 }, { y: 7 } ], "setData() should be called with the transformed data");
      done();
    });

    const chart = chartStub({ series: [s1] }), transport = new MockTransport({data: [3, 5, 7]});
    RangeSelectorSupport.inject(transport, chart, {transforms: s1.transforms});

    clickOn(last7days(chart));
  });

  test("fires range event after range selected", (t, done) => {
    const chart = chartStub(), transport = new MockTransport({data: []});

    H.addEvent(chart, "range", function() {
      t.ok(true, "should call the range event on chart");
      done();
    });

    RangeSelectorSupport.inject(transport, chart, {});

    clickOn(last7days(chart));
  });

});

function last7days(chart) {
  return els(chart.ranger, ".range-button").find((el) => "Last 7 Days" === text(el));
}

function AssertionSeries(name, fn) {
  this.name = name;
  this.setData = function(data) {
    this.data = data;
    fn(data);
  };

  this.transforms = {};
  this.transforms[name] = (data) => _.map(data, (d) => { return { y: d }; });
}
