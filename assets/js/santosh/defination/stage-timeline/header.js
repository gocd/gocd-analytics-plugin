import stageTimelineHeader from "./stage-timeline-header";
import jobTimelineHeader from "./job-timeline-header";
import pipelinePriorityHeader from "./pipeline-priority-header";
import priorityDetailsHeader from "./priority-details-header";
import stageRerunsHeader from "./stage-reruns-header";
import stageStartupHeader from "./stage-startup-header";
import longestWaitingPipelinesHeader from "./longest-waiting-pipelines-header";

class Header {

    #dom

    constructor(requestMaster) {

        // const header = document.getElementById('chart-container-meta');
        //
        // const settingsDiv = document.createElement('div');
        // settingsDiv.setAttribute('id', 'settings');
        //
        // header.append(settingsDiv);

        this.#dom = document.getElementById('settings');

        this.settings = {
            selectedPipeline: undefined,
            showPipelineCounterResult: 'Passed',
            showData: 'time_waiting_secs'
        };
        this.requestMaster = requestMaster;

        console.log('1. Header stage-timeline constructor()');
    }

    setSelectedPipeline(pipelineName) {
        this.settings.selectedPipeline = pipelineName;
    }

    switchHeader(graphName) {
        switch (graphName) {
            case 'JobsTimeline':
                return this.getJobsTimelineHeader();
        }
    }

