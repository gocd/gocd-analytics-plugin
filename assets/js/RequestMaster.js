class RequestMaster {
    constructor(transport) {
        this.transport = transport;
        console.log('1. RequestMaster constructor');
    }

    async getPipelineList() {
        console.log('2. RequestMaster getPipelineList() called');
        const requestParams = {
            metric: "pipeline_list",
        };
        const pipelines = await this.asyncRequest(requestParams);
        return pipelines;
    }

    async getStageTimeline(pipelineName) {
        const requestParams = {
            metric: "stage_timeline", pipeline_name: pipelineName,
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

    async getPriorityPipeline(result) {
        const requestParams = {
            "type": "drilldown",
            metric: "priority_pipeline",
            result:result
        };
        const priorityPipeline = await this.asyncRequest(requestParams);
        return priorityPipeline;
    }

    async getPriorityStage(result) {
        const requestParams = {
            "type": "drilldown",
            metric: "priority_stage",
            result: result
        };
        const priorityStage = await this.asyncRequest(requestParams);
        return priorityStage;
    }

    async getPriorityJob(result) {
        const requestParams = {
            "type": "drilldown",
            metric: "priority_job",
            result: result
        };
        const priorityJob = await this.asyncRequest(requestParams);
        return priorityJob;
    }


    async asyncRequest(requestParams) {
        console.log('3. RequestMaster asyncRequest');
        console.log('🧩 asyncRequest() this.transport, requestParams ', this.transport, requestParams);
        return new Promise((resolve) => {
            this.transport.request("fetch-analytics", requestParams)
                .done((data) => resolve(JSON.parse(data)))
                .fail(console.error.toString());
        });
    }
}

export default RequestMaster