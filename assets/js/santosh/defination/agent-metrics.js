import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";
import {groupBy, secondsToHms, truncateString, uniqBy} from "../utils";
import {getLabelTruncateAwareTooltipFormatterFunction} from "../TooltipHelper";

/**
 * @class
 * @interface {ChartInterface}
 */
class AgentMetrics {

    data = null;
    c = null;

    constructor(settings) {
        this.settings = settings;

        console.log('agent metrics settings = ', settings);
    }

    draw(data, c) {
        this.data = data;
        this.c = c;

        const info = this.prepareData(this.data, this.settings);

        const option = GET_STACKED_BAR_TEMPLATE(info.categories, info.series);
        option.title.text = 'Agent Metrics';

        option.xAxis.axisLabel = info.axisLabel;

        option.xAxis.axisLabel = {
            formatter: function (value) {
                return `${value}x`;
            }
        }
        // option.tooltip.valueFormatter = (value) => info.tooltip_value;

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

        // option.tooltip.formatter = getLabelTruncateAwareTooltipFormatterFunction(info.actualCategories);

        return option;
    }

    getWantedAgents(groupedAgents) {

        console.log("agent metrics getWantedAgents");

        const agentArray = Object.entries(groupedAgents);

        agentArray.sort(([, arrayA], [, arrayB]) => arrayA.length - arrayB.length);

        const category = [];
        const data = [];

        const sortedAgents = Object.fromEntries(agentArray);

        for (const key in sortedAgents) {
            const agent = sortedAgents[key];

            category.push(agent[0].agent_host_name);

            data.push(agent.length);
        }

        console.log("agent metrics getWantedAgents category and data", category, data);

        // return {category: category, data: data};

        return {category: category, data: data};
    }

    getPopularAgents(groupedAgents) {

        console.log("agent metrics getPopularAgents");

        // const agentArray = Object.entries(groupedAgents);
        //
        // console.log("agent metrics agentArray = ", agentArray);
        //
        const category = [];
        const data = [];
        //
        // const sortedAgents = Object.fromEntries(agentArray);

        console.log("agent metrics groupedAgents = ", groupedAgents);

        const distinct_job_agents = [];

        for (const key in groupedAgents) {
            const agents = groupedAgents[key];

            distinct_job_agents.push(uniqBy(agents, "unique_name"));
        }

        console.log("agent metrics distinct job_agents = ", distinct_job_agents);

        distinct_job_agents.sort((arrayA, arrayB) => arrayA.length - arrayB.length);

        console.log("agent metrics distinct job agents after sort", distinct_job_agents);

        distinct_job_agents.forEach(agent => {
            category.push(agent[0].agent_host_name);
            data.push(agent.length);
        });

        console.log("agent metrics getPopularAgents category and data", category, data);

        // return {category: category, data: data};

        return {category, data};
    }

    getAwaitedAgents(groupedAgents) {

        console.log("agent metrics getAwaitedAgents");

        // const agentArray = Object.entries(groupedAgents);
        //
        // agentArray.sort(([, arrayA], [, arrayB]) => arrayA.length - arrayB.length);

        const category = [];
        const data = [];

        // const sortedAgents = Object.fromEntries(agentArray);

        for (const key in groupedAgents) {
            const agent = groupedAgents[key];

            category.push(agent[0].agent_host_name);

            data.push(agent.reduce((sum, item) => sum + item.time_waiting_secs, 0));
        }

        console.log("agent metrics getAwaitedAgents category and data", category, data);

        // return {category: category, data: data};

        return {category: category, data: data};
    }

    getBusyAgents(groupedAgents) {

        console.log("agent metrics getBusyAgents");

        // const agentArray = Object.entries(groupedAgents);
        //
        // agentArray.sort(([, arrayA], [, arrayB]) => arrayA.length - arrayB.length);

        const category = [];
        const data = [];

        // const sortedAgents = Object.fromEntries(agentArray);

        for (const key in groupedAgents) {
            const agent = groupedAgents[key];

            category.push(agent[0].agent_host_name);

            data.push(agent.reduce((sum, item) => sum + item.time_building_secs, 0));
        }

        console.log("agent metrics getBusyAgents category and data", category, data);

        // return {category: category, data: data};

        return {category: category, data: data};
    }

    prepareData(data, settings) {

        console.log("agent metrics prepareData data, settings", data, settings);

        let actualCategories = [];

        let categories = [];

        let data1 = [];

        let result = null;

        data.forEach(item => {
            item['unique_name'] = item['pipeline_name'] + '-' + item['stage_name'] + '-' + item['job_name'];
        });

        const groupedAgents = groupBy(data, 'agent_uuid');

        switch (settings.metric) {
            case 'Wanted':
                result = this.getWantedAgents(groupedAgents);
                // categories = wantedCategory;
                // data1 = wantedData;

                console.log("agent metrics result = ", result);
                break;
            case 'Popular':
                result = this.getPopularAgents(groupedAgents);
                break;
            case 'Awaited':
                result = this.getAwaitedAgents(groupedAgents);
                break;
            case 'Busy':
                result = this.getBusyAgents(groupedAgents);
                break;
        }

        categories = result.category;
        data1 = result.data;

        // data.forEach(m => {
        //     actualCategories.push(m.agent_host_name);
        //     categories.push(truncateString(m.agent_host_name, this.settings.truncateOrder, 15));
        //     data1.push(m.idle_duration_secs);
        // });

        console.log("agent metrics returning categories, data", categories, data1);

        return {
            actualCategories: actualCategories,
            categories: categories,
            series: getBarSeries('Agents', data1),
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

export default AgentMetrics;