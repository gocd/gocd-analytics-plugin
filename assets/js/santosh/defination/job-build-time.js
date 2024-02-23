import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getAreaSeries, getBarSeries} from "../template";
import GET_STACKED_AREA_TEMPLATE from "./stacked-area";
import {timestampToWords} from "../utils";

/**
 * @class
 * @interface {ChartInterface}
 */
class JobBuildTime {

    data = null;

    draw(data) {

        console.log('JobBuildTime data = ', data);

        console.log('draw() data = ', this.data);

        this.data = data.jobs;

        console.log('draw() data = ', this.data);

        const info = this.prepareData(this.data);

        console.log('JobBuildTime info = ', info);

        const option = GET_STACKED_AREA_TEMPLATE(info.categories, info.xData, info.series);
        option.title.text = 'Job Build Time';

        // option.tooltip.formatter = this.tooltipFormatter();

        return option;
    }

    prepareData(data) {
        const legend = ['Wait Time', 'Build Time'];
        const xData = [];
        const data1 = [];
        const data2 = [];
        const colorData = [];

        console.log('prepareData data = ', data);

        data.forEach(m => {
            // xData.push(timestampToWords(m.scheduled_at));
            xData.push(m.scheduled_at);
            data1.push(m.time_waiting_secs);
            data2.push(m.time_building_secs);
            colorData.push(m.result);
        });

        function getSeries() {
            const wt = getAreaSeries("Wait Time", data1);
            const bt = getAreaSeries("Build Time", data2, colorData);
            return [wt, bt]
        }

        return {
            legends: legend,
            xData: xData,
            series: getSeries()
        }
    }


    get_requestParamsPoint(index) {
        return  {
            "job_name": this.data[index].job_name,
            "stage_name": this.data[index].stage_name,
            "pipeline_name": this.data[index].pipeline_name
        }
    }

    getNextGraphName() {
        return null;
    }

    insertBreadcrumb() {
        return true;
    }

    breadcrumbCaption() {
        return this.data[0].stage_name + ' >> ' + this.data[0].job_name;
    }

    getSeriesIndex() {
        return 2;
    }
}

export default JobBuildTime;