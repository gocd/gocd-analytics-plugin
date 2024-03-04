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
    option = {
      title: {
        text: "Pipeline overall",
      },
      legend: {
        top: "bottom",
      },
      toolbox: {
        show: true,
        feature: {
          mark: { show: true },
          dataView: { show: true, readOnly: false },
          restore: { show: true },
          saveAsImage: { show: true },
        },
      },
      series: [
        {
          name: "Priorities",
          type: "pie",
          radius: [45, 150],
          center: ["50%", "50%"],
          roseType: "area",
          itemStyle: {
            borderRadius: 8,
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
