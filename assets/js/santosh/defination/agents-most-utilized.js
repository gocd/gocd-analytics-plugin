import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";

/**
 * @class
 * @interface {ChartInterface}
 */
class AgentsMostUtilized {

    data = null;

    draw(data) {

        this.data = data;

        const info = this.prepareData(this.data);

        const option = GET_STACKED_BAR_TEMPLATE(info.categories, info.series);
        option.title.text = 'Agents with the Highest Utilization';

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

        return option;
    }

    prepareData(data) {
        const categories = [];
        const data1 = [];

        data.forEach(m => {
            categories.push(m.agent_host_name);
            data1.push(m.idle_duration_secs);
        });

        return {
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