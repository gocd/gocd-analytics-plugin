import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";
import {getDateFromTimestampString, secondsToHms} from "../utils";
import TooltipManager from "../TooltipManager";
import * as util from "../utils";

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

        this.data = data;
        this.c = c;

        if(this.settings.selectedPipeline !== '*** All ***') {
            return this.drawForSpecificPipeline();
        }

        // console.log('stage-reruns.js this.data = ', this.data);

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
                data: this.data.map(d => d.stage_name.concat(' / ' + d.pipeline_counter)),
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

    drawForSpecificPipeline() {

        console.log('stage-reruns.js drawForSpecificPipeline() this.data = ', this.data);

        const info = this.prepareDataForSpecificPipeline(this.data);

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
                data: this.data.map(d => d.stage_name.concat(' / ' + d.pipeline_counter)),
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
        return  (params) => {

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
            series: [getBarSeries('Wait Time', data1), getBarSeries('Build Time', data2)],
            // tooltip: getTooltip
        }
    }

    prepareDataForSpecificPipeline(data) {
        const categories = [];
        const data1 = [];
        const data2 = [];

        console.log("prepareDataForSpecificPipeline data = ", data);
        console.log(util.uniq(data.map(d => d.stage_name)));

        return;

      data.forEach(m => {
        categories.push(util.uniq(m.stage_name));
        data1.push(m.scheduled_at);
        data2.push(m.stage_counter);
      });


        const data_group = util.groupBy(data, "scheduled_at");

        const info = [[0, 0, 5], [0, 1, 1], [0, 2, 0], [0, 3, 0], [0, 4, 0], [0, 5, 0], [0, 6, 0], [0, 7, 0], [0, 8, 0], [0, 9, 0], [0, 10, 0], [0, 11, 2], [0, 12, 4], [0, 13, 1], [0, 14, 1], [0, 15, 3], [0, 16, 4], [0, 17, 6], [0, 18, 4], [0, 19, 4], [0, 20, 3], [0, 21, 3], [0, 22, 2], [0, 23, 5], [1, 0, 7], [1, 1, 0], [1, 2, 0], [1, 3, 0], [1, 4, 0], [1, 5, 0], [1, 6, 0], [1, 7, 0], [1, 8, 0], [1, 9, 0], [1, 10, 5], [1, 11, 2], [1, 12, 2], [1, 13, 6], [1, 14, 9], [1, 15, 11], [1, 16, 6], [1, 17, 7], [1, 18, 8], [1, 19, 12], [1, 20, 5], [1, 21, 5], [1, 22, 7], [1, 23, 2], [2, 0, 1], [2, 1, 1], [2, 2, 0], [2, 3, 0], [2, 4, 0], [2, 5, 0], [2, 6, 0], [2, 7, 0], [2, 8, 0], [2, 9, 0], [2, 10, 3], [2, 11, 2], [2, 12, 1], [2, 13, 9], [2, 14, 8], [2, 15, 10], [2, 16, 6], [2, 17, 5], [2, 18, 5], [2, 19, 5], [2, 20, 7], [2, 21, 4], [2, 22, 2], [2, 23, 4], [3, 0, 7], [3, 1, 3], [3, 2, 0], [3, 3, 0], [3, 4, 0], [3, 5, 0], [3, 6, 0], [3, 7, 0], [3, 8, 1], [3, 9, 0], [3, 10, 5], [3, 11, 4], [3, 12, 7], [3, 13, 14], [3, 14, 13], [3, 15, 12], [3, 16, 9], [3, 17, 5], [3, 18, 5], [3, 19, 10], [3, 20, 6], [3, 21, 4], [3, 22, 4], [3, 23, 1], [4, 0, 1], [4, 1, 3], [4, 2, 0], [4, 3, 0], [4, 4, 0], [4, 5, 1], [4, 6, 0], [4, 7, 0], [4, 8, 0], [4, 9, 2], [4, 10, 4], [4, 11, 4], [4, 12, 2], [4, 13, 4], [4, 14, 4], [4, 15, 14], [4, 16, 12], [4, 17, 1], [4, 18, 8], [4, 19, 5], [4, 20, 3], [4, 21, 7], [4, 22, 3], [4, 23, 0], [5, 0, 2], [5, 1, 1], [5, 2, 0], [5, 3, 3], [5, 4, 0], [5, 5, 0], [5, 6, 0], [5, 7, 0], [5, 8, 2], [5, 9, 0], [5, 10, 4], [5, 11, 1], [5, 12, 5], [5, 13, 10], [5, 14, 5], [5, 15, 7], [5, 16, 11], [5, 17, 6], [5, 18, 0], [5, 19, 5], [5, 20, 3], [5, 21, 4], [5, 22, 2], [5, 23, 0], [6, 0, 1], [6, 1, 0], [6, 2, 0], [6, 3, 0], [6, 4, 0], [6, 5, 0], [6, 6, 0], [6, 7, 0], [6, 8, 0], [6, 9, 0], [6, 10, 1], [6, 11, 0], [6, 12, 2], [6, 13, 1], [6, 14, 3], [6, 15, 4], [6, 16, 0], [6, 17, 0], [6, 18, 0], [6, 19, 0], [6, 20, 1], [6, 21, 2], [6, 22, 2], [6, 23, 6]]
        .map(function (item) {
            return [item[1], item[0], item[2] || '-'];
        });

        return {
            categories: categories,
            series: [getBarSeries('Wait Time', data1), getBarSeries('Build Time', data2)],
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