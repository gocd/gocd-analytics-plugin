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

class GraphManager {
  dataStack = [];
  type = null;
  name = null;
  clickCount = 0;
  requestCount = 0;
  base = null;
  registerHandleClick = false;
  breadcrumb = null;

  constructor(type, transport) {
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

    this.chart = echarts.init(chartDom);

    this.initChart();

    this.child = null;

    this.breadcrumb = new Breadcrumb(this.restoreGraph.bind(this));

    console.log("#GraphManager constructor");
  }

  initChart() {
    this.updateChartSize();

    window.addEventListener("resize", () => {
      this.updateChartSize();
    });
  }

  updateChartSize(chart, w, h) {
    var dynamicWidth = window.innerWidth * 0.9; // Adjust the multiplier as needed
    var dynamicHeight = window.innerHeight * 0.8; // Adjust the multiplier as needed

    console.log(
      "updating chart size with w, h = ",
      dynamicWidth,
      dynamicHeight
    );

    this.chart.resize({
      width: dynamicWidth,
      height: dynamicHeight,
    });
  }

  initSeries(name, data) {
    console.log("initSeries with name, data", name, data);

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

      default:
        throw new Error("Invalid series graph request " + name);
    }

    this.name = name;
    this.dataStack.length = 3;

    this.draw(data);
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
      case "stage-timeline":
        this.child = new stageTimeline();
        break;
      case "worrysome":
        this.child = new Worrysome(data);
        break;
      case "priority":
        this.child = new Priority();
        break;
      case "pipeline-state-summary":
        this.child = new PipelineStateSummary(data);
        break;
      default:
        throw new Error("Invalid standalone graph request " + name);
    }

    this.name = name;

    this.draw(data);
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
    console.log("getStackData for name ", name);
    console.log("stackdata available for getStack ", this.dataStack);
    // console.log('returning ', this.dataStack[this.name.toString()]);
    let index = this.dataStack.findIndex((obj) => obj.name === name);
    if (index === -1) {
      throw new Error("no data at index " + index);
    } else {
      console.log("index is present, so I will return", this.dataStack[index]);
    }

    return completeData ? this.dataStack[index] : this.dataStack[index].data;
  }

  setBreadcrumbOption(name) {
    if (!this.hasStackData(name)) {
      console.log("cannot set breadcrumb option");
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
    console.log("restoreGraph index = ", index);
    console.log("checking if dataStack is reachable ", this.dataStack);
    console.log("printing newOption");

    // const newOption = this.dataStack[index];

    this.initSeries(index, this.getStackData(index));

    // console.log('newOption = ', newOption);
    // this.chart.setOption(newOption, true);
  }

  handleClick() {
    // if (typeof this.dataStack[1] !== undefined) {
    //     console.warn('handleClick already registered. returning.');
    //     return;
    // }

    console.log("this.registerHandleClick = ", this.registerHandleClick);
    if (this.registerHandleClick) {
      console.warn("handleClick already registered. returning");
      return;
    }

    console.log(
      "this.child.getNextGraphName() = ",
      this.child.getNextGraphName()
    );
    if (this.child.getNextGraphName() === null) {
      console.warn("There's no more graph in this series");
      return;
    }

    this.chart.on("click", (params) => {
      this.clickCount++;
      console.log("clickCount = ", this.clickCount);

      this.chart.showLoading();

      console.log("current status name ", this.name);

      const requestParamPoint = this.child.get_requestParamsPoint(
        params.dataIndex
      );
      console.log("request param point is ", requestParamPoint);

      // const index = this.dataStack.length - 1;
      // console.log('index = ', index);

      const requestParams = JobChartFactories.get(
        this.child.getNextGraphName()
      ).params(requestParamPoint);

      console.log("requestParams = ", requestParams);

      this.request(requestParams);
    });

    this.registerHandleClick = true;
  }

  request(requestParams) {
    this.requestCount++;
    console.log("requestCount = ", this.requestCount);

    console.log("current graph is name ", this.name);
    console.log("next graph is name ", this.child.getNextGraphName());

    this.transport
      .request("fetch-analytics", requestParams)
      .done((data) => {
        console.log("fetch-analytics ", data);
        this.initSeries(this.child.getNextGraphName(), JSON.parse(data));
      })
      .fail(console.error.toString());
  }
}

export default GraphManager;
