import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";
import {getDateFromTimestampString, secondsToHms} from "../utils";

/**
 * @class
 * @interface {ChartInterface}
 */
class StageReruns {

    data = null;
    c = null;

    constructor(settings) {
        this.settings = settings;

        console.log('stage-reruns.js settings', settings);
    }

    isPipelineSelected() {
        console.log('this.settings.selectedPipeline = ', this.settings.selectedPipeline);
        console.log('isPipelineSelected = ', this.settings.selectedPipeline !== '' || this.settings.selectedPipeline !== '*** All ***');
        return this.settings.selectedPipeline !== '*** All ***';
    }

    draw(data, c) {

        this.data = data;
        this.c = c;

        console.log('stage-reruns.js this.data = ', this.data);

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
                // data: this.data.map(d => d.stage_name + (this.isPipelineSelected() ? d.pipeline_counter : '')),
                data: this.data.map(d => d.stage_name.concat(this.isPipelineSelected() === true ? ' / ' + d.pipeline_counter : '')),
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

        // option.tooltip.formatter = this.tooltipFormatter();
        // option.tooltip.valueFormatter = (value) => secondsToHms(value);

        return option;
    }

    tooltipFormatter() {
        return function (params) {
            this.c.log(params);

            let result = null;

            if (params.length > 0) {
                result = params[0].name + '<br>';
            }

            params.forEach(param => {
                result += param.marker + param.seriesName + ': ' + param.data + '<br>';
            });

            result += '<hr style="border-top: dotted 1px;">Click on the bar for more info';

            return result;
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


    get_requestParamsPoint(index) {
        console.log('stage-reruns.js get_requestParamsPoint ', this.data[index]);
        const selectedStage = this.data[index];

        let ret = {
            "pipeline_name": selectedStage.pipeline_name,
            // "stage_name": selectedStage.stage_name
        };

        if (this.isPipelineSelected()) {
            ret.stage_name = selectedStage.stage_name;
            ret.pipeline_counter = selectedStage.pipeline_counter;
        }

        console.log('ret = ', ret);

        return ret;
    }

    getNextGraphName() {
        if (this.isPipelineSelected()) {
            return "StageRerunsInstances";
        } else {
            return "StageReruns";
        }
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