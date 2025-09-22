import {addOptionsToSelect, formatDatePicker} from "../../utils";
import SlimSelect from 'slim-select';
import {setOrderSelector} from "../../DomManager";
import {DateManager} from "../../DateManager";

// const chartMeta = document.getElementById("chart-container-meta");
let dateFilterSelector = undefined;
let pipelineSelector = undefined;
let dateSelector = undefined;
let requestOrderSelector = undefined;
let viewSelector = undefined;

function onViewClick(event) {
  const selectedOptions = Array.from(event.target.selectedOptions).map(option => option.value);
  console.log("you clicked ", selectedOptions);
}

async function stageRerunsHeader(pipelines, settingsDOM, dateSelectedEvent) {

    await addOptionHeader(settingsDOM);

    dateFilterSelector = document.getElementById("dateFilter");
    pipelineSelector = document.getElementById("pipeline");
    requestOrderSelector = document.getElementById("requestOrder");
    viewSelector = document.getElementById("view");

    const dm = new DateManager();
    const datePicker = await dm.addDatelitePickerDiv(dateFilterSelector, dateSelectedEvent);
    datePicker.hide();

    dateFilterSelector.addEventListener("click", onDatePickerClick);
    viewSelector.addEventListener("change", onViewClick);

    pipelineSelector.addEventListener("change", nativeOnPipelineClick);

    function onDatePickerClick() {
        datePicker.show();
    }

    await addOptionsToSelect(pipelineSelector, ['*** All ***', ...pipelines]);

    await addOptionsToSelect(viewSelector, ["Custom date", "Month to month", "Yearly", "Year to year"]);

  if(viewSelector) {
    for(let i = 1; i < viewSelector.options.length; i++) {
      viewSelector.options[i].disabled = true;
    }
  }

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
    };
}

const custom_date_setting_html = `
  <span id="dateFilter"></span>
      
      <div style="display: flex; flex-direction: row; flex-grow: 1">
         Order: <select id="requestOrder"></select>
      </div>
`;

const monthly_date_setting_html = `
  <div style="display: flex; flex-direction: row; flex-grow: 1">
         Month 1: <select id="month_1">
          <option>January</option>
         </select>
      </div>
`;

async function addOptionHeader(settingsDOM) {
    settingsDOM.innerHTML = `
<div style="position:relative; display: flex; flex-direction: column">
  <div id="setting-header" style="display: flex; flex-direction: row">
    <div style="font-size:18px; flex-grow: 1">
      <b>Stage re-runs</b>
    </div>
    <div style="display: flex; flex-direction: row; flex-grow: 5">
        for Pipeline: <select id="pipeline"></select>
        view: <select id="view"></select>
    </div>
   </div>
   <div id="setting-1" style="display: flex; flex-direction: row">
      <div style="font-size:16px; flex-grow: 1"><b>Associated Settings</b></div>
      ${custom_date_setting_html}
   </div>
</div>
<hr>
    `;
}

export default stageRerunsHeader;