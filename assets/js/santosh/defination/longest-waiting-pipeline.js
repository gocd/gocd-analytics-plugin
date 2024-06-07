import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";
import {getLabelTruncateAwareTooltipFormatterFunction} from "../TooltipHelper";

/**
 * @class
 * @interface {ChartInterface}
 */
class LongestWaitingPipeline {

    data = null;
    c = null;

    constructor(settings) {
        this.settings = settings;

        console.log('settings = ', settings);
    }

    draw(data, c) {

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

        function truncateString(text, truncateOrder, maxLength = 10) {
            if (truncateOrder === 'None' || text.length <= maxLength) {
                return text;
            } else if (truncateOrder === 'Last') {
                return text.substring(0, maxLength - 3) + "...";
            } else if (truncateOrder === 'Middle') {
                let numFirstChars = 2;
                let numLastChars = 2;

                if (text.length <= (numFirstChars + numLastChars + 3)) {
                    return text; // String is already short enough
                }
                const middleLength = text.length - numFirstChars - numLastChars;
                if (middleLength <= 3) {
                    // Not enough space for ellipsis, shorten last characters
                    numLastChars = Math.max(0, middleLength);
                }
                return `${text.slice(0, numFirstChars)}...${text.slice(-numLastChars)}`;
            }

        }

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