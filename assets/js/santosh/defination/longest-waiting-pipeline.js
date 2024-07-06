import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";
import {getLabelTruncateAwareTooltipFormatterFunction} from "../TooltipHelper";
import {truncateString} from "../utils";

/**
 * @class
 * @interface {ChartInterface}
 */
class LongestWaitingPipeline {

    data = null;
    c = null;

    constructor(settings) {

        console.log("longestWaitingPipeline");

        console.log("settings = ", settings);

        this.settings = settings;

        // console.log('settings = ', settings);
    }

    draw(data, c) {

        console.log("data ", data);

        this.data = data;
        this.c = c;

        const info = this.prepareData(this.data);

        const option = GET_STACKED_BAR_TEMPLATE(info.categories, info.series);
        option.title.text = 'Pipelines with the Highest Wait Time';

        option.tooltip.formatter = getLabelTruncateAwareTooltipFormatterFunction(info.actualCategories);

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
        const actualCategories = [];
        const categories = [];
        const data1 = [];
        const data2 = [];

        data.forEach(m => {
            actualCategories.push(m.name);
            categories.push(truncateString(m.name, this.settings.truncateOrder));
            data1.push(m.avg_wait_time_secs);
            data2.push(m.avg_build_time_secs);
        });

        return {
            actualCategories: actualCategories,
            categories: categories,
            series: [getBarSeries('Wait Time', data1), getBarSeries('Build Time', data2)],
            // tooltip: getTooltip
        }
    }


    get_requestParamsPoint(index) {
        return {
            "name": this.data[index].name,
            "startDate": this.settings.startDate,
            "endDate": this.settings.endDate,
            "limit": this.settings.limit
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