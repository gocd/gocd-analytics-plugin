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

export default function xRangeChartSkeleton(options) {
	return {
		chart: {
			type: "xrange",
      zoomType: "xy",
      resetZoomButton: {
        position: {align: "right", verticalAlign: "top", x: -100, y: 15},
        theme: {width: 59, height: 6, r: 3},
        relativeTo: "chart"
      },
      className: _.get(options, "className", "xrange-chart"),
		},
		title: Fragments.chartTitle(options),
		noData: {
			useHTML: true
		},
		credits: {enabled: false},
		xAxis: {
			type: "datetime",
			className: _.get(options, "xAxis.className"),
      title: _.get(options, "xAxis.title"),
      maxPadding: 0.05,
			endOnTick: true,
      labels: { format: "{value:%e %b %H:%M}"},
      tickPositioner: Fragments.makeTickPositioner(50)
		},
    yAxis: {
      title: {
        text: ""
      },
      tickmarkPlacement: "on",
      categories: _.get(options, "yAxis.categories"),
      reversed: false
    },
		tooltip: {
			formatter: _.get(options, "tooltip.formatter"),
      positioner: _.get(options, "tooltip.positioner", Fragments.makeTooltipPositioner()),
			useHTML: true
		},
		time: {
			useUTC: false
		},
		series: options.series
	};
}
