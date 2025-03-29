import Console from "./santosh/Console";

const c = new Console('RequestMaster.js', 'prod');

class RequestMaster {
    constructor(transport) {
        this.transport = transport;
        c.log('1. RequestMaster constructor');
    }

    async getPipelineList() {
        c.log('2. RequestMaster getPipelineList() called');
        const requestParams = {
            metric: "pipeline_list",
        };
        const pipelines = await this.asyncRequest(requestParams);
        return pipelines;
    }

    async getLongestWaitingPipelines(startDate, endDate, limit) {
        const requestParams = {
            metric: "pipelines_with_the_highest_wait_time",
            start: startDate,
            end: endDate,
            limit: limit
        };
        const pipelines = await this.asyncRequest(requestParams);
        console.log("pipelines = ", pipelines);
        return pipelines;
    }

    async getAgentMostUtilized(startDate, endDate, limit) {
        const requestParams = {
            metric: "agents_with_the_highest_utilization",
            start: startDate,
            end: endDate,
            limit: limit
        };
        const agents = await this.asyncRequest(requestParams);
        console.log("agents = ", agents);
        return agents;
    }

    async getAgentLeastUtilized(startDate, endDate, limit) {
        const requestParams = {
            metric: "agents_with_the_lowest_utilization",
            start: startDate,
            end: endDate,
            limit: limit
        };
        const agents = await this.asyncRequest(requestParams);
        console.log("agents = ", agents);
        return agents;
    }

    async getJobWaitBuildTimeRatio(startDate, endDate, percentage, limit) {
        const requestParams = {
            metric: "wait_build_ratio",
            start: startDate,
            end: endDate,
            percentage: percentage,
            limit: limit
        };
        const wait_build_ratio = await this.asyncRequest(requestParams);
        console.log("wait build ratio = ", wait_build_ratio);
        return wait_build_ratio;
    }

    async getDoraMetrics(startDate, endDate, pipelineName) {
        const requestParams = {
            metric: "dora_metrics",
            pipeline_name: pipelineName,
            start: startDate,
            end: endDate
        };
        const doraMetrics = await this.asyncRequest(requestParams);
        return doraMetrics;
    }

    async getStageTimeline(startDate, endDate, pipelineName, requestResult, requestOrder, requestLimit) {
        const requestParams = {
            metric: "stage_timeline",
            pipeline_name: pipelineName,
            result: requestResult,
            order: requestOrder,
            limit: requestLimit,
            start: startDate,
            end: endDate
        };
        const stageTimelines = await this.asyncRequest(requestParams);
        return stageTimelines;
    }

    async getStageStartupTime(startDate, endDate, pipelineName, requestResult, requestOrder, requestLimit) {
        const requestParams = {
            metric: "stage_startup_time",
            start: startDate,
            end: endDate,
            pipeline_name: pipelineName,
            result: requestResult,
            order: requestOrder,
            limit: requestLimit
        };
        const stageTimelines = await this.asyncRequest(requestParams);
        return stageTimelines;
    }

    async getJobTimeline(stageName, pipelineCounterStart, pipelineCounterEnd) {
        const requestParams = {
            "type": "drilldown",
            metric: "job_timeline",
            stage_name: stageName,
            pipeline_counter_start: pipelineCounterStart,
            pipeline_counter_end: pipelineCounterEnd
        };
        const jobTimelines = await this.asyncRequest(requestParams);
        return jobTimelines;
    }

    async getPriorityPipeline(settings) {
        console.log("RequestMaster getPriorityPipeline() called ", settings);
        const requestParams = {
            "type": "drilldown",
            metric: "priority_pipeline",
            result: settings.result,
            start: settings.startDate,
            end: settings.endDate
        };
        const priorityPipeline = await this.asyncRequest(requestParams);
        return priorityPipeline;
    }

    async getPriorityStage(settings) {
        const requestParams = {
            "type": "drilldown",
            metric: "priority_stage",
            result: settings.result,
            start: settings.startDate,
            end: settings.endDate
        };
        const priorityStage = await this.asyncRequest(requestParams);
        return priorityStage;
    }

    async getPriorityJob(settings) {
        const requestParams = {
            "type": "drilldown",
            metric: "priority_job",
            result: settings.result,
            start: settings.startDate,
            end: settings.endDate
        };
        const priorityJob = await this.asyncRequest(requestParams);
        return priorityJob;
    }

    async getStageReruns(settings) {
        const requestParams = {
            "type": "dashboard",
            "metric": "stage_reruns",
            "pipeline_name": settings.selectedPipeline,
            "order": settings.requestOrder,
            "limit": settings.requestLimit,
            "start": settings.startDate,
            "end": settings.endDate
        }
        const stageReruns = await this.asyncRequest(requestParams);
        return stageReruns;
    }


    async asyncRequest(requestParams) {
        c.log('3. RequestMaster asyncRequest');
        c.log('🧩 asyncRequest() this.transport', this.transport);
        c.log('🧩 asyncRequest() requestParams ', requestParams);

        return new Promise((resolve) => {
            this.transport.request("fetch-analytics", requestParams)
                .done((data) => resolve(JSON.parse(data)))
                .fail(console.error.toString());
        });
    }
}

export default RequestMaster