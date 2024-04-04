import stageTimelineHeader from "./stage-timeline-header";
import GraphManager from "../../GraphManager";
import {asyncRequest} from "../../utils";
import jobTimelineHeader from "./job-timeline-header";
import pipelinePriorityHeader from "./pipeline-priority-header";
import requestMaster from "../../../RequestMaster";
import priorityDetailsHeader from "./priority-details-header";

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
                if(selector.value === 'Align Y-Axis Ticks') {
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
                if(selector.value === 'Align Y-Axis Ticks') {
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
            showPipelineCounterResult: 'Only Passed',
            showData: 'time_waiting_secs',
            showLegend: 'Show'
        };

        const pipelines = await this.requestMaster.getPipelineList();
        await console.log('pipelines = ', pipelines);

        const selectors = await stageTimelineHeader(pipelines.map(p => p.name), this.#dom);

        setSelectedPipeline(selectors.pipelineSelector.value);

        handlePipelineSelect(selectors.pipelineSelector);
        handleResultSelect(selectors.resultFilterSelector);
        handleDataSelect(selectors.dataFilterSelector);
        handleLegendSelect(selectors.legendVisibilityFilterSelector);

        function setSelectedPipeline(pipeline_name) {
            settings.selectedPipeline = pipeline_name;
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

}

export default Header;