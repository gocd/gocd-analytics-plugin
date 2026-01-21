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

import _ from "lodash";
import Fragments from "./fragments";
import Formatters from "../formatters";

const PAD = 20;

/** @private filters whether or not to apply the resize  */
function accept(chart) {
  return "bar" === chart.options.chart.type &&
    !!_.get(chart, "series[0].data", []).length;
}

/**
 * @private
 * calculates a divisor to derive the bar thickness
 * from allocated space
 */
function divisor(v) {
  // you'll have to take my word on this, but this
  // yields an inverted, gentle, logarithmic descent
  // over the practical bounds for plotHeight.
  //
  // I made a fiddle: http://jsfiddle.net/8fg8cxh0/13/
  return -0.14 + Math.log(3.5 * v) / (v / 180);
}

/** @private keeps value within min and max */
function enforceBounds(value, min, max) {
  return Math.min(Math.max(min, value), max);
}

function fontSize(thick) {
  let className = "small";
  let adj = -3;

  if (thick > 24) {
    className = "medium";
    adj = -5;
  }

  if (thick > 48) {
    className = "large";
    adj = -8;
  }

  return {className, adj};
}

function resizeBarsAndLabels() {
  const MIN = 10, MAX = 100, chart = this;

  if (accept(chart)) {

    const vert = chart.plotHeight,
     numPoints = chart.series[0].data.length,
         alloc = vert / numPoints,
         thick = enforceBounds(Math.floor(alloc / divisor(vert)), MIN, MAX),
          font = fontSize(thick),
        offset = font.adj - Math.floor(thick / 2);

    const changes = {
      plotOptions: { bar: { maxPointWidth: thick, pointWidth: thick } },
      xAxis: { labels: { y: offset }, className: font.className }
    };

    chart.update(changes);
  }
}

function marginTop(options) {
  return options.title ? (options.subtitle ? 100 : 70) : null;
}

const ROTATION_STEPS = [];
for (let i = 0, step = 5, limit = -45; i >= limit; ROTATION_STEPS.push(i), i -= step);

export default function BarChartSkeleton(options) {
  return {
    variants: options.variants,
    resizer: resizeBarsAndLabels,
    chart: {
      type: "bar",
      spacing: [PAD, PAD, PAD, PAD],
      marginTop: marginTop(options),
      className: _.get(options, "className", "bar-chart")
    },
    title: Fragments.chartTitle(options),
    subtitle: Fragments.chartSubtitle(options),
    noData: {
      useHTML: true
    },
    credits: { enabled: false },
    xAxis: {
      labels: {
        align: "left",
        x: 3,
        reserveSpace: false,
        formatter: _.get(options, "xAxis.formatter"),
        style: { width: window.screen.width }
      },
      tickLength: 0,
      categories: _.get(options, "xAxis.categories")
    },
    yAxis: {
      min: 0,
      title: { text: "Duration" },
      tickPositioner: Fragments.makeTickPositioner(55 /* durations are 50 - 60 px wide */),
      labels: {
        autoRotation: false,
        useHTML: true,
        formatter: Formatters.durationAxisFormatter
      }
    },
    tooltip: {
      shared: _.get(options, "tooltip.shared", true),
      crosshairs: true,
      useHTML: true,
      positioner: _.get(options, "tooltip.positioner", Fragments.makeTooltipPositioner()),
      formatter: _.get(options, "tooltip.formatter")
    },
    plotOptions: {
      series: {
        stacking: "normal"
      }
    },
    series: options.series,
    legend: Fragments.chartLegend(Object.assign({legend: {margin: 3}}, options))
  };
}
