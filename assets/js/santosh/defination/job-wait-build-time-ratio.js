import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";
import {groupBy, secondsToHms, truncateString} from "../utils";
import {getLabelTruncateAwareTooltipFormatterFunction} from "../TooltipHelper";

/**
 * @class
 * @interface {ChartInterface}
 */
class JobWaitBuildTimeRatio {

    data = null;
    c = null;

    constructor(settings) {
        this.settings = settings;

        console.log('job wait build time ratio settings = ', settings);
    }

    draw(data, c) {

        this.data = data;
        this.c = c;

        const info = this.prepareData(this.data);

        const option = GET_STACKED_BAR_TEMPLATE(info.categories, info.series);
        option.title.text = 'Job wait build time ratio';
        // option.xAxis.axisLabel = {
        //     formatter: function (value) {
        //         return `${value}x`;
        //     }
        // }
        // option.tooltip.valueFormatter = (value) => secondsToHms(value);

        // option.series.label = {
        //     show: true,
        //     position: 'top',
        //     formatter: '{b}'
        // }
        //
        // option.yAxis.axisLabel = {
        //     show: false,
        // }

        // option.tooltip.formatter = this.tooltipFormatter();

        option.tooltip.formatter = getLabelTruncateAwareTooltipFormatterFunction(info.actualCategories);

        return option;
    }

    prepareData(data) {
        const actualCategories = [];
        const categories = [];
        const data1 = [];
        const data2 = [];

        const series = ['Waiting', 'Building'];

        data.forEach(m => {
            actualCategories.push(m.job_name);
            categories.push(truncateString(m.job_name, this.settings.truncateOrder, 15));
            data1.push(-Math.abs(m.time_waiting_secs));
            data2.push(m.time_building_secs);
        });

        return {
            actualCategories: actualCategories,
            categories: categories,
            series: [getBarSeries(series[0], data1), getBarSeries(series[1], data2)],
        }
    }


    get_requestParamsPoint(index) {
        return {
            "agent_uuid": this.data[index].uuid, "agent_host_name": this.data[index].agent_host_name,
        }
    }

    getNextGraphName() {
        return "LongestWaitingJobsOnAgent";
    }

    insertBreadcrumb() {
        return false;
    }

    breadcrumbCaption() {
        return "Agents";
    }

    getSeriesIndex() {
        return 0;
    }
}

export default JobWaitBuildTimeRatio;