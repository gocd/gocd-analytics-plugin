import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";

/**
 * @class
 * @interface {ChartInterface}
 */
class LongestWaitingPipeline {

    data = null;

    draw(data) {

        this.data = data;

        const info = this.prepareData(this.data);

        const option = GET_STACKED_BAR_TEMPLATE(info.categories, info.series);
        option.title.text = 'Pipelines with the Highest Wait Time';

        // option.tooltip.formatter = this.tooltipFormatter();

        return option;
    }

    tooltipFormatter() {
        return function (params) {
            console.log(params);

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
        return {
            "name": this.data[index].name
        }
    }

    getNextGraphName() {
        return "LongestWaitingJobs";
    }

    insertBreadcrumb() {
        return false;
    }

    breadcrumbCaption() {
        return "Pipelines";
    }

    breadcrumbTooltip() {
        return "List all Pipelines";
    }

    getSeriesIndex() {
        return 0;
    }
}

export default LongestWaitingPipeline;