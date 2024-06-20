import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";
import {secondsToHms, truncateString} from "../utils";
import {getLabelTruncateAwareTooltipFormatterFunction} from "../TooltipHelper";

/**
 * @class
 * @interface {ChartInterface}
 */
class LongestWaitingJobsOnAgent {

    data = null;

    constructor(settings) {
        this.settings = settings;

        // console.log('settings = ', settings);
    }

    draw(data) {

        this.data = data;

        const info = this.prepareData(this.data.jobs);

        const option = GET_STACKED_BAR_TEMPLATE(info.categories, info.series);
        option.title.text = 'Jobs with the Highest Wait Time on an Agent';

        // option.tooltip.formatter = this.tooltipFormatter();
        // option.tooltip.valueFormatter = (value) => secondsToHms(value);

        option.tooltip.formatter = getLabelTruncateAwareTooltipFormatterFunction(info.actualCategories);

        return option;
    }

    prepareData(data) {
        const actualCategories = [];
        const categories = [];
        const data1 = [];
        const data2 = [];

        data.forEach(m => {
            actualCategories.push([m.pipeline_name, m.stage_name, m.job_name]);
            categories.push(truncateString(m.pipeline_name, this.settings.truncateOrder) + ' >> ' + truncateString(m.stage_name, this.settings.truncateOrder) + ' >> ' + truncateString(m.job_name, this.settings.truncateOrder));

            data1.push(m.time_waiting_secs);
            data2.push(m.time_building_secs);
        });

        return {
            actualCategories: actualCategories,
            categories: categories,
            series: [getBarSeries('Waiting Secs', data1), getBarSeries('Building Secs', data2)]
        }
    }

    get_requestParamsPoint(index) {
        return {
            "job_name": this.data.jobs[index].job_name,
            "stage_name": this.data.jobs[index].stage_name,
            "pipeline_name": this.data.jobs[index].pipeline_name,
            "agent_uuid": this.data.agent_uuid,
            "agent_host_name": this.data.agent_host_name,
        }
    }

    getNextGraphName() {
        return "JobBuildTimeOnAgent";
    }

    insertBreadcrumb() {
        return true;
    }

    breadcrumbCaption() {
        return this.data.agent_host_name;
    }

    getSeriesIndex() {
        return 1;
    }
}

export default LongestWaitingJobsOnAgent;