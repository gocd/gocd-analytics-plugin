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
class PipelineTimeline {
  data = null;

  draw(data) {
    console.log("draw with data ", data);

    this.data = data;

    const info = this.prepareData(this.data);

    console.log("info is ", info);

    // const option = GET_STACKED_AREA_TEMPLATE(info.categories, info.xData, info.series);
    // option.title.text = 'Pipelines with the Highest Wait Time';

    // option.tooltip.formatter = this.tooltipFormatter();

    var option;
    option = {
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
          dataView: { show: true, readOnly: false },
          magicType: { show: true, type: ["line", "bar"] },
          restore: { show: true },
          saveAsImage: { show: true },
        },
      },
      legend: {
        data: info.legends,
      },
      xAxis: [
        {
          type: "category",
          data: info.xData,
          axisPointer: {
            type: "shadow",
          },
        },
      ],
      yAxis: [
        {
          // type: "value",
          // name: "Precipitation",
          // min: 0,
          // max: 250,
          // interval: 50,
          // axisLabel: {
          //   formatter: "{value} s",
          // },
        },
        // {
        //   type: 'value',
        //   name: 'Temperature',
        //   min: 0,
        //   max: 25,
        //   interval: 5,
        //   axisLabel: {
        //     formatter: '{value} °C'
        //   }
        // }
      ],

      series: info.series,

      // series: [
      //   {
      //     name: "Waiting Time",
      //     type: "bar",
      //     tooltip: {
      //       valueFormatter: function (value) {
      //         return value + " s";
      //       },
      //     },
      //     data: [
      //       // 2.0, 4.9, 7.0, 23.2, 25.6, 76.7, 135.6, 162.2, 32.6, 20.0, 6.4, 3.3
      //       1,
      //       20, 100, 4,
      //     ],
      //   },
      //   {
      //     name: "Building Time",
      //     type: "bar",
      //     tooltip: {
      //       valueFormatter: function (value) {
      //         return value + " s";
      //       },
      //     },
      //     data: [
      //       // 2.6, 5.9, 9.0, 26.4, 28.7, 70.7, 175.6, 182.2, 48.7, 18.8, 6.0, 2.3
      //       1,
      //       20, 100, 4,
      //     ],
      //   },
      //   {
      //     name: "",
      //     type: "line",
      //     // yAxisIndex: 0,
      //     // tooltip: {
      //     //   valueFormatter: function (value) {
      //     // return value + ' °C';
      //     // },
      //     // },
      //     data: [10, 30, 110, 10],
      //   },
      // ],
    };

    return option;
  }

  prepareData(data) {
    const legend = ["Waiting Time", "Building Time", "Total Time"];
    const xData = [];
    const data1 = [];
    const data2 = [];
    const data3 = [];
    const colorData = [];

    data.forEach((m) => {
      // xData.push(timestampToWords(m.scheduled_at));
      xData.push(m.counter);
      data1.push(m.time_waiting_secs);
      data2.push(m.total_time_secs - m.time_waiting_secs);
      data3.push(m.total_time_secs);
      colorData.push(m.result);
    });

    function getSeries() {
      const wt = getPlainBarSeries("Waiting Time", data1);
      wt.tooltip = {
        valueFormatter: function (value) {
          return secondsToHms(value);
        },
      };

      const bt = getPlainBarSeries("Building Time", data2, colorData);
      bt.tooltip = {
        valueFormatter: function (value) {
          return secondsToHms(value);
        },
      };

      const monitor = getLineSeries("Total Time", data3);
      monitor.tooltip = {
        valueFormatter: function (value) {
          return secondsToHms(value);
        }
      }

      return [wt, bt, monitor];
    }

    return {
      legends: legend,
      xData: xData,
      series: getSeries(),
    };
  }

  get_requestParamsPoint(index) {
    return null;
  }

  getNextGraphName() {
    return null;
  }
}

export default PipelineTimeline;
