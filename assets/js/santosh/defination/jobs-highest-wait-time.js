import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";
import {secondsToHms} from "../utils";


class JobsHighestWaitTime {

    data = null;

    draw(data) {

        this.data = data.jobs;

        const info = this.prepareData(this.data);

        const option = GET_STACKED_BAR_TEMPLATE(info.categories, info.series);
        option.title.text = 'Jobs with the Highest Wait Time';
        option.tooltip.valueFormatter = (value) => secondsToHms(value);

        // option.yAxis.axisLabel = {
        //     inside: true,
        //     verticalAlign: 'top',
        //     lineHeight: -230,
        // };
        // option.series[0].label = {
        //     show: true,
        //     position: 'top',
        //     formatter: '{b}'
        // }

        // option.tooltip.formatter = this.tooltipFormatter();

        return option;
    }

    prepareData(data) {
        const categories = [];
        const data1 = [];
        const data2 = [];

        data.forEach(m => {
            categories.push(m.stage_name + ' >> ' + m.job_name);

            data1.push(m.time_waiting_secs);
            data2.push(m.time_building_secs);
        });

        return {
            categories: categories,
            series: [getBarSeries('Waiting Secs', data1), getBarSeries('Building Secs', data2)],
        }
    }

    get_requestParamsPoint(index) {
        return {
            "job_name": this.data[index].job_name,
            "stage_name": this.data[index].stage_name,
            "pipeline_name": this.data[index].pipeline_name
        }
    }

    getNextGraphName() {
        return "JobBuildTime";
    }

    insertBreadcrumb() {
        return true;
    }

    breadcrumbCaption() {
        return this.data[0].pipeline_name;
    }

    breadcrumbTooltip() {
        return "Pipeline: " + this.breadcrumbCaption();
    }

    getSeriesIndex() {
        return 1;
    }
}

export default JobsHighestWaitTime;