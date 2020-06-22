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

import { group } from "tape-plus";
import {el, text, clickOn, MockFactories, MockTransport} from "../helpers/test-helpers";
import H from "js/lib/load-highcharts.js";
import DDS from "js/lib/drilldown-support";

group("DrilldownSupport", (test) => {
  test("charts should be able to drill down through multiple levels", (t, done) => {
    const c = document.createElement("div");

    const Level1 = {
      title: { text: "Level 1" },
      series: [
        { name: "stuff", data: [{ x: 1, y: 1 }] }
      ]
    };

    const assertions = {
      "Level 2": function () {
        const chart = this.chart,
              point = chart.series[0].data[0];

        t.equal(text(c, ".highcharts-title"), "Level 2", "should render drilldown level 2 title");
        t.equal(text(c, ".highcharts-legend-item text"), "Level 2 - series 1", "should render drilldown level 2 legend");
        t.ok(el(c, ".upstream-button"), "drillup button should be present after drilling down");
        t.ok(el(c, ".drilldown-series .highcharts-point"), "should render Level 2 chart point as a drilldown point");
        t.equal(point.series.options.downstream, "Level 3", "point should have reference to next downstream metric");
        point.firePointEvent("click", pointEvent(point));
      },

      "Level 3": function () {
        const chart = this.chart,
              point = chart.series[0].data[0];

        t.equal(text(c, ".highcharts-title"), "Level 3", "should render drilldown level 3 title");
        t.equal(text(c, ".highcharts-legend-item text"), "Level 3 - series 1", "should render drilldown level 3 legend");
        t.ok(el(c, ".upstream-button"), "drillup button should be present after drilling down");
        t.ok(el(c, ".highcharts-series-0 .highcharts-point"), "should render point");
        t.notOk(el(c, ".drilldown-series .highcharts-point"), "point should not be a drilldown point");
        t.notOk(point.series.options.downstream, "point should not have a downstream metric reference");
        done();
      }
    };

    const config = DDS.withDrilldown(Level1, ["Level 2", "Level 3"], new MockFactories(assertions), new MockTransport([
      { data: [{ x: 2, y: 2 }] },
      { data: [{ x: 3, y: 3 }] }
    ]));

    H.chart(c, config, function () {
      const chart = this,
            point = chart.series[0].data[0];

      t.equal(text(c, ".highcharts-title"), "Level 1", "should render top-level chart title");
      t.equal(text(c, ".highcharts-legend-item text"), "stuff", "should render top-level chart legend");
      t.notOk(el(c, ".upstream-button"), "top-level chart should not have a drillup button");
      t.ok(el(c, ".drilldown-series .highcharts-point"), "should render top-level chart point as a drilldown point");
      t.equal(point.series.options.downstream, "Level 2", "point should have reference to next downstream metric");

      point.firePointEvent("click", pointEvent(point));
    });
  });

  test("charts should be able to drill back up", (t, done) => {
    const c = document.createElement("div");

    const Level1Assertions = [
      function firstVisit() {
        const chart = this.chart,
              point = chart.series[0].data[0];

        t.equal(text(c, ".highcharts-title"), "Level 1", "should render top-level chart title");
        t.equal(point.series.options.downstream, "Level 2", "point should have reference to next downstream metric");
        t.notOk(el(c, ".upstream-button"), "drillup button should not be present");

        point.firePointEvent("click", pointEvent(point)); // drill down to next level
      },
      function returnAfterDrillup() {
        const chart = this.chart,
              point = chart.series[0].data[0];

        t.equal(text(c, ".highcharts-title"), "Level 1", "should have returned to top-level");
        t.equal(point.series.options.downstream, "Level 2", "point still references next downstream metric");
        t.notOk(el(c, ".upstream-button"), "drillup button should not be present");

        done();
      }
    ];

    const DrilldownAssertions = {
      "Level 2": function () {
        const chart = this.chart,
              point = chart.series[0].data[0];

        t.equal(text(c, ".highcharts-title"), "Level 2", "should render drilldown level 2 title");
        t.equal(text(c, ".highcharts-legend-item text"), "Level 2 - series 1", "should render drilldown level 2 legend");
        t.ok(el(c, ".upstream-button"), "drillup button should be present after drilling down");
        t.notOk(point.series.options.downstream, "point should not have a downstream metric reference");

        clickOn(c, ".upstream-button");
      }
    };

    const Level1 = {
      title: { text: "Level 1" },
      plotOptions: { series: { events: { afterAnimate: function runAssertions() { Level1Assertions.shift().apply(this); } } } },
      series: [
        { name: "stuff", data: [{ x: 1, y: 1 }] }
      ]
    };

    const config = DDS.withDrilldown(Level1, ["Level 2"], new MockFactories(DrilldownAssertions), new MockTransport([
      { data: [{ x: 2, y: 2 }] }
    ]));

    H.chart(c, config);
  });
});

// creates a fake event for firePointEvent()
function pointEvent(point) {
  function noop() {}
  return {
    type: "click",
    point,
    target: point.graphic.element,
    stopPropagation: noop,
    preventDefault: noop
  };
}
