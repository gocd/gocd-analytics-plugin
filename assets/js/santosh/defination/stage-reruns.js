import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";
import {getDateFromTimestampString, groupBy, secondsToHms} from "../utils";
import TooltipManager from "../TooltipManager";

/**
 * @class
 * @interface {ChartInterface}
 */
class StageReruns {

  data = null;
  c = null;

  constructor(settings) {
    this.settings = settings;

    // console.log('stage-reruns.js settings', settings);
  }

  isPipelineSelected() {
    // console.log('this.settings.selectedPipeline = ', this.settings.selectedPipeline);
    // console.log('isPipelineSelected = ', this.settings.selectedPipeline !== '' || this.settings.selectedPipeline !== '*** All ***');
    return this.settings.selectedPipeline !== '*** All ***';
  }

  draw(data, c) {

    console.log("stage-reruns.js draw()");

    this.data = data;
    this.c = c;

    if (this.settings.selectedPipeline !== '*** All ***') {
      return this.drawForSelectedPipeline(data, c)
    } else {
      return this.drawForAllPipelines(data, c);
    }

    // console.log('stage-reruns.js this.data = ', this.data);

  }

  drawForSelectedPipeline(draw, c) {
    console.log("stage-reruns.js drawForSelectedPipeline()");

    const info = this.prepareDataForSelectedPipeline(this.data);

    console.log("stage-reruns.js data preparation complete", info);

    const option = {
      tooltip: {
        position: 'top'
      },
      grid: {
        height: '50%',
        top: '10%'
      },
      xAxis: {
        type: 'category',
        data: info.arr_scheduled_at,
        splitArea: {
          show: true
        }
      },
      yAxis: {
        type: 'category',
        data: info.arr_stage_name,
        splitArea: {
          show: true
        }
      },
      visualMap: {
        min: info.min,
        max: info.max,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: '15%'
      },
      series: [
        {
          name: this.settings.selectedPipeline,
          type: 'heatmap',
          data: info.points,
          label: {
            show: true
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    };

    return option;
  }

  prepareDataForSelectedPipeline(data) {

    data.forEach(d => {
      d.scheduled_at = getDateFromTimestampString(d.scheduled_at);
    });

    const arr_scheduled_at = data.map(d => d.scheduled_at);

    // console.log("stage-reruns.js dateFormattedData complete", data);

    const groupedData = groupBy(data, "stage_name");

    const arr_stage_name = Object.keys(groupedData);

    // console.log("stage-reruns.js groupedData complete", groupedData);

    const points = [];

    let min = 0, max = 0;

    for (let i = 0; i < Object.keys(groupedData).length; i++) {
      const keys = Object.keys(groupedData);
      const key = keys[i];

      const value = groupedData[key];

      console.log("key, value ", key, value);

      for (let j = 0; j < value.length; j++) {

        const stage_counter = value[j].stage_counter;

        if (stage_counter < min) {
          min = stage_counter;
        }

        if (stage_counter > max) {
          max = stage_counter;
        }

        points.push([i, j, stage_counter || '-']);
      }

    }


    // .map(function (item) {
    //   return [item[1], item[0], item[2] || '-'];
    // });

    return {
      arr_scheduled_at: arr_scheduled_at,
      arr_stage_name: arr_stage_name,
      points: points,
      min: min,
      max: max
    };
  }

  drawForAllPipelines(draw, c) {
    const info = this.prepareData(this.data);

    const option = {
      title: {
        text: 'Stage reruns for ' + this.settings.selectedPipeline,
      },
      toolbox: {
        feature: {
          dataView: {
            readOnly: true,
          }
        }
      },
      tooltip: {},
      xAxis: {
        type: 'category',
        data: this.data.map(
            d => d.stage_name.concat(' / ' + d.pipeline_counter)),
      },
      yAxis: {
        name: 'Times',
        type: 'value'
      },
      series: [
        {
          data: this.data.map(d => d.stage_counter),
          type: 'bar',
          orientation: 'vertical',
          showBackground: true,
          backgroundStyle: {
            color: 'rgba(180, 180, 180, 0.2)'
          }
        }
      ]
    };

    option.tooltip.formatter = this.tooltipFormatter();
    // option.tooltip.valueFormatter = (value) => secondsToHms(value);

    return option;
  }

  tooltipFormatter() {
    return (params) => {

      console.log("params = ", params);

      const info = this.data[params.dataIndex];

      const tooltip = new TooltipManager();
      tooltip.addTitle(info.stage_name);

      tooltip.addItem(params.marker, "Pipeline name", info.pipeline_name);
      tooltip.addItem(params.marker, "Pipeline counter", info.pipeline_counter);

      tooltip.addFooter("Click to see specific details");

      return tooltip.getStandard();
    };
  }

  prepareData(data) {
    const categories = [];
    const data1 = [];
    const data2 = [];

    data.forEach(m => {
      categories.push(m.name);
      data1.push(m.avg_wait_time_secs);
      data2.push(m.avg_build_time_secs);
    });

    return {
      categories: categories,
      series: [getBarSeries('Wait Time', data1),
        getBarSeries('Build Time', data2)],
      // tooltip: getTooltip
    }
  }

  get_requestParamsPoint(index) {
    // console.log('stage-reruns.js get_requestParamsPoint ', this.data[index]);
    const selectedStage = this.data[index];

    console.log("selectedStage", selectedStage);

    let ret = {
      "pipeline_name": selectedStage.pipeline_name,
      // "stage_name": selectedStage.stage_name
    };

    // if (this.isPipelineSelected()) {
    ret.stage_name = selectedStage.stage_name;
    ret.pipeline_counter = selectedStage.pipeline_counter;
    // }

    // console.log('ret = ', ret);

    return ret;
  }

  getNextGraphName() {
    // if (this.isPipelineSelected()) {
    return "StageRerunsInstances";
    // } else {
    //     return "StageReruns";
    // }
  }

  insertBreadcrumb() {
    return false;
  }

  breadcrumbCaption() {
    return "Stage reruns";
  }

  breadcrumbTooltip() {
    return "List all Pipelines";
  }

  getSeriesIndex() {
    return 0;
  }

  nativeClickHandler2remove(transport, params) {
    console.log('nativeClickHandler params', params);

    // const selectedDate = params.name.toString();
    // console.log("Searching for data on ", selectedDate);
    //
    // const filteredData =
    //     this.data.filter(item => selectedDate === getDateFromTimestampString(item.scheduled_at));
    //
    // console.log(filteredData);
    //
    // return filteredData;
  }
}

export default StageReruns;