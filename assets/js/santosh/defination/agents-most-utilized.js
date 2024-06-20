import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";
import {secondsToHms, truncateString} from "../utils";
import {getLabelTruncateAwareTooltipFormatterFunction} from "../TooltipHelper";

/**
 * @class
 * @interface {ChartInterface}
 */
class AgentsMostUtilized {

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
        option.title.text = 'Agents with the Highest Utilization';
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

        data.forEach(m => {
            actualCategories.push(m.agent_host_name);
            categories.push(truncateString(m.agent_host_name, this.settings.truncateOrder, 15));
            data1.push(m.idle_duration_secs);
        });

        return {
            actualCategories: actualCategories,
            categories: categories,
            series: getBarSeries('Agents', data1)
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

export default AgentsMostUtilized;