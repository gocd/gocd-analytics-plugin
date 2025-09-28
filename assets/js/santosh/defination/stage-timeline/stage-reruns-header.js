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

async function onViewClick(event) {
  const selectedOptions = Array.from(event.target.selectedOptions).map(option => option.value);
  console.log("you clicked ", selectedOptions);

  // TODO: check to see if there' a pipeline selected

  const div_associated_settings_body = document.getElementById("associated-settings-body");

  switch(event.target.selectedIndex) {
    case 0:
      break;
    case 1:
      await monthly_date_setting_html(div_associated_settings_body);
      break;
    case 2:
      break;
    case 3:
      break;
    default:
      console.error("Unknown selected index", event);
  }
}

function nativeOnPipelineClick(event) {
  console.log("nativeOnPipelineClick with selectedIndex", event.target.selectedIndex);
  if(event.target.selectedIndex === 0) {
    disableAllOptionsOfExceptForIndex();
  } else {
    enableAllOptions();
  }
}

function disableAllOptionsOfExceptForIndex(selector, index) {
  if(viewSelector) {
    for(let i = 1; i < viewSelector.options.length; i++) {
      viewSelector.options[i].disabled = true;
    }
  } else {
    console.log("invalid selector");
  }
}

function enableAllOptions() {
  if(viewSelector) {
    for(let i = 0; i < viewSelector.options.length; i++) {
      viewSelector.options[i].disabled = false;
    }
  }
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

    disableAllOptionsOfExceptForIndex(viewSelector, 1);

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
      viewSelector
    };
}

const custom_date_setting_html = `
  <span id="dateFilter"></span>
      
      <div style="display: flex; flex-direction: row; flex-grow: 1">
         Order: <select id="requestOrder"></select>
      </div>
`;

const monthly_date_setting_html = `
  <div id="monthly_date_setting_div" style="display: flex; flex-direction: row; flex-grow: 1">
         Month 1: <select id="month_1"></select>
         Month 2: <select id="month_2"></select>
  </div>
`;

  output_div.innerHTML = html;

  const month_1_selector = document.getElementById("month_1");
  const month_2_selector = document.getElementById("month_2");

  const months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];

  await addOptionsToSelect(month_1_selector, months);
  await addOptionsToSelect(month_2_selector, months);
}

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
      <div style="font-size:16px; flex-direction: row; flex-grow: 1">
        <b>Associated Settings</b>
        <div id="associated-settings-body">
            ${custom_date_setting_html}
            ${monthly_date_setting_html}
            ${yearly_date_setting_html}
            ${year_to_year_date_setting_html}
        </div>
      </div>
   </div>
</div>
<hr>
    `;
}

export default stageRerunsHeader;