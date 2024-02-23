import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";

/**
 * @class
 * @interface {ChartInterface}
 */
class LongestWaitingJobsOnAgent {

    data = null;

    draw(data) {

        this.data = data;

        const info = this.prepareData(this.data.jobs);

        const option = GET_STACKED_BAR_TEMPLATE(info.categories, info.series);
        option.title.text = 'Jobs with the Highest Wait Time on an Agent';

        // option.tooltip.formatter = this.tooltipFormatter();

        return option;
    }

    prepareData(data) {
        const categories = [];
        const data1 = [];
        const data2 = [];

        data.forEach(m => {
            categories.push(m.pipeline_name + ' >> ' + m.stage_name + ' >> ' + m.job_name);

            data1.push(m.time_waiting_secs);
            data2.push(m.time_building_secs);
        });

        return {
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