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

const PAD = 20;

function marginTop(options) {
  return options.title ? (options.subtitle ? 145 : 70) : null;
}

export default function AreaChartSkeleton(options) {
  const series = _.filter(options.series, function (s) {
    return "mean build time" !== s.name.toLowerCase();
  });

  function autoFit() {
    if (_.get(series, "[0].data", []).length < 4) return;

    const chart = this, ceiling = Fragments.calculateAxisMax(series);

    if (null !== ceiling) {
      chart.yAxis[0].setExtremes(0, ceiling);
      chart.showResetZoom();
    }
  }

  return {
    variants: options.variants,
    chart: {
      type: "area",
      zoomType: "xy",
      panning: true,
      panKey: "shift",
      resetZoomButton: {
        // be explicit about align & verticalAlign so it survives drillup/down
        position: {align: "right", verticalAlign: "top", x: -20, y: 150},
        theme: {width: 59, height: 6, r: 3},
        relativeTo: "chart"
      },
      spacing: [PAD, PAD, PAD, PAD],
      marginTop: marginTop(options),
      className: _.get(options, "className", "area-chart"),
      events: {
        load: autoFit,
        range: autoFit
      }
    },
    title: Fragments.chartTitle(options),
    subtitle: Fragments.chartSubtitle(options),
    noData: {
      useHTML: true
    },
    credits: { enabled: false },
    plotOptions: {
      series: {
        stacking: "normal",
        marker: { radius: 4.5 }
      }
    },
    xAxis: {
      title: _.get(options, "xAxis.title"),
      className: _.get(options, "xAxis.className"),
      categories: _.get(options, "xAxis.categories"),
      tickPositioner: Fragments.makeTickPositioner(50 /* dates are approx 50 px wide */),
      labels: { formatter: options.xAxis.formatter, skipThresh: 42, autoRotation: false }
    },
    yAxis: {
      min: 0,
      title: _.get(options, "yAxis.title"),
      labels: { formatter: _.get(options, "yAxis.formatter"), useHTML: false }
    },
    tooltip: {
      shared: _.get(options, "tooltip.shared", true),
      crosshairs: true,
      useHTML: true,
      positioner: _.get(options, "tooltip.positioner", Fragments.makeTooltipPositioner()),
      formatter: _.get(options, "tooltip.formatter")
    },
    series: options.series,
    legend: Fragments.chartLegend(options)
  };
}
