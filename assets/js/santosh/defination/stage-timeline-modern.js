import _ from "lodash";

import * as echarts from "echarts";
import {asyncRequest, color, groupBy, secondsToHms, uniq, uniqBy, updateChartSize} from "../utils";
import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import momentHumanizeForGocd from "../../lib/moment-humanize-for-gocd";
import Console from "../Console";
import StackedBar from "../stacked-bar";

/**
 * @class
 * @interface {ChartInterface}
 */
const c = new Console('stage-timeline-modern.js', 'dev');

class StageTimelineModern {
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
        console.log('settings = ', settings);
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

        const {
            keys, grid, series, elements, failedPipelineCounters
        } = this.prepareData(this.data, this.chartWidth, this.chartHeight);

        if (keys === undefined) {
            return {};
        }

        this.keys = keys;

        // c.logs('stage-timeline draw keys, grid, series, elements = ', keys, grid, series, elements);
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

                    // console.log('params', params);
                    // c.log('I will search data ', data);

                    const pipeline_counter = params.name;
                    const stage_name = params.seriesName;

                    // c.log('formatter params', params);
                    // c.log('to find pipeline_counter and stage_name', pipeline_counter, stage_name);
                    // c.log('typeof pipeline_counter', typeof pipeline_counter, ' typeof stage_name', typeof stage_name);

                    const allFilteredObjects = data.filter(
                        d => d.stage_name === stage_name
                            &&
                            keys.some(key => parseInt(key) === parseInt(d.pipeline_counter)
                            ));


                    // console.log('allFilteredObjects', allFilteredObjects);

                    const filteredObjects = data.filter(
                        d =>
                            d.pipeline_counter === parseInt(pipeline_counter)
                            &&
                            d.stage_name === stage_name
                    );
                    // c.log('filteredObjects = ', filteredObjects);

                    const latestObject = filteredObjects.reduce((latest, current) => {
                        const latestTimestamp = new Date(latest.scheduled_at);
                        const currentTimestamp = new Date(current.scheduled_at);
                        return currentTimestamp > latestTimestamp ? current : latest;
                    }, filteredObjects[0]);

                    // c.log('latestObject = ', latestObject);

