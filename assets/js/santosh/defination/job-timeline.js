import _ from "lodash";

import * as echarts from "echarts";
import {color, groupBy, secondsToHms, uniq, uniqBy, updateChartSize} from "../utils";
import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {sendLinkRequest} from "../../lib/gocd-link-support";

/**
 * @class
 * @interface {ChartInterface}
 */
class JobTimeline {
    data = null;
    chartWidth = null;
    chartHeight = null;

    keys = [];

    constructor(settings, width, height) {
        this.settings = settings;

        console.log('job-timeline constructor settings', settings);

        this.chartWidth = width;
        this.chartHeight = height;
    }

    draw(data) {
        this.data = data;

        const {
            keys, grid, series, elements, failedPipelineCounters
        } = this.prepareData(this.data, this.chartWidth, this.chartHeight);

        this.keys = keys;

        console.log('stage-timeline draw keys, grid, series, elements = ', keys, grid, series, elements);

        var option;

        option = {
            title: {
                text: 'Jobs timeline across stage',
            },
            tooltip: {
                // trigger: "axis",
                axisPointer: {
                    type: "shadow",
                },
                formatter: (params) => {

                    console.log('I will search data ', data);

                    const pipeline_counter = params.name;
                    const job_name = params.seriesName;

                    console.log('formatter params', params);
                    console.log('to find pipeline_counter and stage_name', pipeline_counter, job_name);
                    console.log('typeof pipeline_counter', typeof pipeline_counter, ' typeof stage_name', typeof job_name);

                    const filteredObjects = data.filter(
                        d =>
                            d.pipeline_counter === parseInt(pipeline_counter)
                            &&
                            d.job_name === job_name
                    );
                    console.log('filteredObjects = ', filteredObjects);

                    const latestObject = filteredObjects.reduce((latest, current) => {
                        const latestTimestamp = new Date(latest.scheduled_at);
                        const currentTimestamp = new Date(current.scheduled_at);
                        return currentTimestamp > latestTimestamp ? current : latest;
                    }, filteredObjects[0]);

                    console.log('latestObject = ', latestObject);

                    return `
${params.marker} ${latestObject.job_name}
<br>
🕰 Waiting time:️ ${secondsToHms(latestObject.time_waiting_secs)}
<br>
🔨 Building time: ${secondsToHms(latestObject.time_building_secs)}
<br>
⬆ Total time:️ ${secondsToHms(latestObject.duration_secs)}
<br>
🗳️Result: ${latestObject.result}
<hr>
🤖 Agent UUID: ${latestObject.agent_uuid}
<hr>
Assigned at: ${latestObject.assigned_at}
<br>
Scheduled at: ${latestObject.scheduled_at}
<br>
Completed at: ${latestObject.completed_at}
`;
                }
            },
            color: color, legend: {
                selectedMode: false, top: "bottom",
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
        console.log("1 job-timeline data = ", data);

        console.log('1.2 job-timeline chartWidth, chartHeight', chartWidth, chartHeight);

        const failedJobCounters = Array.from(new Set(data.filter((i) => i.result === "Failed").map((i) => i.pipeline_counter)));
        console.log("failedJobCounters = ", failedJobCounters);

        const passedJobCounters = Array.from(new Set(data.filter((i) => i.result === "Passed").map((i) => i.pipeline_counter)));
        console.log("passedJobCounters = ", passedJobCounters);


        const groupedResult = groupBy(data, "pipeline_counter");

        console.log('2 job-timeline groupedResult = ', groupedResult);

        for (const key in groupedResult) {
            // Sort the array in descending order of scheduled_at
            groupedResult[key].sort((a, b) => new Date(b.scheduled_at) - new Date(a.scheduled_at));

            // Remove duplicates based on stage_name
            groupedResult[key] = uniqBy(groupedResult[key], "job_name");
        }

        console.log('2.1 job-timeline groupedResult = ', groupedResult);

        switch (this.settings.showPipelineCounterResult) {
            case 'Only Passed':
                failedJobCounters.forEach((pipelineCounter) => {
                    delete groupedResult[pipelineCounter];
                });
                break;
            case 'Only Failed':
                passedJobCounters.forEach((pipelineCounter) => {
                    delete groupedResult[pipelineCounter];
                });
                break;
        }

        let keys = Object.keys(groupedResult);
        console.log('3 job-timeline keys = ', keys);

        let jobNames = uniq(Object.values(groupedResult).flatMap((item) => item.map((entry) => entry.job_name)));

        console.log('4 job-timeline jobNames = ', jobNames);

        let rdata = [];

        jobNames.forEach((job) => {
            const d = [];
            Object.values(groupedResult).flatMap((item) => item.map((entry) => {
                if (entry.job_name === job) {
                    // d.push(entry.time_waiting_secs);
                    switch (this.settings.showData) {
                        case 'time_waiting_secs':
                            d.push(entry.time_waiting_secs);
                            break;
                        case 'time_building_secs':
                            d.push(entry.time_building_secs);
                            break;
                        case 'total_time_secs':
                            d.push(entry.duration_secs);
                            break;
                    }
                }
            }));
            rdata.push(d);
        });

        // There should not be negative values in rawData
        const rawData = rdata;

        console.log('5 job-timeline rawData = ', rawData);

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
        const series = jobNames.map((name, sid) => {
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
            keys: keys, grid: grid, series: series, elements: elements, failedPipelineCounters: failedJobCounters
        };
    }

    get_requestParamsPoint(dataIndex, params) {
        return {}
    }

    getNextGraphName() {
        return null;
    }

    insertBreadcrumb() {
        return true;
    }

    breadcrumbCaption() {
        return this.data[0].stage_name;
    }

    breadcrumbTooltip() {
        return "List all Stages";
    }

    getSeriesIndex() {
        return 1;
    }

    nativeClickHandler(transport, params) {
        console.log('nativeClickHandler params', params);

        const pipeline_counter = params.name;
        const job_name = params.seriesName;

        console.log('formatter params', params);
        console.log('to find pipeline_counter and stage_name', pipeline_counter, job_name);
        console.log('typeof pipeline_counter', typeof pipeline_counter, ' typeof stage_name', typeof job_name);

        const filteredObjects = this.data.filter(
            d =>
                d.pipeline_counter === parseInt(pipeline_counter)
                &&
                d.job_name === job_name
        );
        console.log('filteredObjects = ', filteredObjects);

        const latestObject = filteredObjects.reduce((latest, current) => {
            const latestTimestamp = new Date(latest.scheduled_at);
            const currentTimestamp = new Date(current.scheduled_at);
            return currentTimestamp > latestTimestamp ? current : latest;
        }, filteredObjects[0]);


        const linkParam = {
            link_to: 'job_details_page',
            pipeline_name: latestObject.pipeline_name,
            pipeline_counter: latestObject.pipeline_counter,
            stage_name: latestObject.stage_name,
            stage_counter: latestObject.stage_counter,
            job_name: latestObject.job_name
        };
        sendLinkRequest(transport, linkParam);
    }
}

export default JobTimeline;