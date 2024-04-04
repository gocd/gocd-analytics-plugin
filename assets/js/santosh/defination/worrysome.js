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
class Worrysome {
  data = null;

  draw(data) {
    console.log("worrysome draw with data ", data);

    this.data = data;

    const info = this.prepareData(this.data);

    console.log("info is ", info);

    // const option = GET_STACKED_AREA_TEMPLATE(info.categories, info.xData, info.series);
    // option.title.text = 'Pipelines with the Highest Wait Time';

    // option.tooltip.formatter = this.tooltipFormatter();

    var option;
    option = {
      title: {
        text: "Jobs waiting vs Agents available",
      },
      legend: {
        top: "bottom",
      },
      xAxis: {
        type: "category",
        data: info.xData,
      },
      yAxis: {
        type: "value",
        axisLabel: {
          formatter: function (value) {
            return secondsToHms(value);
          },
        }
      },
      series: info.series,
    };

    return option;
  }

  prepareData(data) {
    console.log("prepareData = ", data);

    const jobs_data = data.jobs.map((a) => ({
      ...a,
      scheduled_at: new Date(a.scheduled_at).toDateString(),
    }));

    console.log("jobs_data = ", jobs_data);

    const agents_data = data.agents.map((a) => ({
      ...a,
      utilization_date: new Date(a.utilization_date).toDateString(),
    }));

    console.log("agents_data = ", agents_data);

    const legend = ["Jobs waiting", "Agents available"];
    const xData = jobs_data.map((j) => j.scheduled_at);
    const data1 = jobs_data.map((j) => j.time_waiting_secs);
    const data2 = [];

    xData.forEach((d) => {
      data = agents_data.find((a) => {
        console.log("a = ", a, " d = ", d);

        return a.utilization_date === d;
      });

      console.log("data found = ", data);

      if (data !== null) {
        data2.push(data.idle_duration_secs);
      }
    });

    function getSeries() {
      const job = getLineSeries("Jobs waiting", data1);
      job.smooth = true;
      job.tooltip = {
        valueFormatter: function (value) {
          return value + " s";
        },
      };

      const agent = getLineSeries("Agents available", data2);
      agent.smooth = true;
      agent.tooltip = {
        valueFormatter: function (value) {
          return value + " s";
        },
      };

      return [job, agent];
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

export default Worrysome;
