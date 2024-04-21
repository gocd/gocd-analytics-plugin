import _ from "lodash";

import * as echarts from "echarts";
import {asyncRequest, color, groupBy, secondsToHms, uniq, uniqBy, updateChartSize} from "../utils";
import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import momentHumanizeForGocd from "../../lib/moment-humanize-for-gocd";
import Console from "../Console";

/**
 * @class
 * @interface {ChartInterface}
 */
const c = new Console('stage-timeline.js', 'dev');

class StageTimeline {
    data = null;
    chartWidth = null;
    chartHeight = null;

    keys = [];

    constructor(settings, width, height, footer) {
        this.settings = settings;

        this.chartWidth = width;
        this.chartHeight = height;

        this.footer = footer;

        console.log('5. stage-timeline after init');
    }

    getTooltip(counter, stage_name) {
        return counter;

        const filteredObjects = this.data.filter(obj => obj.pipeline_counter === counter && obj.stage_name === stage_name);

        const latestObject = filteredObjects.reduce((latest, current) => {
            const latestTimestamp = new Date(latest.scheduled_at);
            const currentTimestamp = new Date(current.scheduled_at);
            return currentTimestamp > latestTimestamp ? current : latest;
        }, filteredObjects[0]);

        console.log('filteredObjects = ', filteredObjects);
        console.log('latestObject = ', latestObject);

        return latestObject;
    }

    draw(data) {

        this.data = data;

        const filteredObjects = data.filter(obj => obj.pipeline_counter === 14 && obj.stage_name === 'stage-6');
        c.log('filteredObjects = ', filteredObjects);

        const {
            keys, grid, series, elements, failedPipelineCounters
        } = this.prepareData(this.data, this.chartWidth, this.chartHeight);

        this.keys = keys;

        c.log('stage-timeline draw keys, grid, series, elements = ', keys, grid, series, elements);
        console.log('stage-timeline draw keys, grid, series, elements = ', keys, grid, series, elements);

        var option;

        option = {
            title: {
                text: 'Stage timeline for pipeline'
            },
            tooltip: {
                // trigger: "axis",
                axisPointer: {
                    type: "shadow",
                },
                formatter: (params) => {

                    c.log('I will search data ', data);

                    const pipeline_counter = params.name;
                    const stage_name = params.seriesName;

                    c.log('formatter params', params);
                    c.log('to find pipeline_counter and stage_name', pipeline_counter, stage_name);
                    c.log('typeof pipeline_counter', typeof pipeline_counter, ' typeof stage_name', typeof stage_name);

                    const filteredObjects = data.filter(
                        d =>
                            d.pipeline_counter === parseInt(pipeline_counter)
                            &&
                            d.stage_name === stage_name
                    );
                    c.log('filteredObjects = ', filteredObjects);

                    const latestObject = filteredObjects.reduce((latest, current) => {
                        const latestTimestamp = new Date(latest.scheduled_at);
                        const currentTimestamp = new Date(current.scheduled_at);
                        return currentTimestamp > latestTimestamp ? current : latest;
                    }, filteredObjects[0]);

                    c.log('latestObject = ', latestObject);

                    return `
${params.marker} ${latestObject.stage_name}
<br>
🕰 Waiting time:️ ${secondsToHms(latestObject.time_waiting_secs)}
<br>
🔨 Building time: ${secondsToHms(latestObject.total_time_secs - latestObject.time_waiting_secs)}
<br>
⬆ Total time:️ ${secondsToHms(latestObject.total_time_secs)}
<br>
🗳️Result: ${latestObject.result}
<hr>
👦 Approved by: ${latestObject.approved_by}
<hr>
Scheduled at: ${latestObject.scheduled_at}
<br>
Completed at: ${latestObject.completed_at}
`;
                }
            },
            color: color, legend: {
                selectedMode: false, top: "bottom", show: this.settings.showLegend === "Show" ? true : false
            }, grid, yAxis: {
                type: "value",
            }, xAxis: {
                type: "category", data: keys,
            }, series, graphic: {
                elements,
            },
        };

        return option;
    }

