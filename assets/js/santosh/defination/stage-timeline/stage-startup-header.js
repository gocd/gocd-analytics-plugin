import {addOptionsToSelect} from "../../utils";
import SlimSelect from 'slim-select';
import {DateManager} from "../../DateManager";

// const chartMeta = document.getElementById("chart-container-meta");
let pipelineSelector = undefined;
let dateFilterSelector = undefined;
let requestResultSelector = undefined;
let requestOrderSelector = undefined;
let requestLimitInput = undefined;

async function stageStartupHeader(pipelines, settingsDOM, dateSelectedEvent, pipelineSelectedEvent) {

    console.log('✅ stage-startup-header now I will add pipelines to select ', pipelines);

    await addOptionHeader(settingsDOM);
    dateFilterSelector = document.getElementById("dateFilter");
    requestResultSelector = document.getElementById("requestResult");
    requestOrderSelector = document.getElementById("requestOrder");
    requestLimitInput = document.getElementById("requestLimit");

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
                pipelineSelectedEvent(newVal[0].value);
            }
        }
    })
    pipelineSelector = slimSelect;

    await addOptionsToSelect(requestResultSelector,
        [
            {text: "Only Passed", value: "Passed"},
            {text: "Only Failed", value: "Failed"},
            {text: "Only Cancelled", value: "Cancelled"},
            {text: "Any", value: "Any"}
        ]
    );

    await addOptionsToSelect(requestOrderSelector, [{text: "Ascending", value: "ASC"}, {
        text: "Descending",
        value: "DESC"
    }]);

    return {
        dateFilterSelector,
        pipelineSelector,
        requestResultSelector,
        requestOrderSelector,
        requestLimitInput,
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
<!--    <div style="display: flex; flex-direction: row; flex-grow: 1">-->
<!--    Result: <select id="requestResult"></select>-->
<!--</div>-->

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

</div>
<hr>
    `;
}

export default stageStartupHeader;