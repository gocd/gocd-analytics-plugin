import {addOptionsToSelect, formatDatePicker} from "../../utils";
import SlimSelect from 'slim-select';
import {setOrderSelector} from "../../DomManager";
import {DateManager} from "../../DateManager";

// const chartMeta = document.getElementById("chart-container-meta");
let dateFilterSelector = undefined;
let pipelineSelector = undefined;
let dateSelector = undefined;
let requestOrderSelector = undefined;
let requestLimitInput = undefined;

async function stageRerunsHeader(pipelines, settingsDOM, dateSelectedEvent) {

    await addOptionHeader(settingsDOM);

    dateFilterSelector = document.getElementById("dateFilter");
    pipelineSelector = document.getElementById("pipeline");
    requestOrderSelector = document.getElementById("requestOrder");
    requestLimitInput = document.getElementById("requestLimit");

    const dm = new DateManager();
    const datePicker = await dm.addDatelitePickerDiv(dateFilterSelector, dateSelectedEvent);
    datePicker.hide();

    dateFilterSelector.addEventListener("click", onDatePickerClick);

    function onDatePickerClick() {
        datePicker.show();
    }

    await addOptionsToSelect(pipelineSelector, ['*** All ***', ...pipelines]);

    new SlimSelect({
        select: '#pipeline'
    })

    // await addOptionsToSelect(requestOrderSelector, [{text: "Ascending", value: "ASC"}, {
    //     text: "Descending",
    //     value: "DESC"
    // }]);

    await setOrderSelector(requestOrderSelector);

    return {
        dateFilterSelector: dateFilterSelector,
        pipelineSelector,
        requestOrderSelector,
        requestLimitInput,
    };
}

async function addOptionHeader(settingsDOM) {
    settingsDOM.innerHTML = `
<div style="position:relative; display: flex; flex-direction: column">
   <div id="setting-1" style="display: flex; flex-direction: row">
      <div style="font-size:18px; flex-grow: 1"><b>Settings</b></div>
      <span id="dateFilter"></span>
      
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

export default stageRerunsHeader;