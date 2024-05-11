import {addOptionsToSelect} from "../../utils";
import SlimSelect from 'slim-select';

// const chartMeta = document.getElementById("chart-container-meta");
let pipelineSelector = undefined;
let dateSelector = undefined;
let requestResultSelector = undefined;
let requestOrderSelector = undefined;
let requestLimitSelector = undefined;
let resultFilterSelector = undefined;
let dataFilterSelector = undefined;
let legendVisibilityFilterSelector = undefined;

async function stageTimelineHeader(pipelines, settingsDOM) {

    console.log('✅ 11 stage-timeline-header now I will add pipelines to select ', pipelines);

    await addOptionHeader(settingsDOM);
    pipelineSelector = document.getElementById("pipeline");
    requestResultSelector = document.getElementById("requestResult");
    requestOrderSelector = document.getElementById("requestOrder");
    requestLimitSelector = document.getElementById("requestLimit");
    resultFilterSelector = document.getElementById("resultFilter");
    dataFilterSelector = document.getElementById("dataFilter");
    legendVisibilityFilterSelector = document.getElementById("legendVisibilityFilter");

    await addOptionsToSelect(pipelineSelector, pipelines);

    new SlimSelect({
        select: '#pipeline'
    })

    await addOptionsToSelect(requestResultSelector, [{text: "Passed", value: "Passed"}, {
        text: "Failed",
        value: "Failed"
    }, {text: "Cancelled", value: "Cancelled"}, {text: "Any", valuer: "Any"}]);

    await addOptionsToSelect(requestOrderSelector, [{text: "Ascending", value: "ASC"}, {
        text: "Descending",
        value: "DESC"
    }]);

    await addOptionsToSelect(resultFilterSelector, [{text: "Only Passed", value: "Only Passed"}, {
        text: "Only Failed",
        value: "Only Failed"
    }, {text: "Only Cancelled", value: "Only Cancelled"}, {text: "Show both", value: "Show both"}]);

    // new SlimSelect({
    //     select: '#resultFilter'
    // })

    await addOptionsToSelect(dataFilterSelector, [{
        text: "Waiting time",
        value: "time_waiting_secs"
    }, {text: "Building time", value: "time_building_secs"}, {text: "Total time", value: "total_time_secs"}]);

    // new SlimSelect({
    //     select: '#dataFilter'
    // })

    await addOptionsToSelect(legendVisibilityFilterSelector, [{text: "Show", value: "Show"}, {
        text: "Hide",
        value: "Hide"
    }]);


    return {
        pipelineSelector,
        requestResultSelector,
        resultFilterSelector,
        dataFilterSelector,
        legendVisibilityFilterSelector
    };
}

async function addOptionHeader(settingsDOM) {
    settingsDOM.innerHTML = `
    <div style="position:relative; display: flex; flex-direction: column">
    
    <div id="setting-1" style="display: flex; flex-direction: row">
    
    <div style="font-size:18px; flex-grow: 1"><b>Settings</b></div>

<!--    <div style="flex-grow: 1">-->
<!--    <input type="checkbox" id="weird" name="weird" value="weird">-->
<!--    <label for="weird"> Show only if data have delta</label>-->
<!--    </div>-->
    Request ->
    <div style="display: flex; flex-direction: row; flex-grow: 1">
    Result: <select id="requestResult"></select>
</div>

<div style="display: flex; flex-direction: row; flex-grow: 1">
    Order: <select id="requestOrder"></select>
</div>

<div style="display: flex; flex-direction: row; flex-grow: 1">
    Limit: <input id="requestLimit" type="number" min="1" value="10"/>
</div>

<div style="display: flex; flex-direction: row; flex-grow: 7">
    Pipeline: <select id="pipeline"></select>
</div>

</div>

<div id="setting-2" style="display: flex; flex-direction: row">


<div>
Filter result: <select id="resultFilter">
</select>
</div>

<div>
Metric: <select id="dataFilter">
</select>
</div>

<div>
Show legend: <select id="legendVisibilityFilter"></select>
</div>

</div>

</div>
<hr>
    `;
}

export default stageTimelineHeader;