    prepareData(data, chartWidth, chartHeight) {
        c.log("1 stage-timeline data = ", data);
        console.log("1 stage-timeline data = ", data);

        c.log('1.2 stage-timeline chartWidth, chartHeight', chartWidth, chartHeight);

        const failedPipelineCounters = Array.from(new Set(data.filter((i) => i.result === "Failed").map((i) => i.pipeline_counter)));
        c.log("failedPipelineCounters = ", failedPipelineCounters);

        const passedPipelineCounters = Array.from(new Set(data.filter((i) => i.result === "Passed").map((i) => i.pipeline_counter)));
        c.log("passedPipelineCounters = ", passedPipelineCounters);

        const cancelledPipelineCounters = Array.from(new Set(data.filter((i) => i.result === "Cancelled").map((i) => i.pipeline_counter)));
        c.log("cancelledPipelineCounters = ", cancelledPipelineCounters);


        const groupedResult = groupBy(data, "pipeline_counter");

        c.log('2 stage-timeline groupedResult = ', groupedResult);
        console.log('2 stage-timeline groupedResult = ', groupedResult);

        for (const key in groupedResult) {
            // Sort the array in descending order of scheduled_at
            groupedResult[key].sort((a, b) => new Date(b.scheduled_at) - new Date(a.scheduled_at));

            // Remove duplicates based on stage_name
            groupedResult[key] = uniqBy(groupedResult[key], "stage_name");
        }

        c.log('2.1 stage-timeline groupedResult = ', groupedResult);

        let footerMessage = '';

        switch (this.settings.showPipelineCounterResult) {
            case 'Only Passed':
                failedPipelineCounters.forEach((pipelineCounter) => {
                    delete groupedResult[pipelineCounter];
                });
                cancelledPipelineCounters.forEach((pipelineCounter) => {
                    delete groupedResult[pipelineCounter];
                });
                footerMessage = "Instances ignored - Failed: " + failedPipelineCounters.length + " Cancelled: " + cancelledPipelineCounters.length;
                break;
            case 'Only Failed':
                passedPipelineCounters.forEach((pipelineCounter) => {
                    delete groupedResult[pipelineCounter];
                });
                cancelledPipelineCounters.forEach((pipelineCounter) => {
                    delete groupedResult[pipelineCounter];
                });
                footerMessage = "Instances ignored - Passed: " + passedPipelineCounters.length + " Cancelled: " + cancelledPipelineCounters.length;
                break;
            case 'Only Cancelled':
                passedPipelineCounters.forEach((pipelineCounter) => {
                    delete groupedResult[pipelineCounter];
                });
                failedPipelineCounters.forEach((pipelineCounter) => {
                    delete groupedResult[pipelineCounter];
                });
                footerMessage = "Instances ignored - Passed: " + passedPipelineCounters.length + " Failed: " + failedPipelineCounters.length;
                break;
        }

        this.footer.showMessage(footerMessage, "Info", false);

        let keys = Object.keys(groupedResult);
        c.log('3 stage-timeline keys = ', keys);

        let stageNames = uniq(Object.values(groupedResult).flatMap((item) => item.map((entry) => entry.stage_name)));

        c.log('4 stage-timeline stageNames = ', stageNames);

        let rdata = [];

        stageNames.forEach((stage) => {
            const d = [];
            Object.values(groupedResult).flatMap((item) => item.map((entry) => {
                // if (entry.stage_name === stage) d.push(entry.time_waiting_secs);
                if (entry.stage_name === stage) {
                    switch (this.settings.showData) {
                        case 'time_waiting_secs':
                            d.push(entry.time_waiting_secs);
                            break;
                        case 'time_building_secs':
                            d.push(entry.total_time_secs - entry.time_waiting_secs);
                            break;
                        case 'total_time_secs':
                            d.push(entry.total_time_secs);
                            break;
                    }
                }
            }));
            rdata.push(d);
        });

        // There should not be negative values in rawData
        const rawData = rdata;

        c.log('5 stage-timeline rawData = ', rawData);
        console.log('5 stage-timeline rawData = ', rawData);

        const totalData = [];
        for (let i = 0; i < rawData[0].length; ++i) {
            let sum = 0;
            for (let j = 0; j < rawData.length; ++j) {
                sum += rawData[j][i];
            }
            totalData.push(sum);
        }

        const grid = {
            left: 100, right: 100, top: 50, bottom: 50,
        };
        const gridWidth = chartWidth - grid.left - grid.right;
        const gridHeight = chartHeight - grid.top - grid.bottom;
        const categoryWidth = gridWidth / rawData[0].length;
        const barWidth = categoryWidth * 0.6;
        const barPadding = (categoryWidth - barWidth) / 2;
        const series = stageNames.map((name, sid) => {
            return {
                name, type: "bar", stack: "total", barWidth: "60%", label: {
                    show: true, formatter: (params) => Math.round(params.value * 1000) / 10 + "%",
                }, data: rawData[sid].map((d, did) => totalData[did] <= 0 ? 0 : d / totalData[did]),
            };
        });
        // const color = ["#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de"];

        const elements = [];
        for (let j = 1, jlen = rawData[0].length; j < jlen; ++j) {
            const leftX = grid.left + categoryWidth * j - barPadding;
            const rightX = leftX + barPadding * 2;
            let leftY = grid.top + gridHeight;
            let rightY = leftY;
            for (let i = 0, len = series.length; i < len; ++i) {
                const points = [];
                const leftBarHeight = (rawData[i][j - 1] / totalData[j - 1]) * gridHeight;
                points.push([leftX, leftY]);
                points.push([leftX, leftY - leftBarHeight]);
                const rightBarHeight = (rawData[i][j] / totalData[j]) * gridHeight;
                points.push([rightX, rightY - rightBarHeight]);
                points.push([rightX, rightY]);
                points.push([leftX, leftY]);
                leftY -= leftBarHeight;
                rightY -= rightBarHeight;
                elements.push({
                    type: "polygon", shape: {
                        points,
                    }, style: {
                        fill: color[i], opacity: 0.25,
                    },
                });
            }
        }

        return {
            keys: keys, grid: grid, series: series, elements: elements, failedPipelineCounters: failedPipelineCounters
        };
    }

    get_requestParamsPoint(dataIndex, params) {
        c.log('stage-timeline clicked params = ', params);
        c.log('this.keys = ', this.keys);
        return {
            "stage_name": params.seriesName,
            "pipeline_counter_start": this.keys[0],
            "pipeline_counter_end": this.keys[this.keys.length - 1],
        }
    }

    getNextGraphName() {
        return "JobsTimeline";
    }

    insertBreadcrumb() {
        return false;
    }

    breadcrumbCaption() {
        return "Stages";
    }

    breadcrumbTooltip() {
        return "List all Stages";
    }

    getSeriesIndex() {
        return 0;
    }
}

export default StageTimeline;