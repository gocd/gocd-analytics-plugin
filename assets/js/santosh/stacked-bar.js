import {secondsToHms, updateChartSize} from "./utils";
import JobChartFactories from "../factories/job-chart-factories";
import GraphDataManager from "./GraphDataManager";
import * as echarts from "echarts";
import StackedArea from "./stacked-area";
import {DomManager} from "./DomManager"

console.log('santosh/stacked-bar.js loaded');

class StackedBar {
    #chart
    #instances
    #dom

    constructor(name, meta, drilldowns, transport) {

        console.log('StackedBar called');

        this.name = name;
        this.meta = meta;
        this.drilldowns = drilldowns;
        this.transport = transport;

        this.level = 0;

        this.categories = [];
        this.series = [];

        this.dataStore = [];

        // this.point = {};

        this.#chart = this.generate();
        console.log('this.#chart = ', this.#chart);
        // this.#instances[this.level] = this.#chart;


        this.handleClick();

        this.#dom = new DomManager(this.name);
        // this.#dom.addNavigationHeader();

        this.tooltip = undefined;
    }

    constructGraphData() {

        console.log("constructGraphData()");

        switch (this.name) {
            case 'LongestWaitingPipelines':
                this.constructLongestWaitingPipelinesGraphData();
                break;
            case 'AgentsMostUtilized':
                this.constructAgentsMostUtilizedGraphData();
                break;
            default:
                throw new Error("Unknown base name");
        }

    }

    constructLongestWaitingPipelinesGraphData() {
        let dm = new GraphDataManager(this.meta);
        let title;

        console.log('debug constructLongestWaitingPipelinesGraphData()');
        console.log('debug switching this.level ', this.level);

        switch (this.level) {
            case 0:
                this.title = {text: 'Pipelines with the Highest Wait Time', subtext: 'Average over the last 7 days'};
                dm = dm.getLongestWaitingPipelines();
                console.log('dm = ', dm);
                break;
            case 1:
                this.title = {
                    text: 'Jobs with the Highest Wait Time',
                    subtext: 'Average over the last 7 days'
                };
                dm = dm.getJobsHighestWaitTime();
                break;
            case 2:
                title = {text: 'Job Build Time', subtext: 'Pipeline: >> ' + this.meta.pipeline_name + ' Stage: >> Job:'}
                let res = dm.getJobBuildTime();
                StackedArea.generate(title, res.legends, res.xData, res.series);
                console.log('StackArea generate complete');
                break;
            default:
                throw Error("constructGraphData() Unknown level: " + this.level);
        }

        if (this.level !== 2) {
            console.log("level is not 2 so calling categories");
            this.categories = dm.categories();
            this.series = dm.series();
            this.tooltip = dm.tooltip();
        }

        console.log('#debug dm = ', dm);

        this.addDataStore();
    }

    constructAgentsMostUtilizedGraphData() {
        let dm = new GraphDataManager(this.meta);
        let title;

        switch (this.level) {
            case 0:
                this.title = {
                    text: 'Agents with the Highest Utilization',
                    subtext: '<a href="">Average over the last 7 days.</a>'
                };
                dm = dm.getAgentsMostUtilized();
                break;
            case 1:
                this.title = {
                    text: 'Jobs with the Highest Wait Time on an Agent',
                    subtext: 'Average over the last 7 days'
                };
                dm = dm.getLongestWaitingJobsOnAgent();
                break;
            case 2:
                title = {
                    text: 'Job Build Time on an Agent',
                    subtext: 'Agent: ' + this.meta.agent_host_name + ' (' + this.meta.agent_uuid + ')'
                }
                let res = dm.getJobBuildTimeOnAgent();
                console.log('res =', res);
                StackedArea.generate(title, res.legends, res.xData, res.series);
                console.log('StackArea generate complete');
                break;
            default:
                throw Error("constructGraphData() Unknown level: " + this.level);
        }

        if (this.level !== 2) {
            console.log("level is not 2 so calling categories");
            this.categories = dm.categories();
            this.series = dm.series();
        }

        this.addDataStore();
    }


    prepareRequestParams(index) {
        console.log("prepareParams() with this.level = ", this.level);
        let point;

        switch (this.name) {

            case 'LongestWaitingPipelines':

                switch (this.level + 1) {
                    case 1:
                        point = new GraphDataManager(this.meta).getJobsHighestWaitTimePoint(index);
                        console.log("point = ", point);
                        // change
                        // return JobChartFactories.get(this.drilldowns[this.level]).params(point);
                        const params = JobChartFactories.get(this.drilldowns[this.level]).params(point);
                        console.log('request params = ', params);
                        break;
                    case 2:
                        console.log("creating point for JobBuildTime");
                        point = new GraphDataManager(this.meta).getJobBuildTimePoint(index);
                        console.log("point = ", point);
                        return JobChartFactories.get(this.drilldowns[this.level]).params(point);
                        break;
                    default:
                        throw Error("prepareRequestParams() Unknown level: " + this.level);
                }
                break;

            case 'AgentsMostUtilized':
                switch (this.level + 1) {
                    case 1:
                        console.log("prepareParams 1");
                        console.log("meta = ", this.meta);
                        point = new GraphDataManager(this.meta).getUtilizationPoint(index);
                        console.log("point = ", point);
                        return JobChartFactories.get(this.drilldowns[this.level]).params(point);
                        break;
                    case 2:
                        point = new GraphDataManager(this.meta).getLongestWaitingJobsOnAgentPoint(index);
                        console.log("point = ", point);
                        return JobChartFactories.get(this.drilldowns[this.level]).params(point);
                        break;
                    default:
                        throw Error("prepareRequestParams() Unknown level: " + this.level);
                }
                break;

            default:
                throw new Error("Unknown name");
        }
    }

    handleClick() {

        console.log('handleClick()');

        this.#chart.on('click', (params) => {
            console.log('chart clicked with params ', params);

            if (this.level === 2) {
                console.log("can't click more");
                return;
            }

            this.#chart.showLoading();

            const requestParams = this.prepareRequestParams(params.dataIndex);
            this.level += 1;

            this.request(requestParams);
        });

    }

    restoreGraphDataToPreviousState() {
        const prevLevel = this.level - 1;

        this.title = this.dataStore[prevLevel].title;
        this.categories = this.dataStore[prevLevel].categories;
        this.series = this.dataStore[prevLevel].series;
    }

    request(requestParams) {
        this.transport.request("fetch-analytics", requestParams)
            .done((data) => {

                data = JSON.parse(data);
                console.log("data parsed ", data);

                this.meta = data;

                var i = this.generate();
                // this.#instances[this.level] = i;

                this.#dom.addLink('Second link');
            })
            .fail(console.error);
    }

    addDataStore() {
        this.dataStore[this.level] =
            {
                title: this.title,
                categories: this.categories,
                series: structuredClone(this.series),
            };
    }

    generate(alreadyConstructed) {
        console.log("() generate");

        if (alreadyConstructed === undefined) {
            console.log("not constructed. constructing...")
            this.constructGraphData();
        }

        if (this.level === 2) {
            console.log("already level 2, returning");
            return;
        }

        console.log('generating graph with columns, series = ', this.categories, this.series);

        const chartDom = document.getElementById('chart-container');

        // const myChart = echarts.init(chartDom, {
            // width: '600px',
            // height: '400px',
        // });


        const myChart = echarts.init(chartDom);

        console.log('myChart = ', myChart);

        updateChartSize(myChart);

        window.addEventListener('resize', function() {
            updateChartSize(myChart);
        });


        var option;

        option = {
            title: {
                text: this.title.text,
                subtext: this.title.subtext,
            },
            // tooltip: {
            //     trigger: 'axis',
            //     axisPointer: {
            //         // Use axis to trigger tooltip
            //         type: 'shadow' // 'shadow' as default; can also be 'line' or 'shadow'
            //     },
            //     formatter: this.tooltip,
            // },
            tooltip: this.tooltip,
            legend: {
                // top: 20,
                // right: 30
                bottom: 1,
            },
            toolbox: {
                feature: {
                    saveAsImage: {},
                    dataZoom: {
                        // yAxisIndex: 'none',
                    },
                    dataView: {
                        readOnly: false,
                    }
                }
            },
            grid: {
                // left: '3%',
                // right: '4%',
                // bottom: '3%',
                containLabel: true
            },
            xAxis: {
                type: 'value',
                axisLabel: {
                    formatter: function (value) {
                        return secondsToHms(value);
                    },
                }
            },
            yAxis: {
                type: 'category',
                data: this.categories
            },
            series: this.series
        };

        option && myChart.setOption(option, true);

        console.log("returning myChart");

        myChart.hideLoading();

        return myChart;
    }
};

export default StackedBar;