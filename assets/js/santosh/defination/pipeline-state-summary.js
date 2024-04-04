import * as echarts from "echarts";
import {secondsToHms, updateChartSize} from "../utils";

// import { getAreaSeries, getBarSeries } from "../template";
import GET_STACKED_AREA_TEMPLATE from "./stacked-area";
import {
  getAreaSeries,
  getBarSeries,
  getLineSeries,
  getPlainBarSeries,
} from "../template";

/**
 * @class
 * @interface {ChartInterface}
 */
class PipelineStateSummary {
  data = null;

  draw(data) {
    console.log("draw with data ", data);

    this.data = data;

    const info = this.prepareData(this.data);

    console.log("info is ", info);

    // const option = GET_STACKED_AREA_TEMPLATE(info.categories, info.xData, info.series);
    // option.title.text = 'Pipelines with the Highest Wait Time';

    // option.tooltip.formatter = this.tooltipFormatter();

    const dataAll = info;
    const markLineOpt = {
      animation: false,
      label: {
        formatter: "y = 0.5 * x + 3",
        align: "right",
      },
      lineStyle: {
        type: "solid",
      },
      tooltip: {
        formatter: "y = 0.5 * x + 3",
      },
      data: [
        [
          {
            coord: [0, 3],
            symbol: "none",
          },
          {
            coord: [20, 13],
            symbol: "none",
          },
        ],
      ],
    };

    var option;
    option = {
      title: {
        text: "Pipeline Summary",
        left: "center",
        top: 0,
      },
      grid: [
        { left: "7%", top: "7%", width: "38%", height: "38%" },
        { right: "7%", top: "7%", width: "38%", height: "38%" },
        { left: "7%", bottom: "7%", width: "38%", height: "38%" },
        { right: "7%", bottom: "7%", width: "38%", height: "38%" },
      ],
      tooltip: {
        formatter: "Group {a}: ({c})",
      },
      xAxis: [
        {
          gridIndex: 0,
          // min: 0, max: 20
        },
        {
          gridIndex: 1,
          //  min: 0, max: 20
        },
        {
          gridIndex: 2,
          // min: 0, max: 20
        },
        {
          gridIndex: 3,
          // min: 0, max: 20
        },
      ],
      yAxis: [
        {
          gridIndex: 0,
          // min: 0, max: 15
          axisLabel: {
            formatter: function (value) {
              return secondsToHms(value);
            },
          }
        },
        {
          gridIndex: 1,
          // min: 0, max: 15
          axisLabel: {
            formatter: function (value) {
              return secondsToHms(value);
            },
          }
        },
        {
          gridIndex: 2,
          //  min: 0, max: 15
          axisLabel: {
            formatter: function (value) {
              return secondsToHms(value);
            },
          }
        },
        {
          gridIndex: 3,
          // min: 0, max: 15
          axisLabel: {
            formatter: function (value) {
              return secondsToHms(value);
            },
          }
        },
      ],
      series: [
        {
          name: "I",
          type: "scatter",
          xAxisIndex: 0,
          yAxisIndex: 0,
          data: dataAll[0],
          markLine: markLineOpt,
        },
        {
          name: "II",
          type: "scatter",
          xAxisIndex: 1,
          yAxisIndex: 1,
          data: dataAll[1],
          markLine: markLineOpt,
        },
        {
          name: "III",
          type: "scatter",
          xAxisIndex: 2,
          yAxisIndex: 2,
          data: dataAll[2],
          markLine: markLineOpt,
        },
        {
          name: "IV",
          type: "scatter",
          xAxisIndex: 3,
          yAxisIndex: 3,
          data: dataAll[3],
          markLine: markLineOpt,
        },
      ],
    };

    return option;
  }

  prepareData(data) {
    const wait = [];
    const build_pass = [];
    const build_fail = [];
    const build_cancel = [];

    data.forEach((m) => {
      wait.push([m.id, m.time_waiting_secs]);
      const build_time = m.total_time_secs - m.time_waiting_secs;

      switch (m.result) {
        case "Passed":
          build_pass.push([m.id, build_time]);
          break;
        case "Failed":
          build_fail.push([m.id, build_time]);
          break;
        case "Cancelled":
          build_cancel.push([m.id, build_time]);
          break;
        default:
          throw new Error("New unknown result " + m.result);
      }
    });

    return [wait, build_pass, build_cancel, build_fail];
  }

  get_requestParamsPoint(index) {
    return null;
  }

  getNextGraphName() {
    return null;
  }
}

export default PipelineStateSummary;
