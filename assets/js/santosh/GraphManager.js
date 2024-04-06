// import {updateChartSize} from "./utils";
import * as echarts from "echarts";
import LongestWaitingPipeline from "./defination/longest-waiting-pipeline";
import JobsHighestWaitTime from "./defination/jobs-highest-wait-time";
import JobChartFactories from "../factories/job-chart-factories";
import JobBuildTime from "./defination/job-build-time";
import AgentsMostUtilized from "./defination/agents-most-utilized";
import LongestWaitingJobsOnAgent from "./defination/longest-waiting-jobs-on-agent";
import JobBuildTimeOnAgent from "./defination/job-build-time-on-agent";
import PipelineInstances from "./defination/pipeline-instances";
import PipelineTimeline from "./defination/pipeline-timeline";
import Breadcrumb from "./breadcrumb";
import stageTimeline from "./defination/stage-timeline";
import Priority from "./defination/priority";
import PipelineStateSummary from "./defination/pipeline-state-summary";
import Worrysome from "./defination/worrysome";
import jobTimeline from "./defination/job-timeline";
import StageTimeline from "./defination/stage-timeline";
import JobTimeline from "./defination/job-timeline";
import stageTimelineHeader from "./defination/stage-timeline/stage-timeline-header";
import Header from "./defination/stage-timeline/header";
import RequestMaster from "../RequestMaster";
import PipelinePriority from "./defination/pipeline-priority";
import StagePriority from "./defination/stage-priority";
import JobPriority from "./defination/job-priority";
import PipelinePriorityDetails from "./defination/pipeline-priority-details";
import StagePriorityDetails from "./defination/stage-priority-details";
import JobPriorityDetails from "./defination/job-priority-details";

class GraphManager {
    dataStack = [];
    type = null;
    name = null;
    clickCount = 0;
    requestCount = 0;
    base = null;
    registerHandleClick = false;
    breadcrumb = null;
    settings = null;

    constructor(type, transport, informSeriesMovement = null, footer=null, c = null) {
        let chartDom = null;

        switch (type) {
            case "series":
                chartDom = document.getElementById("chart-container");
                break;
            case "standalone":
                chartDom = document.getElementById("chart-container");
                break;
            default:
                throw new Error("Invalid graph type " + type);
        }

        this.type = type;

        this.transport = transport;

        this.informSeriesMovement = informSeriesMovement;
        this.footer = footer;
        this.c = c;

        this.chart = echarts.init(chartDom);

        this.initChart();

        this.child = null;

        this.breadcrumb = new Breadcrumb(this.restoreGraph.bind(this));

        this.c.log("#GraphManager constructor");
    }

    initChart() {
        this.updateChartSize();

        window.addEventListener("resize", () => {
            this.updateChartSize();
        });
    }

    clear() {
        this.chart.clear();
    }


    updateChartSize(chart, w, h) {
        var dynamicWidth = window.innerWidth * 0.9; // Adjust the multiplier as needed
        var dynamicHeight = window.innerHeight * 0.8; // Adjust the multiplier as needed

        this.c.log(
            "updating chart size with w, h = ",
            dynamicWidth,
            dynamicHeight
        );

        this.chart.resize({
            width: dynamicWidth,
            height: dynamicHeight,
        });
    }

    call_initSeriesWithNewSettings(settings) {
        const params = this.current_initSeriesParams;
        this.initSeries(params.name, params.data, settings);
    }

    initSeries(name, data, settings) {
        this.c.log("initSeries with name, data, settings", name, data, settings);

        this.current_initSeriesParams = {name: name, data: data, settings: settings};
        this.settings = settings;

        switch (name) {
            case "LongestWaitingPipelines":
                this.child = new LongestWaitingPipeline();
                break;
            case "LongestWaitingJobs":
                this.child = new JobsHighestWaitTime();
                break;
            case "JobBuildTime":
                this.child = new JobBuildTime();
                break;

            case "AgentsMostUtilized":
                this.child = new AgentsMostUtilized();
                break;
            case "LongestWaitingJobsOnAgent":
                this.child = new LongestWaitingJobsOnAgent();
                break;
            case "JobBuildTimeOnAgent":
                this.child = new JobBuildTimeOnAgent();
                break;

            case "stage-timeline":
                this.child = new StageTimeline(settings, this.chart.getWidth(), this.chart.getHeight(), this.footer);
                break;

            case "JobsTimeline":
                this.child = new JobTimeline(settings, this.chart.getWidth(), this.chart.getHeight());
                break;

            case "priority":
                this.child = new Priority();
                break;

            case "PipelinePriority":
                this.child = new PipelinePriority(settings);
                break;

            case "PipelinePriorityDetails":
                this.child = new PipelinePriorityDetails(settings);
                break;

            case "StagePriorityDetails":
                this.child = new StagePriorityDetails(settings);
                break;

            case "JobPriorityDetails":
                this.child = new JobPriorityDetails(settings);
                break;

            case "StagePriority":
                this.child = new StagePriority(settings);
                break;

            case "JobPriority":
                this.child = new JobPriority(settings);
                break;

            default:
                throw new Error("Invalid series graph request " + name);
        }

        this.name = name;
        this.dataStack.length = 3;

        this.draw(data, this.c);
        this.handleClick();
    }

