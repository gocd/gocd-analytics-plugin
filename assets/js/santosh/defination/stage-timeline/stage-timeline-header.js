import {addOptionsToSelect} from "../../utils";
import SlimSelect from 'slim-select';
import {DateManager} from "../../DateManager";

// const chartMeta = document.getElementById("chart-container-meta");
let dateFilterSelector = undefined;
let pipelineSelector = undefined;
let requestResultSelector = undefined;
let requestOrderSelector = undefined;
let requestLimitInput = undefined;
let resultFilterSelector = undefined;
let dataFilterSelector = undefined;
let legendVisibilityFilterSelector = undefined;
let visualizeVariationSelector = undefined;

async function stageTimelineHeader(pipelines, settingsDOM, dateSelectedEvent, pipelineSelectedEvent) {

    console.log('✅ 11 stage-timeline-header now I will add pipelines to select ', pipelines);

    await addOptionHeader(settingsDOM);

    dateFilterSelector = document.getElementById("dateFilter");
    requestResultSelector = document.getElementById("requestResult");
    requestOrderSelector = document.getElementById("requestOrder");
    requestLimitInput = document.getElementById("requestLimit");
    resultFilterSelector = document.getElementById("resultFilter");
    dataFilterSelector = document.getElementById("dataFilter");
    legendVisibilityFilterSelector = document.getElementById("legendVisibilityFilter");
    visualizeVariationSelector = document.getElementById("visualizeVariation");

    const dm = new DateManager();
    const datePicker = await dm.addDatelitePickerDiv(dateFilterSelector, dateSelectedEvent);
    datePicker.hide();

    dateFilterSelector.addEventListener("click", onDatePickerClick);

    function onDatePickerClick() {
        datePicker.show();
    }

    const actualPipelineSelector = document.getElementById("pipeline");
    await addOptionsToSelect(actualPipelineSelector, pipelines);

    const slimSelect = new SlimSelect({
        select: '#pipeline',
        events: {
            afterChange: (newVal) => {
                console.log("afterChange ", newVal);
                pipelineSelectedEvent(newVal[0].value);
            }
        }
    });
    pipelineSelector = slimSelect;

    await addOptionsToSelect(requestResultSelector, [{text: "Passed", value: "Passed"}, {
        text: "Failed",
        value: "Failed"
    }, {text: "Cancelled", value: "Cancelled"}, {text: "Any", value: "Any"}]);

    await addOptionsToSelect(requestOrderSelector, [{text: "Ascending", value: "ASC"}, {
        text: "Descending",
        value: "DESC"
    }]);

    await addOptionsToSelect(resultFilterSelector, [{text: "Only Passed", value: "Only Passed"},
        {text: "Any Passed", value: "Any Passed"},
        {
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
        dateFilterSelector,
        pipelineSelector,
        requestResultSelector,
        resultFilterSelector,
        requestOrderSelector,
        requestLimitInput,
        dataFilterSelector,
        legendVisibilityFilterSelector,
        visualizeVariationSelector
    };
}

async function addOptionHeader(settingsDOM) {
    settingsDOM.innerHTML = `
    <div style="position:relative; display: flex; flex-direction: column">
    
    <div id="setting-1" style="display: flex; flex-direction: row">
    
    <div style="font-size:18px; flex-grow: 1">
        <b>Settings</b>
        <span id="dateFilter"></span>
    </div>

<!--    <div style="flex-grow: 1">-->
<!--    <input type="checkbox" id="weird" name="weird" value="weird">-->
<!--    <label for="weird"> Show only if data have delta</label>-->
<!--    </div>-->
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

<div>
<input type="checkbox" id="visualizeVariation" />
    <label for="visualizeVariation">Visualize Variation</label>
</div>

</div>

</div>
<hr>
    `;
}

export default stageTimelineHeader;