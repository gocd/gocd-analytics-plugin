import {addOptionsToSelect} from "../../utils";
import SlimSelect from 'slim-select';

// const chartMeta = document.getElementById("chart-container-meta");
let pipelineSelector = undefined;
let resultFilterSelector = undefined;
let dataFilterSelector = undefined;
let legendVisibilityFilterSelector = undefined;

async function stageTimelineHeader(pipelines, settingsDOM) {

    console.log('✅ 11 stage-timeline-header now I will add pipelines to select ', pipelines);

    await addOptionHeader(settingsDOM);
    pipelineSelector = document.getElementById("pipeline");
    resultFilterSelector = document.getElementById("resultFilter");
    dataFilterSelector = document.getElementById("dataFilter");
    legendVisibilityFilterSelector = document.getElementById("legendVisibilityFilter");

    await addOptionsToSelect(pipelineSelector, pipelines);

    new SlimSelect({
        select: '#pipeline'
    })

    await addOptionsToSelect(resultFilterSelector, ["Only Passed", "Only Failed", "Show both"]);

    // new SlimSelect({
    //     select: '#resultFilter'
    // })

    await addOptionsToSelect(dataFilterSelector, ["time_waiting_secs", "time_building_secs", "total_time_secs"]);

    // new SlimSelect({
    //     select: '#dataFilter'
    // })

    await addOptionsToSelect(legendVisibilityFilterSelector, ["Show", "Hide"]);


    return {pipelineSelector, resultFilterSelector, dataFilterSelector, legendVisibilityFilterSelector};
}

async function addOptionHeader(settingsDOM) {
    settingsDOM.innerHTML = `
    <div style="position:relative; display: flex; flex-direction: column">
    
    <div id="setting-1" style="display: flex; flex-direction: row">
    
    <div style="font-size:18px; flex-grow: 1"><b>Settings</b></div>

    <div style="flex-grow: 1">
    <input type="checkbox" id="weird" name="weird" value="weird">
    <label for="weird"> Show only if data have delta</label>
    </div>

<div style="display: flex; flex-direction: row; flex-grow: 8">
    Pipeline: <select id="pipeline"></select>
</div>

</div>

<div id="setting-2" style="display: flex; flex-direction: row">


<div>
Filter: <select id="resultFilter">
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