                    return `
${params.marker} ${latestObject.stage_name} ${filteredObjects.length > 1 ? ` 🙈 +${filteredObjects.length - 1}` : ``}
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
<hr>
🛤️ 🕰 Series Waiting time:️ ${secondsToHms(filteredObjects.reduce((acc, item) => acc + item.time_waiting_secs, 0))}
<br>
🛤️ 🔨 Series Building time:️ ${secondsToHms(filteredObjects.reduce((acc, item) => acc + (item.total_time_secs - item.time_waiting_secs), 0))}
<br>
🛤️ ⬆ Series Total time:️ ${secondsToHms(filteredObjects.reduce((acc, item) => acc + item.total_time_secs, 0))}
`;
                }
            },
            color: color, legend: {
                selectedMode: true, top: "bottom", show: this.settings.showLegend === "Show" ? true : false
            }, grid, yAxis: {
                type: "value",
                max: 1
            }, xAxis: {
                type: "category", data: keys,
            }, series,
            // graphic: {
            //     elements,
            // },
        };

        if (this.settings.showPipelineCounterResult === 'Only Passed') {
            option.graphic = {
                elements,
            }
        }


        return option;
    }

    groupDataByPipelineCounter(raw_data) {
        return groupBy(raw_data, 'pipeline_counter');
    }

    sortArrayInDescendingOrderOfScheduledAt(pipeline_counter_group) {
        const array = _.cloneDeep(pipeline_counter_group);
        for (const key in array) {
            // Sort the array in descending order of scheduled_at
            array[key].sort((a, b) => new Date(b.scheduled_at) - new Date(a.scheduled_at));
        }
        return array;
    }

    removeDuplicateStages(date_sorted_pipeline_counter_group) {
        const array = _.cloneDeep(date_sorted_pipeline_counter_group);
        for (const key in array) {
            // Remove duplicates based on stage_name
            array[key] = uniqBy(array[key], 'stage_name');
        }
        return array;
    }

    getUniqueStageNames(unique_sorted_pipeline_counter_group) {
        return uniq(Object.values(unique_sorted_pipeline_counter_group).flatMap((item) => item.map((entry) => entry.stage_name)));
    }

    getPipelineCountersByResult(result) {
        return Array.from(new Set(this.data.filter((i) => i.result === result).map((i) => i.pipeline_counter)));
    }

    getFailedPipelineCounters() {
        return this.getPipelineCountersByResult('Failed');
    }

    getPassedPipelineCounters() {
        return this.getPipelineCountersByResult('Passed');
    }

    getCancelledPipelineCounters() {
        return this.getPipelineCountersByResult('Cancelled');
    }

     filterObjectsWithOnlyResult(obj, result) {
         for (let prop in obj) {
             if (obj.hasOwnProperty(prop)) {
                 obj[prop] = obj[prop].filter(item => item.result === result);
             }
         }
         return obj;
    }

    filterDataBySettingsResultVisibility(unique_sorted_pipeline_counter_group) {
        let array = _.cloneDeep(unique_sorted_pipeline_counter_group);
        console.log('array before filter', array);
        console.log('array length', array.length);
        switch (this.settings.showPipelineCounterResult) {
            case 'Only Passed':
                console.log('Only Passed');
                this.getFailedPipelineCounters().forEach(pipeline_counter => delete array[pipeline_counter]);
                this.getCancelledPipelineCounters().forEach(pipeline_counter => delete array[pipeline_counter]);
                break;
            case 'Any Passed':
                array = this.filterObjectsWithOnlyResult(array, 'Passed');
                console.log('Any Passed array: ', array);
                break;
            case 'Only Failed':
                console.log('Only Failed');
                this.getPassedPipelineCounters().forEach(pipeline_counter => delete array[pipeline_counter]);
                this.getCancelledPipelineCounters().forEach(pipeline_counter => delete array[pipeline_counter]);
                break;
            case 'Only Cancelled':
                console.log('Only Cancelled');
                this.getPassedPipelineCounters().forEach(pipeline_counter => delete array[pipeline_counter]);
                this.getFailedPipelineCounters().forEach(pipeline_counter => delete array[pipeline_counter]);
                break;
        }

        console.log('array after filter', array);
        console.log('array length', array.length);

        return array;
    }

    getFooterMessageBySettingsResultVisibility() {
        switch (this.settings.showPipelineCounterResult) {
            case 'Only Passed':
                return "Instances ignored - Failed: " + this.getFailedPipelineCounters().length + " Cancelled: " + this.getCancelledPipelineCounters().length;
            case 'Only Failed':
                return "Instances ignored - Passed: " + this.getPassedPipelineCounters().length + " Cancelled: " + this.getCancelledPipelineCounters().length;
            case 'Only Cancelled':
                return "Instances ignored - Passed: " + this.getPassedPipelineCounters().length + " Failed: " + this.getFailedPipelineCounters().length;
        }
    }

    prepareFilteredRawDataBasedOnSettingsShowData(stage_names, final_data) {
        let rdata = [];
        stage_names.forEach((stage) => {
            const d = [];
            Object.values(final_data).flatMap((item) => item.map((entry) => {
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
                } else {
                    d.push(0);
                }
            }));
            rdata.push(d);
        });

        return rdata;
    }

    newPrepareBasedOnPipelineCounter(stage_names, final_data) {
        console.log('stage_count', stage_names.length);
        console.log('final_data', final_data);

        let rdata = [];

        stage_names.forEach(s => rdata.push([]));

        for (const key in final_data) {

            const pipeline_counter_array = final_data[key];
            console.log('pipeline_counter_array', pipeline_counter_array);

            stage_names.forEach((stage, index) => {
                // const pipeline = pipeline_counter_data.filter((entry) => {
                console.log('Searching for stage_name ', stage);

                const pipeline = pipeline_counter_array.find(pipeline =>
                    pipeline.stage_name.toString() === stage.toString()
                );
                // if (pipeline.length === 0) {
                if (pipeline === undefined) {
                    // console.log('❌ No stage name found for stage_name ', stage);
                    rdata[index].push(0);
                    // d.push(0);
                } else {
                    // console.log('✅ stage name found and time_waiting_secs is', pipeline.time_waiting_secs);
                    switch (this.settings.showData) {
                        case 'time_waiting_secs':
                            // d.push(pipeline.time_waiting_secs);
                            rdata[index].push(pipeline.time_waiting_secs);
                            break;
                        case 'time_building_secs':
                            // d.push(pipeline.total_time_secs - pipeline.time_waiting_secs);
                            rdata[index].push(pipeline.total_time_secs - pipeline.time_waiting_secs);
                            break;
                        case 'total_time_secs':
                            // d.push(pipeline.total_time_secs);
                            rdata[index].push(pipeline.total_time_secs);
                            break;
                    }
                }
            });

            console.log('rdata = ', rdata);

        }

        console.log('final rdata = ', rdata);

        return rdata;

    }


    prepareData(data, chartWidth, chartHeight) {
        c.logs("1 stage-timeline data = ", data);
        console.log("1 stage-timeline data = ", data);

        c.logs('1.2 stage-timeline chartWidth, chartHeight', chartWidth, chartHeight);

        const pipeline_counter_group = this.groupDataByPipelineCounter(data);
        console.log('pipeline_counter_group', pipeline_counter_group);

        const date_sorted_pipeline_counter_group = this.sortArrayInDescendingOrderOfScheduledAt(pipeline_counter_group);
        console.log('date_sorted_pipeline_counter_group', date_sorted_pipeline_counter_group);

        const unique_sorted_pipeline_counter_group = this.removeDuplicateStages(date_sorted_pipeline_counter_group);
        console.log('unique_sorted_pipeline_counter_group', unique_sorted_pipeline_counter_group);

        const final_data = this.filterDataBySettingsResultVisibility(unique_sorted_pipeline_counter_group);
        console.log('final_data', final_data);

        const footerMessage = this.getFooterMessageBySettingsResultVisibility();

        this.footer.showMessage(footerMessage, "Info", false);

        let keys = Object.keys(final_data);
        c.logs('3 stage-timeline keys = ', keys);

        if (keys.length === 0) {
            return {keys: undefined}
        }

        const stage_names = this.getUniqueStageNames(final_data);

        // There should not be negative values in rawData
        // const rawData = this.prepareFilteredRawDataBasedOnSettingsShowData(stage_names, final_data);
        const rawData = this.newPrepareBasedOnPipelineCounter(stage_names, final_data);

        c.logs('stage-timeline-modern rawData, length = ', rawData, rawData.length);

        const grid = {
            left: 100, right: 100, top: 50, bottom: 50,
        };

        if (stage_names.length === 1) {
            return {
                keys: keys, grid: grid, series: {
                    data: rawData.flat(),
                    type: 'bar',
                    showBackground: true,
                    backgroundStyle: {
                        color: 'rgba(180, 180, 180, 0.2)'
                    }
                }, elements: null
            }
        }

        const totalData = [];
        for (let i = 0; i < rawData[0].length; ++i) {
            let sum = 0;
            for (let j = 0; j < rawData.length; ++j) {
                sum += rawData[j][i];
            }
            totalData.push(sum);
        }

        const gridWidth = chartWidth - grid.left - grid.right;
        const gridHeight = chartHeight - grid.top - grid.bottom;
        const categoryWidth = gridWidth / rawData[0].length;
        const barWidth = categoryWidth * 0.6;
        const barPadding = (categoryWidth - barWidth) / 2;
        const series = stage_names.map((name, sid) => {
            return {
                name, type: "bar", stack: "total", barWidth: "60%", label: {
                    show: true, formatter: (params) => Math.round(params.value * 1000) / 10 + "%",
                }, data: rawData[sid].map((d, did) => totalData[did] <= 0 ? 0 : d / totalData[did]),
            };
        });

        console.log('series', series);

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
            keys: keys, grid: grid, series: series, elements: elements
        };
    }

    get_requestParamsPoint(dataIndex, params) {
        c.logs('stage-timeline clicked params = ', params);
        c.logs('this.keys = ', this.keys);
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

export default StageTimelineModern;