    initStandalone(name, data) {
        switch (name) {
            case "pipeline-instances":
                this.child = new PipelineInstances();
                break;
            case "vms":
                break;
            case "pipeline-timeline":
                this.child = new PipelineTimeline(data);
                break;
            case "worrysome":
                this.child = new Worrysome(data);
                break;
            case "pipeline-state-summary":
                this.child = new PipelineStateSummary(data);
                break;
            default:
                throw new Error("Invalid standalone graph request " + name);
        }

        this.name = name;

        this.draw(data, this.c);
        this.handleClick();
    }

    draw(data) {
        const option = this.child.draw(data);
        option && this.chart.setOption(option, true);

        this.chart.hideLoading();

        // this.dataStack[this.child.getSeriesIndex()] = option;
        // this.dataStack.push({name: this.name, data: data});

        if (this.type === "series") {
            this.setDataStack(data);

            console.log("dataStack = ", this.dataStack);
            console.log("dataStack length = ", this.dataStack.length);

            // if (this.child.getSeriesIndex() === 0) {
            //     this.base = option;
            // }

            this.insertBreadcrumb();
        }
    }

    setDataStack(data) {
        this.dataStack[this.child.getSeriesIndex()] = {
            name: this.name,
            data: data,
            settings: this.current_initSeriesParams.settings
        };
    }

    hasStackData(name) {
        let index = this.dataStack.findIndex((obj) => obj.name === name);
        if (index === -1) {
            return false;
        } else {
            return true;
        }
    }

    getStackDataIndex(name) {
        let index = this.dataStack.findIndex((obj) => obj.name === name);
        return index;
    }

    getStackData(name, completeData = false) {
        this.c.log("getStackData for name ", name);
        this.c.log("stackdata available for getStack ", this.dataStack);
        // console.log('returning ', this.dataStack[this.name.toString()]);
        let index = this.dataStack.findIndex((obj) => obj.name === name);
        if (index === -1) {
            throw new Error("no data at index " + index);
        } else {
            this.c.log("index is present, so I will return", this.dataStack[index]);
        }

        return completeData ? this.dataStack[index] : this.dataStack[index].data;
    }

    setBreadcrumbOption(name) {
        if (!this.hasStackData(name)) {
            this.c.log("cannot set breadcrumb option");
            return;
        }

        this.dataStack[this.getStackDataIndex(name)] = {};
    }

    insertBreadcrumb() {
        // if (this.child.insertBreadcrumb()) {
        // breadcrumb.add(["LongestWaitingPipelines", "LongestWaitingJobs", "JobBuildTime"], this.restoreGraph.bind(this));
        // }

        this.breadcrumb.add(this.name, this.child.breadcrumbCaption());
    }

    restoreGraph(index) {
        this.c.log("restoreGraph index = ", index);
        this.c.log("checking if dataStack is reachable ", this.dataStack);
        this.c.log("printing newOption");

        // const newOption = this.dataStack[index];

        this.initSeries(index, this.getStackData(index), this.current_initSeriesParams.settings);

        // console.log('newOption = ', newOption);
        // this.chart.setOption(newOption, true);
    }

    handleClick() {
        // if (typeof this.dataStack[1] !== undefined) {
        //     console.warn('handleClick already registered. returning.');
        //     return;
        // }

        this.c.log('👆 handleClick() for ', this.child);

        this.c.log("this.registerHandleClick = ", this.registerHandleClick);
        if (this.registerHandleClick) {
            console.warn("handleClick already registered. returning");
            return;
        }

        this.c.log(
            "this.child.getNextGraphName() = ",
            this.child.getNextGraphName()
        );
        // if (this.child.getNextGraphName() === null) {
        //     console.warn("There's no more graph in this series");
        //     return;
        // }

        this.chart.on("click", (params) => {

            if(typeof this.child.nativeClickHandler === 'function') {
                this.child.nativeClickHandler(this.transport, params);
            }

            if (this.child.getNextGraphName() === null) {
                console.warn("There's no more graph in this series");
                return;
            }

            this.c.log('chart is clicked with params', params);

            this.clickCount++;
            this.c.log("clickCount = ", this.clickCount);

            this.chart.showLoading();

            this.c.log("current status name ", this.name);

            const requestParamPoint = this.child.get_requestParamsPoint(
                params.dataIndex, params
            );
            this.c.log("request param point is ", requestParamPoint);

            // const index = this.dataStack.length - 1;
            // console.log('index = ', index);

            const requestParams = JobChartFactories.get(
                this.child.getNextGraphName()
            ).params(requestParamPoint);

            this.c.log("requestParams = ", requestParams);

            this.request(requestParams);
        });

        this.registerHandleClick = true;
    }

    request(requestParams) {
        this.c.log('requestParams = ', requestParams);

        this.requestCount++;
        this.c.log("requestCount = ", this.requestCount);

        this.c.log("current graph is name ", this.name);

        const nextGraphName = this.child.getNextGraphName();

        this.c.log("next graph is name ", nextGraphName);

        this.transport
            .request("fetch-analytics", requestParams)
            .done(async (data) => {
                this.c.log("fetch-analytics ", data);
                let settings;
                if (this.informSeriesMovement !== null) {
                    settings = await this.informSeriesMovement(nextGraphName, requestParams);
                    this.c.log('settings = ', settings);
                }
                this.initSeries(nextGraphName, JSON.parse(data), settings);

            })
            .fail(console.error.toString());
    }
}

export default GraphManager;
