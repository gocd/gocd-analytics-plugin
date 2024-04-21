import * as echarts from "echarts";
import {secondsToHms, timestampToWords, updateChartSize} from "../utils";

// import { getAreaSeries, getBarSeries } from "../template";
import GET_STACKED_AREA_TEMPLATE from "./stacked-area";
import {
    getAreaSeries,
    getBarSeries,
    getLineSeries,
    getPlainBarSeries,
} from "../template";
import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import momentHumanizeForGocd from "../../lib/moment-humanize-for-gocd";

/**
 * @class
 * @interface {ChartInterface}
 */
class JobPriorityDetails {
    data = null;

    constructor(settings) {
        this.settings = settings;

        console.log('job-priority-details constructor settings', settings);
    }

    draw(data) {
        console.log("JobPriorityDetails draw with data ", data);

        this.data = data;

        // const info = this.prepareData(this.data);
        const info = this.groupAndSummarizeObjects(this.data);

        // console.log("info is ", info);

        // option.tooltip.formatter = this.tooltipFormatter();

        const option = {
            title: {
                text: this.data[0].job_name + ' details'
            },
            tooltip: {
                trigger: "axis",
                axisPointer: {
                    type: "cross",
                    crossStyle: {
                        color: "#999",
                    },
                },
            },
            toolbox: {
                feature: {
                    // dataView: { show: true, readOnly: false },
                    // magicType: { show: true, type: ["line", "bar"] },
                    // restore: { show: true },
                    saveAsImage: {show: true},
                },
            },
            legend: {
                right: "5%"
            },
            xAxis: [
                {
                    type: "category",
                    data: info.map(i => i.date),
                    axisPointer: {
                        type: "shadow",
                    },
                    axisLabel: {
                        formatter: function(value) {
                            return timestampToWords(value);
                        }
                    }
                },
            ],
            yAxis: [
                {
                    type: 'value',
                    name: 'Times',
                    position: 'left',
                    alignTicks: this.settings.alignTicks,
                    axisLine: {
                        show: true,
                        lineStyle: {
                            color: 'blue'
                        }
                    },
                    axisLabel: {
                        formatter: '{value} x'
                    }
                },
                {
                    type: 'value',
                    name: 'Time',
                    position: 'right',
                    alignTicks: this.settings.alignTicks,
                    offset: 10,
                    axisLine: {
                        show: true,
                        lineStyle: {
                            color: 'green'
                        }
                    },
                    axisLabel: {
                        formatter: function (value) {
                            return secondsToHms(value);
                        },
                    }
                }
            ],

            series: [
                {
                    name: 'Times',
                    type: 'line',
                    data: info.map(i => i.times)
                },
                {
                    name: 'Total time spent',
                    type: 'bar',
                    yAxisIndex: 1,
                    data: info.map(i => i.time_waiting_secs + i.time_building_secs),
                    emphasis: {
                        focus: 'series'
                    },
                    markLine: {
                        lineStyle: {
                            type: 'dashed',
                            color: '#FFC0CB'
                        },
                        data: [[{type: 'min'}, {type: 'max'}]]
                    }
                },
                {
                    name: 'Time Waiting',
                    type: 'bar',
                    yAxisIndex: 1,
                    barWidth: 5,
                    stack: 'Total time spent',
                    emphasis: {
                        focus: 'series'
                    },
                    data: info.map(i => i.time_waiting_secs)
                },
                {
                    name: 'Time building',
                    type: 'bar',
                    yAxisIndex: 1,
                    stack: 'Total time spent',
                    emphasis: {
                        focus: 'series'
                    },
                    data: info.map(i => (i.time_building_secs))
                },
            ],
        };

        return option;
    }

    groupAndSummarizeObjects(inputArray) {
        const groupedData = {};

        // Iterate through each object in the input array
        for (const obj of inputArray) {
            const scheduledDate = obj.scheduled_at.slice(0, 10); // Extract date part

            // Initialize or update the grouped data for the scheduled date
            if (!groupedData[scheduledDate]) {
                groupedData[scheduledDate] = {
                    date: scheduledDate,
                    total_time_secs: obj.total_time_secs,
                    time_waiting_secs: obj.time_waiting_secs,
                    times: 1, // Initialize count to 1
                };
            } else {
                // Update existing grouped data
                groupedData[scheduledDate].total_time_secs += obj.total_time_secs;
                groupedData[scheduledDate].time_waiting_secs += obj.time_waiting_secs;
                groupedData[scheduledDate].times++;
            }
        }

        // Convert the grouped data object into an array of values
        const resultArray = Object.values(groupedData);

        // Sort the array by date in ascending order
        resultArray.sort((a, b) => a.date.localeCompare(b.date));

        return resultArray;
    }


    prepareData(data) {

        const legend = ["Waiting Time", "Building Time", "Total Time"];
        const xData = [];
        const data1 = [];
        const data2 = [];
        const data3 = [];
        // const colorData = [];

        const result = this.groupAndSummarizeObjects(data);
        console.log('result = ', result);

        result.forEach((m) => {
            // xData.push(timestampToWords(m.scheduled_at));
            const wait = m.time_waiting_secs;
            const total = m.total_time_secs;
            const build = total - wait;
            xData.push(m.date);
            data1.push(wait);
            data2.push(build);
            data3.push(m.times);
            // colorData.push(m.result);
        });

        function getSeries() {
            const wt = getPlainBarSeries("Waiting Time", data1);
            wt.tooltip = {
                valueFormatter: function (value) {
                    return value + " s";
                },
            };

            const bt = getPlainBarSeries("Building Time", data2);
            bt.tooltip = {
                valueFormatter: function (value) {
                    return value + " s";
                },
            };

            const monitor = getLineSeries("Total Time", data3);

            return [wt, bt, monitor];
        }

        return {
            legends: legend,
            xData: xData,
            series: getSeries(),
        };
    }

    breadcrumbCaption() {
        return "pipeline-priority";
    }

    get_requestParamsPoint(index) {
        console.log('get_requestParamsPoint this.data, index', this.data, index);
        return {pipeline_name: this.data[index].name};
    }

    getNextGraphName() {
        return null;
    }

    getSeriesIndex() {
        return 1;
    }
}

export default JobPriorityDetails;
