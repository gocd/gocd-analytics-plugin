import * as echarts from "echarts";
import { updateChartSize } from "../utils";

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
class Priority {
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
    option = option = {
      title: {
        text: "Priority",
      },
      tooltip: {
        trigger: "item",
        formatter: "{a} <br/>{b} : {c}%",
      },
      toolbox: {
        feature: {
          dataView: { readOnly: false },
          restore: {},
          saveAsImage: {},
        },
      },
      legend: {
        data: info.legends,
      },
      series: [
        {
          name: "Funnel",
          type: "funnel",
          left: "10%",
          top: 60,
          bottom: 60,
          width: "80%",
          min: 0,
          max: 100,
          minSize: "0%",
          maxSize: "100%",
          sort: "descending",
          gap: 2,
          label: {
            show: true,
            position: "inside",
          },
          labelLine: {
            length: 10,
            lineStyle: {
              width: 1,
              type: "solid",
            },
          },
          itemStyle: {
            borderColor: "#fff",
            borderWidth: 1,
          },
          emphasis: {
            label: {
              fontSize: 20,
            },
          },
          data: info.seriesData,
        },
      ],
    };

    return option;
  }

  prepareData(data) {
    const legend = ["Pass", "Fail", "Cancelled"];
    const colorData = [];

    const pc = { name: legend[0], value: data.passcount };
    const fc = { name: legend[1], value: data.failcount };
    const cc = { name: legend[2], value: data.cancelcount };

    function getSeriesData() {
      return [pc, fc, cc];
    }

    return {
      legends: legend,
      seriesData: getSeriesData(),
    };
  }

  get_requestParamsPoint(index) {
    return null;
  }

  getNextGraphName() {
    return null;
  }
}

export default Priority;
