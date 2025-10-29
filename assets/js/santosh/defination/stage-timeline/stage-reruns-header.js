import {
  addOptionsToSelect,
  firstDateOfYear,
  formatDatePicker, lastDateOfYear
} from "../../utils";
import SlimSelect from 'slim-select';
import 'slim-select/dist/slimselect.css';
import {setOrderSelector} from "../../DomManager";
import {DateManager} from "../../DateManager";

// const chartMeta = document.getElementById("chart-container-meta");
let dateFilterSelector = undefined;
let pipelineSelector = undefined;
let dateSelector = undefined;
let requestOrderSelector = undefined;
let viewSelector = undefined;

let month_1_selector = undefined;
let month_2_selector = undefined;

let year_selector = undefined;
let year_1_selector = undefined;
let year_2_selector = undefined;
let re_runs_after_selector = undefined;

let date_selected_event_hook = undefined;

let associated_setting_custom_selector = undefined;
let associated_setting_monthly_selector = undefined;
let associated_setting_yearly_selector = undefined;
let associated_setting_year_to_year_selector = undefined;

async function onViewClick(event) {
  const selectedOptions = Array.from(event.target.selectedOptions).map(
      option => option.value);
  console.log("you clicked ", selectedOptions);

  // TODO: check to see if there' a pipeline selected

  switch (event.target.selectedIndex) {
    case 0:
      hideAllAdditionalSettings(associated_setting_custom_selector);
      break;
    case 1:
      hideAllAdditionalSettings(associated_setting_monthly_selector);
      break;
    case 2:
      hideAllAdditionalSettings(associated_setting_yearly_selector);
      break;
    case 3:
      hideAllAdditionalSettings(associated_setting_year_to_year_selector);
      break;
    default:
      console.error("Unknown selected index", event);
  }
}

function hideAllAdditionalSettings(except) {
  associated_setting_custom_selector.style.display = "none";
  associated_setting_monthly_selector.style.display = "none";
  associated_setting_yearly_selector.style.display = "none";
  associated_setting_year_to_year_selector.style.display = "none";

  except.style.display = "flex";
}

function nativeOnPipelineClick(event) {
  console.log("nativeOnPipelineClick with selectedIndex",
      event.target.selectedIndex);
  if (event.target.selectedIndex === 0) {
    disableAllOptionsOfExceptForIndex();
  } else {
    enableAllOptions();
  }
}

function disableAllOptionsOfExceptForIndex(selector, index) {
  if (viewSelector) {
    for (let i = 1; i < viewSelector.options.length; i++) {
      viewSelector.options[i].disabled = true;
    }
  } else {
    console.log("invalid selector");
  }
}

function enableAllOptions() {
  if (viewSelector) {
    for (let i = 0; i < viewSelector.options.length; i++) {
      viewSelector.options[i].disabled = false;
    }
  }
}

function defineSettingSelectors() {
  dateFilterSelector = document.getElementById("dateFilter");
  pipelineSelector = document.getElementById("pipeline");
  requestOrderSelector = document.getElementById("requestOrder");
  viewSelector = document.getElementById("view");
}

function defineAdditionalSettingSelectors() {
  associated_setting_custom_selector = document.getElementById(
      "custom_date_setting_div");
  associated_setting_monthly_selector = document.getElementById(
      "monthly_date_setting_div");
  associated_setting_yearly_selector = document.getElementById(
      "yearly_date_setting_div");
  associated_setting_year_to_year_selector = document.getElementById(
      "year_to_year_date_setting_div");
}

function defineAdditionalSettingComponentSelectors() {
  month_1_selector = document.getElementById("month_1");
  month_2_selector = document.getElementById("month_2");

  year_selector = document.getElementById("year_input");

  year_1_selector = document.getElementById("year_1_input");
  year_2_selector = document.getElementById("year_2_input");

  re_runs_after_selector = document.getElementById("re_runs_after");
}

async function populateSettingComponents(pipelines) {
  await addOptionsToSelect(pipelineSelector, ['*** All ***', ...pipelines]);

  await addOptionsToSelect(viewSelector,
      ["Custom date", "Month to month", "Yearly", "Year to year"]);
}

async function populateAdditionalSettingComponents() {
  const months = ["January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"];

  await addOptionsToSelect(month_1_selector, months);
  await addOptionsToSelect(month_2_selector, months);

  const trigger_after_list = ["Any", "Failure", "Success", "Cancellation"];

  await addOptionsToSelect(re_runs_after_selector, trigger_after_list);
}

async function manageDatePicker(dateSelectedEvent) {
  const dm = new DateManager();
  const datePicker = await dm.addDatelitePickerDiv(dateFilterSelector,
      dateSelectedEvent);
  datePicker.hide();

  dateFilterSelector.addEventListener("click", onDatePickerClick);

  function onDatePickerClick() {
    datePicker.show();
  }
}

function defineEventListeners() {
  viewSelector.addEventListener("change", onViewClick);

  pipelineSelector.addEventListener("change", nativeOnPipelineClick);

  year_selector.addEventListener("change", () => {
    const year = Number(year_selector.value);
    date_selected_event_hook(firstDateOfYear(year), lastDateOfYear(year));
  });
}

async function stageRerunsHeader(pipelines, settingsDOM, dateSelectedEvent) {

  date_selected_event_hook = dateSelectedEvent;

  await addOptionHeader(settingsDOM);

  defineSettingSelectors();

  defineAdditionalSettingSelectors();

  defineAdditionalSettingComponentSelectors();

  await populateSettingComponents(pipelines);

  await populateAdditionalSettingComponents();

  await manageDatePicker(dateSelectedEvent);

  hideAllAdditionalSettings(associated_setting_custom_selector);

  defineEventListeners();

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
    viewSelector,
    re_runs_after_selector
  };
}

const custom_date_setting_html = `
<div id="custom_date_setting_div">
  <span id="dateFilter"></span>
      <div style="display: flex; flex-direction: row; flex-grow: 1">
         Order: <select id="requestOrder"></select>
      </div>
</div>
`;

const monthly_date_setting_html = `
  <div id="monthly_date_setting_div" style="display: flex; flex-direction: row; flex-grow: 1">
         Month 1: <select id="month_1"></select>
         Month 2: <select id="month_2"></select>
  </div>
`;

const yearly_date_setting_html = `
      <div id="yearly_date_setting_div" style="display: flex; flex-direction: row; flex-grow: 1">
         Year: <input id="year_input" type="number" />
         Re-runs after: <select id="re_runs_after"></select>
         Triggered by <select>
            <option>Any</option>
            <option>Change</option>
            <option>User</option>
         </select>
      </div>
`;

const year_to_year_date_setting_html = `
      <div id="year_to_year_date_setting_div" style="display: flex; flex-direction: row; flex-grow: 1">
         Year 1: <input id="year_1_input" type="number" />
         Year 2: <input id="year_2_input" type="number" />
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