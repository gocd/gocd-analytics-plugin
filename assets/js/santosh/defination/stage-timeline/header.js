import stageTimelineHeader from "./stage-timeline-header";
import jobTimelineHeader from "./job-timeline-header";
import pipelinePriorityHeader from "./pipeline-priority-header";
import priorityDetailsHeader from "./priority-details-header";
import stageRerunsHeader from "./stage-reruns-header";
import stageStartupHeader from "./stage-startup-header";
import longestWaitingPipelinesHeader from "./longest-waiting-pipelines-header";
import {
    convertDateObjectToDBDateFormat, formatDatePicker,
    getDateFromTimestampString, getFirstDayOfTheCurrentMonth, getHumanReadableDateFromDBFormatDate,
    getPreviousMonthDateInDBFormat,
    getTodaysDateInDBFormat
} from "../../utils";
import waitBuildTimeRatioHeader from "./wait-build-time-ratio-header";
import agentMetricsHeader from "./agent-metrics-header";
import doraMetricsHeader from "./dora-metrics-header";
import pipelineStateSummaryHeader from "./pipeline-state-summary-header";

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

        // console.log('1. Header stage-timeline constructor()');
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

    async getPriorityPipelineHeader(changeHandler, requestParams, savedSettings) {
        console.log('getPriorityPipelineHeader() must have been graph movement');

        const selectedResult = requestParams === null ? "" : requestParams.result;

        let settings = {
            scope: 'Pipelines',
            alignTicks: true,
            result: selectedResult,
            startDate: getPreviousMonthDateInDBFormat(),
            endDate: getTodaysDateInDBFormat()
        };

        const selectors = await pipelinePriorityHeader(this.#dom, handleDateSelect);

        selectors.resultFilterSelector.value = selectedResult;
        selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(settings.startDate)} - ${getHumanReadableDateFromDBFormatDate(settings.endDate)}`

        handleTicksSelect(selectors.ticksFilterSelector);
        handleScopeSelect(selectors.scopeFilterSelector);
        handleResultSelect(selectors.resultFilterSelector);

        function handleDateSelect(date1, date2) {
            console.log("handleDateSelect from header.js date1, date2 = ", date1, date2);


            settings.startDate = convertDateObjectToDBDateFormat(date1);
            settings.endDate = convertDateObjectToDBDateFormat(date2);

            changeHandler(settings);
        }

        function setSelectedTicks(result) {
            console.log("setting tick selector value = ", result);
            settings.alignTicks = result;
        }

        function handleTicksSelect(selector) {
            console.log('handleTicksSelect()');
            selector.addEventListener("change", () => {
                console.log("about to set selected ticks to ", selector.value);
                if (selector.value === "true") {
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
            console.log("handle result select()");
            selector.addEventListener("change", () => {
                console.log("about to set selected result to ", selector.value);
                setSelectedResult(selector.value);
                changeHandler(settings);
            });
        }

        // console.log('returning settings', settings);

        if (savedSettings != undefined) {
            console.log('savedSettings = ', savedSettings);

            savedSettings.result = savedSettings.result === "" ? selectedResult : savedSettings.result;

            selectors.scopeFilterSelector.value = savedSettings.scope;
            selectors.resultFilterSelector.value = savedSettings.result;
            selectors.ticksFilterSelector.value = savedSettings.alignTicks;

            // for alignment to work
            setSelectedScope(savedSettings.scope);
            setSelectedResult(savedSettings.result);
            setSelectedTicks(savedSettings.alignTicks);

            selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(savedSettings.startDate)} - ${getHumanReadableDateFromDBFormatDate(savedSettings.endDate)}`

            console.log("savedSettings returning = ", savedSettings);

            return savedSettings;
        }

        return settings;
    }

    async getPriorityDetailsHeader(changeHandler, selectedOptions, savedSettings) {
        // console.log('getPriorityDetailsHeader()');

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

        // console.log('returning settings', settings);

        if (savedSettings != undefined) {
            console.log('savedSettings = ', savedSettings);

            selectors.ticksFilterSelector.value = savedSettings.alignTicks;
            setSelectedTicks(savedSettings.alignTicks);

            return savedSettings;
        }

        return settings;
    }

    async getJobsTimelineHeader(changeHandler) {
        // console.log('getJobsTimelineHeader()');

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

        // console.log('returning settings', settings);

        return settings;
    }

    async getStageTimelineHeader(changeHandler, savedSettings) {
        let settings = {
            startDate: getFirstDayOfTheCurrentMonth(),
            endDate: getTodaysDateInDBFormat(),
            selectedPipeline: undefined,
            requestResult: 'Passed',
            requestOrder: 'DESC',
            requestLimit: 10,
            showPipelineCounterResult: 'Only Passed',
            showData: 'time_waiting_secs',
            showLegend: 'Show',
            visualizeVariation: true
        };

        const pipelines = await this.requestMaster.getPipelineList();
        // await console.log('pipelines = ', pipelines);

        const selectors = await stageTimelineHeader(pipelines.map(p => p.name), this.#dom, handleDateSelect, handlePipelineSelect);

        setSelectedPipeline(selectors.pipelineSelector.value);

        handleRequestResultSelect(selectors.requestResultSelector);
        handleRequestOrderSelect(selectors.requestOrderSelector);
        handleRequestLimitInput(selectors.requestLimitInput);
        handleResultSelect(selectors.resultFilterSelector);
        handleDataSelect(selectors.dataFilterSelector);
        handleLegendSelect(selectors.legendVisibilityFilterSelector);
        handleVisualizeVariationSelect(selectors.visualizeVariationSelector);

        function handleDateSelect(date1, date2) {
            console.log("handleDateSelect from header.js date1, date2 = ", date1, date2);

            settings.startDate = convertDateObjectToDBDateFormat(date1);
            settings.endDate = convertDateObjectToDBDateFormat(date2);

            changeHandler(settings);
        }

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

        function setSelectedVisualizeVariation(checked) {
            settings.visualizeVariation = checked;
        }

        function handlePipelineSelect(newValue) {
            console.log("handle pipeline select with value as ", newValue);
            setSelectedPipeline(newValue);

            changeHandler(settings);
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

        function handleVisualizeVariationSelect(selector) {
            selector.addEventListener("change", () => {

                if (selector.checked) {
                    setSelectedVisualizeVariation(true);
                } else {
                    setSelectedVisualizeVariation(false);
                }

                changeHandler(settings);
            });
        }

        if (savedSettings != undefined) {
            selectors.requestOrderSelector.value = savedSettings.requestOrder;
            selectors.visualizeVariationSelector.value = savedSettings.visualizeVariation;

            selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(savedSettings.startDate)} - ${getHumanReadableDateFromDBFormatDate(savedSettings.endDate)}`;

            selectors.pipelineSelector.setSelected(savedSettings.selectedPipeline, false);
            selectors.requestLimitInput.value = savedSettings.requestLimit;
            selectors.resultFilterSelector.value = savedSettings.showPipelineCounterResult;
            selectors.legendVisibilityFilterSelector.value = savedSettings.showLegend;
            selectors.dataFilterSelector.value = savedSettings.showData;

            settings.startDate = savedSettings.startDate;
            settings.endDate = savedSettings.endDate;

        } else {
            selectors.requestOrderSelector.value = settings.requestOrder;
            selectors.visualizeVariationSelector.checked = settings.visualizeVariation;

            selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(settings.startDate)} - ${getHumanReadableDateFromDBFormatDate(settings.endDate)}`;
        }

        return settings;
    }

    async getDoraMetricsHeader(changeHandler, savedSettings) {
        let settings = {
            startDate: getFirstDayOfTheCurrentMonth(),
            endDate: getTodaysDateInDBFormat(),
            selectedPipeline: undefined,
        };

        const pipelines = await this.requestMaster.getPipelineList();
        // await console.log('pipelines = ', pipelines);

        const selectors = await doraMetricsHeader(pipelines.map(p => p.name), this.#dom, handleDateSelect, handlePipelineSelect);

        setSelectedPipeline(selectors.pipelineSelector.value);

        function handleDateSelect(date1, date2) {
            console.log("handleDateSelect from header.js date1, date2 = ", date1, date2);

            settings.startDate = convertDateObjectToDBDateFormat(date1);
            settings.endDate = convertDateObjectToDBDateFormat(date2);

            changeHandler(settings);
        }

        function setSelectedPipeline(pipeline_name) {
            settings.selectedPipeline = pipeline_name;
        }

        function handlePipelineSelect(newValue) {
            console.log("handle pipeline select with value as ", newValue);
            setSelectedPipeline(newValue);

            changeHandler(settings);
        }

        if (savedSettings != undefined) {

            selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(savedSettings.startDate)} - ${getHumanReadableDateFromDBFormatDate(savedSettings.endDate)}`;

            selectors.pipelineSelector.setSelected(savedSettings.selectedPipeline, false);

            settings.startDate = savedSettings.startDate;
            settings.endDate = savedSettings.endDate;
        } else {

            selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(settings.startDate)} - ${getHumanReadableDateFromDBFormatDate(settings.endDate)}`;
        }

        return settings;
    }

    async getStageStartupHeader(changeHandler) {
        let settings = {
            startDate: getFirstDayOfTheCurrentMonth(),
            endDate: getTodaysDateInDBFormat(),
            selectedPipeline: undefined,
            // requestResult: 'Passed',
            requestOrder: 'DESC',
            requestLimit: 10,
            // showPipelineCounterResult: 'Only Passed',
            showData: 'time_waiting_secs',
            showLegend: 'Show'
        };

        const pipelines = await this.requestMaster.getPipelineList();
        // await console.log('pipelines = ', pipelines);

        const selectors = await stageStartupHeader(pipelines.map(p => p.name), this.#dom, handleDateSelect, handlePipelineSelect);

        selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(settings.startDate)} - ${getHumanReadableDateFromDBFormatDate(settings.endDate)}`;

        setSelectedPipeline(selectors.pipelineSelector.value);

        handleRequestResultSelect(selectors.requestResultSelector);
        handleRequestOrderSelect(selectors.requestOrderSelector);
        handleRequestLimitInput(selectors.requestLimitInput);

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

        function handleDateSelect(date1, date2) {
            console.log("handleDateSelect from header.js date1, date2 = ", date1, date2);

            settings.startDate = convertDateObjectToDBDateFormat(date1);
            settings.endDate = convertDateObjectToDBDateFormat(date2);

            changeHandler(settings);
        }

        function handlePipelineSelect(newValue) {
            console.log("handle pipeline select with value as ", newValue);
            setSelectedPipeline(newValue);

            changeHandler(settings);
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

        return settings;
    }

    async getStageRerunsHeader(changeHandler) {
        let settings = {
            selectedPipeline: '',
            requestOrder: 'DESC',
            requestLimit: 10,
            requestMinimumStageCounter: 2,
            startDate: getFirstDayOfTheCurrentMonth(),
            endDate: getTodaysDateInDBFormat(),
        };

        const pipelines = await this.requestMaster.getPipelineList();
        // await console.log('pipelines = ', pipelines);

        const selectors = await stageRerunsHeader(pipelines.map(p => p.name), this.#dom, handleDateSelect);

        selectors.requestOrderSelector.value = settings.requestOrder;
        selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(settings.startDate)} - ${getHumanReadableDateFromDBFormatDate(settings.endDate)}`;

        setSelectedPipeline(selectors.pipelineSelector.value);

        handlePipelineSelect(selectors.pipelineSelector);
        // handleRequestResultSelect(selectors.requestResultSelector);
        handleRequestOrderSelect(selectors.requestOrderSelector);
        handleRequestLimitInput(selectors.requestLimitInput);
        // handleResultSelect(selectors.resultFilterSelector);
        // handleDataSelect(selectors.dataFilterSelector);
        // handleLegendSelect(selectors.legendVisibilityFilterSelector);

        function handleDateSelect(date1, date2) {
            console.log("handleDateSelect from header.js date1, date2 = ", date1, date2);

            settings.startDate = convertDateObjectToDBDateFormat(date1);
            settings.endDate = convertDateObjectToDBDateFormat(date2);

            changeHandler(settings);
        }

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

    async getLongestWaitingPipelinesHeader(changeHandler, savedSettings) {

        console.log("Santosh getLongestWaitingPipelinesHeader savedSettings = ", savedSettings);

        let settings = {
            truncateOrder: 'Last',
            startDate: getFirstDayOfTheCurrentMonth(),
            endDate: getTodaysDateInDBFormat(),
            limit: 10
        };

        const selectors = await longestWaitingPipelinesHeader(this.#dom, handleDateSelect);

        handleTruncateOrderSelect(selectors.truncateOrderSelector);

        function handleDateSelect(date1, date2) {
            console.log("handleDateSelect from header.js date1, date2 = ", date1, date2);

            settings.startDate = convertDateObjectToDBDateFormat(date1);
            settings.endDate = convertDateObjectToDBDateFormat(date2);

            changeHandler(settings);
        }

        function handleTruncateOrderSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedTruncateOrder(selector.value);

                changeHandler(settings);
            });
        }

        function setSelectedTruncateOrder(result) {
            settings.truncateOrder = result;
        }

        if (savedSettings != undefined) {
            console.log("Santosh savedSettings check");

            console.log('savedSettings = ', savedSettings);
            selectors.truncateOrderSelector.value = savedSettings.truncateOrder;

            selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(savedSettings.startDate)} - ${getHumanReadableDateFromDBFormatDate(savedSettings.endDate)}`;

            return savedSettings;
        } else {
            console.log("Santosh savedSettings check is undefined");
            console.log("getting start date and end date from settings = ", settings.startDate, settings.endDate);
            selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(settings.startDate)} - ${getHumanReadableDateFromDBFormatDate(settings.endDate)}`;
        }

        return settings;
    }

    async getWaitBuildTimeRatioHeader(changeHandler, savedSettings) {

        console.log("Santosh getWaitBuildTimeRatioHeader savedSettings = ", savedSettings);

        let settings = {
            truncateOrder: 'Last',
            startDate: getFirstDayOfTheCurrentMonth(),
            endDate: getTodaysDateInDBFormat(),
            percentage: 10,
            limit: 10
        };

        const selectors = await waitBuildTimeRatioHeader(this.#dom, handleDateSelect);

        console.log("Santosh getWaitBuildTimeRatioHeader selectors = ", selectors);

        // handleTruncateOrderSelect(selectors.truncateOrderSelector);

        handlePercentageChange(selectors.percentageSelector);

        console.log("Santosh getWaitBuildTimeRatioHeader handle truncate order select done");

        function handleDateSelect(date1, date2) {
            console.log("handleDateSelect from header.js date1, date2 = ", date1, date2);

            settings.startDate = convertDateObjectToDBDateFormat(date1);
            settings.endDate = convertDateObjectToDBDateFormat(date2);

            changeHandler(settings);
        }

        function handleTruncateOrderSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedTruncateOrder(selector.value);

                changeHandler(settings);
            });
        }

        function setSelectedTruncateOrder(result) {
            settings.truncateOrder = result;
        }

        function handlePercentageChange(selector) {
            selector.addEventListener("change", () => {
                setPercentage(selector.value);

                changeHandler(settings);
            });
        }

        function setPercentage(percent) {
            settings.percentage = percent;
        }

        if (savedSettings != undefined) {
            console.log("Santosh savedSettings check");

            console.log('savedSettings = ', savedSettings);
            selectors.truncateOrderSelector.value = savedSettings.truncateOrder;

            selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(savedSettings.startDate)} - ${getHumanReadableDateFromDBFormatDate(savedSettings.endDate)}`;

            return savedSettings;
        } else {
            console.log("Santosh savedSettings check is undefined");
            console.log("getting start date and end date from settings = ", settings.startDate, settings.endDate);
            selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(settings.startDate)} - ${getHumanReadableDateFromDBFormatDate(settings.endDate)}`;
        }

        console.log("Santosh getWaitBuildTimeRatioHeader returning settings = ", settings);

        return settings;
    }

    async getAgentMetricsHeader(changeHandler, savedSettings) {

        console.log("Santosh getAgentMetricsHeader savedSettings = ", savedSettings);

        let settings = {
            truncateOrder: 'Last',
            startDate: getFirstDayOfTheCurrentMonth(),
            endDate: getTodaysDateInDBFormat(),
            occurence: 'Most',
            metric: 'Wanted',
            limit: 10
        };

        const selectors = await agentMetricsHeader(this.#dom, handleDateSelect);

        console.log("Santosh getAgentMetricsHeader selectors = ", selectors);

        // handleTruncateOrderSelect(selectors.truncateOrderSelector);

        handleOccurenceSelect(selectors.occurenceFilterSelector);

        handleMetricSelect(selectors.metricFilterSelector);

        function handleDateSelect(date1, date2) {
            console.log("handleDateSelect from header.js date1, date2 = ", date1, date2);

            settings.startDate = convertDateObjectToDBDateFormat(date1);
            settings.endDate = convertDateObjectToDBDateFormat(date2);

            changeHandler(settings);
        }

        function handleTruncateOrderSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedTruncateOrder(selector.value);

                changeHandler(settings);
            });
        }

        function setSelectedTruncateOrder(result) {
            settings.truncateOrder = result;
        }

        function handleOccurenceSelect(selector) {
            selector.addEventListener("change", () => {
                setSelectedOccurence(selector.value);

                changeHandler(settings);
            });
        }

        function setSelectedOccurence(occurence) {
            settings.occurence = occurence;
        }

        function handleMetricSelect(selector) {
            selector.addEventListener("change", () => {
                console.log("agent metric, metric option selected with value = ", selector.value);

                setSelectedMetric(selector.value);

                changeHandler(settings);
            });
        }

        function setSelectedMetric(metric) {
            settings.metric = metric;
        }

        if (savedSettings != undefined) {
            console.log("Santosh savedSettings check");

            console.log('savedSettings = ', savedSettings);
            selectors.truncateOrderSelector.value = savedSettings.truncateOrder;

            selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(savedSettings.startDate)} - ${getHumanReadableDateFromDBFormatDate(savedSettings.endDate)}`;

            return savedSettings;
        } else {
            console.log("Santosh savedSettings check is undefined");
            console.log("getting start date and end date from settings = ", settings.startDate, settings.endDate);
            selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(settings.startDate)} - ${getHumanReadableDateFromDBFormatDate(settings.endDate)}`;
        }

        console.log("Santosh getWaitBuildTimeRatioHeader returning settings = ", settings);

        return settings;
    }

    async getPipelineStateSummaryHeader(changeHandler, savedSettings) {

        console.log("Santosh getPipelineStateSummaryHeader savedSettings = ", savedSettings);

        let settings = {
            startDate: getFirstDayOfTheCurrentMonth(),
            endDate: getTodaysDateInDBFormat(),
        };

        const selectors = await pipelineStateSummaryHeader(this.#dom, handleDateSelect);

        function handleDateSelect(date1, date2) {
            console.log("handleDateSelect from header.js date1, date2 = ", date1, date2);

            settings.startDate = convertDateObjectToDBDateFormat(date1);
            settings.endDate = convertDateObjectToDBDateFormat(date2);

            changeHandler(settings);
        }

        if (savedSettings != undefined) {
            console.log("Santosh savedSettings check");

            console.log('savedSettings = ', savedSettings);

            selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(savedSettings.startDate)} - ${getHumanReadableDateFromDBFormatDate(savedSettings.endDate)}`;

            return savedSettings;
        } else {
            console.log("Santosh savedSettings check is undefined");
            console.log("getting start date and end date from settings = ", settings.startDate, settings.endDate);
            selectors.dateFilterSelector.textContent = `${getHumanReadableDateFromDBFormatDate(settings.startDate)} - ${getHumanReadableDateFromDBFormatDate(settings.endDate)}`;
        }

        return settings;
    }

    async clear() {
        this.#dom.innerHTML = "";
    }

}

export default Header;