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

const PAD = 10;

export default function SparklineSkeleton(options) {
    return {
        chart: {
            noResize: true,
            type: "area",
            borderWidth: 0,
            margin: [PAD, PAD, PAD, PAD],
            // width: 800,
            height: 60,
            style: {
                overflow: "visible"
            },

            // small optimalization, saves 1-2 ms each sparkline
            skipClone: true
        },
        title: {
            text: ""
        },
        credits: {
            enabled: false
        },
        xAxis: {
            labels: {
                enabled: false
            },
            title: {
                text: null
            },
            startOnTick: false,
            endOnTick: false,
            tickPositions: []
        },
        yAxis: {
            endOnTick: false,
            startOnTick: false,
            labels: {
                enabled: false
            },
            title: {
                text: null
            },
            tickPositions: [0]
        },
        legend: {
            enabled: false
        },
        tooltip: {
            shared: true,
            useHTML: true,
            containerClassOnHide: "hidden-tooltip",
            hideDelay: 0,
            outside: true,
            positioner: _.get(options, "tooltip.positioner", Fragments.makeTooltipPositioner()),
            formatter: _.get(options, "tooltip.formatter")
        },
        series: options.series,
        plotOptions: {
            series: {
                animation: false,
                lineWidth: 1,
                shadow: false,
                states: {
                    hover: {
                        lineWidth: 1
                    }
                },
                marker: {
                    radius: 3.5,
                    states: {
                        hover: {
                            radius: 5
                        }
                    }
                },
                fillOpacity: 0.25
            }
        }
    };
}
