import {addOptionsToSelect} from "../../utils";
import SlimSelect from 'slim-select';
import {DateManager} from "../../DateManager";

// const chartMeta = document.getElementById("chart-container-meta");
let dateFilterSelector = undefined;
let pipelineSelector = undefined;

async function doraMetricHeader(pipelines, settingsDOM, dateSelectedEvent, pipelineSelectedEvent) {

    console.log('✅ 11 stage-timeline-header now I will add pipelines to select ', pipelines);

    await addOptionHeader(settingsDOM);

    dateFilterSelector = document.getElementById("dateFilter");

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

    return {
        dateFilterSelector,
        pipelineSelector,
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

<div style="display: flex; flex-direction: row; flex-grow: 7">
    Pipeline: <select id="pipeline"></select>
</div>

</div>

</div>

<hr>
    `;
}

export default doraMetricHeader;