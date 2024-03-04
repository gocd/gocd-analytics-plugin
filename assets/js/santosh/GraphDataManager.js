import {getBarSeries, getAreaSeries, getTooltipWithFormatter} from './template'
import {secondsToHms, timestampToWords} from './utils';

class GraphDataManager {
    constructor(meta) {
        this.meta = meta;
    }

    getUtilizationPoint(index) {
        return {
            "agent_uuid": this.meta[index].uuid, "agent_host_name": this.meta[index].agent_host_name,
        }
    }

    getAgentsMostUtilized() {
        const categories = [];
        const data1 = [];

        this.meta.forEach(m => {
            categories.push(m.agent_host_name);
            data1.push(m.idle_duration_secs);
        });

        function getCategories() {
            return categories;
        }

        function getSeries() {
            return getBarSeries('Agents', data1);
        }

        return {
            categories: getCategories,
            series: getSeries
        }
    }

    getLongestWaitingJobsOnAgentPoint(index) {
        return {
            "job_name": this.meta.jobs[index].job_name,
            "stage_name": this.meta.jobs[index].stage_name,
            "pipeline_name": this.meta.jobs[index].pipeline_name,
            "agent_uuid": this.meta.agent_uuid,
            "agent_host_name": this.meta.jobs[index].agent_host_name,
        }
    }

    getLongestWaitingJobsOnAgent() {
        const categories = [];
        const data1 = [];
        const data2 = [];

        console.log("meta passed is ", this.meta);

        this.meta.jobs.forEach(m => {
            categories.push(m.pipeline_name + ' >> ' + m.stage_name + ' >> ' + m.job_name);

            data1.push(m.time_waiting_secs);
            data2.push(m.time_building_secs);
        });

        function getCategories() {
            return categories;
        }

        function getSeries() {
            // return [{
            //     name: 'Waiting Secs',
            //     type: 'bar',
            //     stack: 'total',
            //     label: {
            //         show: true
            //     },
            //     emphasis: {
            //         focus: 'series'
            //     },
            //     data: data1
            // }, {
            //     name: 'Building Secs',
            //     type: 'bar',
            //     stack: 'total',
            //     label: {
            //         show: true
            //     },
            //     emphasis: {
            //         focus: 'series'
            //     },
            //     data: data2
            // }]

            return [getBarSeries('Waiting Secs', data1), getBarSeries('Building Secs', data2)]
        }

        return {
            categories: getCategories,
            series: getSeries
        }
    }

    getJobBuildTimeOnAgent() {
        const legend = ['Wait Time', 'Build Time'];
        const xData = [];
        const data1 = [];
        const data2 = [];
        const colorData = [];

        console.log("meta passed is ", this.meta);

        this.meta.jobs.forEach(m => {
            xData.push(m.scheduled_at);
            data1.push(m.time_waiting_secs);
            data2.push(m.time_building_secs);
            colorData.push(m.result);
        });

        function getSeries() {
            const wt = getAreaSeries("Wait Time", data1);
            const bt = getAreaSeries("Build Time", data2, colorData);
            return [wt, bt]
        }

        return {
            legends: legend,
            xData: xData,
            series: getSeries()
        }
    }

    getLongestWaitingPipelines() {
        const categories = [];
        const data1 = [];
        const data2 = [];

        this.meta.forEach(m => {
            categories.push(m.name);
            data1.push(m.avg_wait_time_secs);
            data2.push(m.avg_build_time_secs);
        });

        function getCategories() {
            return categories;
        }

        function getSeries() {
            return [getBarSeries('Wait Time', data1), getBarSeries('Build Time', data2)];
        }

        function getTooltip() {
            return getTooltipWithFormatter('<b>Pipeline</b>: {b}<br><b>Wait Time</b>: {c0}s<br><b>Build Time</b>: {c1}s<hr><i>Click on bar for more info</i>');
        }

        return {
            categories: getCategories,
            series: getSeries,
            tooltip: getTooltip
        }
    }

    getJobsHighestWaitTimePoint(index) {
        return {
            "name": this.meta[index].name
        }
    }

    getJobsHighestWaitTime() {
        const categories = [];
        const data1 = [];
        const data2 = [];

        console.log("meta passed is ", this.meta);

        this.meta.jobs.forEach(m => {
            categories.push(m.stage_name + ' >> ' + m.job_name);

            data1.push(m.time_waiting_secs);
            data2.push(m.time_building_secs);
        });

        function getCategories() {
            return categories;
        }

        function getSeries() {
            return [{
                name: 'Waiting Secs',
                type: 'bar',
                stack: 'total',
                label: {
                    show: true
                },
                emphasis: {
                    focus: 'series'
                },
                data: data1
            }, {
                name: 'Building Secs',
                type: 'bar',
                stack: 'total',
                label: {
                    show: true
                },
                emphasis: {
                    focus: 'series'
                },
                data: data2
            }]
        }

        function getTooltip() {
            return getTooltipWithFormatter('<b>Pipeline</b>: {b}<br><b>Wait Time</b>: {c0}s<br><b>Build Time</b>: {c1}s<hr><i>Click on bar for more info</i>');
        }

        return {
            categories: getCategories,
            series: getSeries,
            tooltip: getTooltip
        }
    }

    getJobBuildTimePoint(index) {
        console.log("Job build time point meta: ", this.meta);
        return {
            "job_name": this.meta.jobs[index].job_name,
            "stage_name": this.meta.jobs[index].stage_name,
            "pipeline_name": this.meta.jobs[index].pipeline_name
        }
    }

    getJobBuildTime() {
        const legend = ['Wait Time', 'Build Time'];
        const xData = [];
        const data1 = [];
        const data2 = [];
        const colorData = [];

        console.log("meta passed is ", this.meta);

        this.meta.jobs.forEach(m => {
            xData.push(timestampToWords(m.scheduled_at));
            data1.push(m.time_waiting_secs);
            data2.push(m.time_building_secs);
            colorData.push(m.result);
        });

        function getSeries() {
            const wt = getAreaSeries("Wait Time", data1);
            const bt = getAreaSeries("Build Time", data2, colorData);
            return [wt, bt]
        }

        return {
            legends: legend,
            xData: xData,
            series: getSeries()
        }
    }
}


export default GraphDataManager;