    async getPriorityPipelineHeader(changeHandler, selectedResult) {
        console.log('getPriorityPipelineHeader()');

        let settings = {
            scope: 'Pipelines',
            alignTicks: true,
            result: selectedResult
        };

        const selectors = await pipelinePriorityHeader(this.#dom);

        selectors.resultFilterSelector.value = selectedResult;

        handleTicksSelect(selectors.ticksFilterSelector);
        handleScopeSelect(selectors.scopeFilterSelector);
        handleResultSelect(selectors.resultFilterSelector);

        function setSelectedTicks(result) {
            settings.alignTicks = result;
        }

        function handleTicksSelect(selector) {
            selector.addEventListener("change", () => {
                if (selector.value === 'Align Y-Axis Ticks') {
                    setSelectedTicks(true);
                } else {
                    setSelectedTicks(false);
                }
                changeHandler(settings);
            });
        }

        function setSelectedScope(result) {
            settings.scope = result;
        }

        function handleScopeSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedScope(selector.value);
                changeHandler(settings);
            });
        }

        function setSelectedResult(result) {
            settings.result = result;
        }

        function handleResultSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedResult(selector.value);
                changeHandler(settings);
            });
        }

        console.log('returning settings', settings);

        return settings;
    }

    async getPriorityDetailsHeader(changeHandler, selectedOptions) {
        console.log('getPriorityDetailsHeader()');

        let settings = {
            alignTicks: true,
        };

        const selectors = await priorityDetailsHeader(selectedOptions, this.#dom);

        handleTicksSelect(selectors.ticksFilterSelector);

        function setSelectedTicks(result) {
            settings.alignTicks = result;
        }

        function handleTicksSelect(selector) {
            selector.addEventListener("change", () => {
                if (selector.value === 'Align Y-Axis Ticks') {
                    setSelectedTicks(true);
                } else {
                    setSelectedTicks(false);
                }
                changeHandler(settings);
            });
        }

        console.log('returning settings', settings);

        return settings;
    }

    async getJobsTimelineHeader(changeHandler) {
        console.log('getJobsTimelineHeader()');

        let settings = {
            showPipelineCounterResult: 'Only Passed',
            showData: 'time_waiting_secs'
        };

        const selectors = await jobTimelineHeader(this.#dom);

        handleResultSelect(selectors.resultFilterSelector);
        handleDataSelect(selectors.dataFilterSelector);

        function setSelectedResult(result) {
            settings.showPipelineCounterResult = result;
        }

        function setSelectedData(data) {
            settings.showData = data;
        }

        function handleResultSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedResult(selector.value);

                changeHandler(settings);
            });
        }

        function handleDataSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedData(selector.value);

                changeHandler(settings);
            });
        }

        console.log('returning settings', settings);

        return settings;
    }

    async getStageTimelineHeader(changeHandler) {
        let settings = {
            selectedPipeline: undefined,
            requestResult: 'Passed',
            requestOrder: 'DESC',
            requestLimit: 10,
            showPipelineCounterResult: 'Only Passed',
            showData: 'time_waiting_secs',
            showLegend: 'Show'
        };

        const pipelines = await this.requestMaster.getPipelineList();
        await console.log('pipelines = ', pipelines);

        const selectors = await stageTimelineHeader(pipelines.map(p => p.name), this.#dom);

        setSelectedPipeline(selectors.pipelineSelector.value);

        handlePipelineSelect(selectors.pipelineSelector);
        handleRequestResultSelect(selectors.requestResultSelector);
        handleRequestOrderSelect(selectors.requestOrderSelector);
        handleRequestLimitInput(selectors.requestLimitInput);
        handleResultSelect(selectors.resultFilterSelector);
        handleDataSelect(selectors.dataFilterSelector);
        handleLegendSelect(selectors.legendVisibilityFilterSelector);

        function setSelectedPipeline(pipeline_name) {
            settings.selectedPipeline = pipeline_name;
        }

        function setSelectedRequestResult(result) {
            settings.requestResult = result;
        }

        function setSelectedRequestOrder(result) {
            settings.requestOrder = result;
        }

        function setSelectedRequestLimit(result) {
            settings.requestLimit = result;
        }

        function setSelectedResult(result) {
            settings.showPipelineCounterResult = result;
        }

        function setSelectedData(data) {
            settings.showData = data;
        }

        function setSelectedLegendFilter(data) {
            settings.showLegend = data;
        }

        function handlePipelineSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedPipeline(selector.value);

                changeHandler(settings);
            });
        }

        function handleRequestResultSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedRequestResult(selector.value);

                changeHandler(settings);
            });
        }

        function handleRequestOrderSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedRequestOrder(selector.value);

                changeHandler(settings);
            });
        }

        function handleRequestLimitInput(selector) {
            selector.addEventListener("change", () => {
                setSelectedRequestLimit(selector.value);

                changeHandler(settings);
            });
        }

        function handleResultSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedResult(selector.value);

                changeHandler(settings);
            });
        }

        function handleDataSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedData(selector.value);

                changeHandler(settings);
            });
        }

        function handleLegendSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedLegendFilter(selector.value);

                changeHandler(settings);
            });
        }

        return settings;
    }

    async getStageStartupHeader(changeHandler) {
        let settings = {
            selectedPipeline: undefined,
            // requestResult: 'Passed',
            requestOrder: 'DESC',
            requestLimit: 10,
            // showPipelineCounterResult: 'Only Passed',
            showData: 'time_waiting_secs',
            showLegend: 'Show'
        };

        const pipelines = await this.requestMaster.getPipelineList();
        await console.log('pipelines = ', pipelines);

        const selectors = await stageStartupHeader(pipelines.map(p => p.name), this.#dom);

        setSelectedPipeline(selectors.pipelineSelector.value);

        handlePipelineSelect(selectors.pipelineSelector);
        // handleRequestResultSelect(selectors.requestResultSelector);
        handleRequestOrderSelect(selectors.requestOrderSelector);
        handleRequestLimitInput(selectors.requestLimitInput);
        // handleResultSelect(selectors.resultFilterSelector);
        // handleDataSelect(selectors.dataFilterSelector);
        handleLegendSelect(selectors.legendVisibilityFilterSelector);

        function setSelectedPipeline(pipeline_name) {
            settings.selectedPipeline = pipeline_name;
        }

        function setSelectedRequestResult(result) {
            settings.requestResult = result;
        }

        function setSelectedRequestOrder(result) {
            settings.requestOrder = result;
        }

        function setSelectedRequestLimit(result) {
            settings.requestLimit = result;
        }

        function setSelectedResult(result) {
            settings.showPipelineCounterResult = result;
        }

        function setSelectedData(data) {
            settings.showData = data;
        }

        function setSelectedLegendFilter(data) {
            settings.showLegend = data;
        }

        function handlePipelineSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedPipeline(selector.value);

                changeHandler(settings);
            });
        }

        function handleRequestResultSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedRequestResult(selector.value);

                changeHandler(settings);
            });
        }

        function handleRequestOrderSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedRequestOrder(selector.value);

                changeHandler(settings);
            });
        }

        function handleRequestLimitInput(selector) {
            selector.addEventListener("change", () => {
                setSelectedRequestLimit(selector.value);

                changeHandler(settings);
            });
        }

        function handleResultSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedResult(selector.value);

                changeHandler(settings);
            });
        }

        function handleDataSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedData(selector.value);

                changeHandler(settings);
            });
        }

        function handleLegendSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedLegendFilter(selector.value);

                changeHandler(settings);
            });
        }

        return settings;
    }

    async getStageRerunsHeader(changeHandler) {
        let settings = {
            selectedPipeline: '',
            // requestResult: 'Passed',
            requestOrder: 'DESC',
            requestLimit: 10,
            requestMinimumStageCounter: 2,
            // showPipelineCounterResult: 'Only Passed',
            // showData: 'time_waiting_secs',
            // showLegend: 'Show'
        };

        const pipelines = await this.requestMaster.getPipelineList();
        await console.log('pipelines = ', pipelines);

        const selectors = await stageRerunsHeader(pipelines.map(p => p.name), this.#dom);

        selectors.requestOrderSelector.value = settings.requestOrder;

        setSelectedPipeline(selectors.pipelineSelector.value);

        handlePipelineSelect(selectors.pipelineSelector);
        // handleRequestResultSelect(selectors.requestResultSelector);
        handleRequestOrderSelect(selectors.requestOrderSelector);
        handleRequestLimitInput(selectors.requestLimitInput);
        // handleResultSelect(selectors.resultFilterSelector);
        // handleDataSelect(selectors.dataFilterSelector);
        // handleLegendSelect(selectors.legendVisibilityFilterSelector);

        function setSelectedPipeline(pipeline_name) {
            settings.selectedPipeline = pipeline_name;
        }

        function setSelectedRequestResult(result) {
            settings.requestResult = result;
        }

        function setSelectedRequestOrder(result) {
            settings.requestOrder = result;
        }

        function setSelectedRequestLimit(result) {
            settings.requestLimit = result;
        }

        function setSelectedResult(result) {
            settings.showPipelineCounterResult = result;
        }

        function setSelectedData(data) {
            settings.showData = data;
        }

        function setSelectedLegendFilter(data) {
            settings.showLegend = data;
        }

        function handlePipelineSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedPipeline(selector.value);

                changeHandler(settings);
            });
        }

        function handleRequestResultSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedRequestResult(selector.value);

                changeHandler(settings);
            });
        }

        function handleRequestOrderSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedRequestOrder(selector.value);

                changeHandler(settings);
            });
        }

        function handleRequestLimitInput(selector) {
            selector.addEventListener("change", () => {
                setSelectedRequestLimit(selector.value);

                changeHandler(settings);
            });
        }

        function handleResultSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedResult(selector.value);

                changeHandler(settings);
            });
        }

        function handleDataSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedData(selector.value);

                changeHandler(settings);
            });
        }

        function handleLegendSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedLegendFilter(selector.value);

                changeHandler(settings);
            });
        }

        return settings;
    }

    async getLongestWaitingPipelinesHeader(changeHandler) {
        let settings = {
            truncateOrder: 'Last'
        };

        const selectors = await longestWaitingPipelinesHeader(this.#dom);

        handleTruncateOrderSelect(selectors.truncateOrderSelector);

        function handleTruncateOrderSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedTruncateOrder(selector.value);

                changeHandler(settings);
            });
        }

        function setSelectedTruncateOrder(result) {
            settings.truncateOrder = result;
        }

        return settings;
    }

}

export default Header;