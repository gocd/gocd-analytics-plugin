import {addOptionsToSelect, formatDatePicker} from "../../utils";
import {DateManager} from "../../DateManager";
import {init} from "../../../../settings";

// const chartMeta = document.getElementById("chart-container-meta");
let dateFilterSelector = undefined;
let scopeFilterSelector = undefined;
let resultFilterSelector = undefined;
let percentageSelector = undefined;

async function waitBuildTimeRatioHeader(settingsDOM, dateSelectedEvent) {

    await addOptionHeader(settingsDOM);
    let is_settings_header_visible = true
    await init(is_settings_header_visible);

    dateFilterSelector = document.getElementById("dateFilter");
    scopeFilterSelector = document.getElementById("scopeFilter");
    resultFilterSelector = document.getElementById("resultFilter");
    percentageSelector = document.getElementById("percentage");

    console.log('dateFilterSelector', dateFilterSelector);

    const dm = new DateManager();
    const datePicker = await dm.addDatelitePickerDiv(dateFilterSelector, dateSelected);
    datePicker.hide();

    dateFilterSelector.addEventListener("click", onDatePickerClick);

    function dateSelected(date1, date2) {
        console.log("dataSelected");

        console.log('date1', date1);
        console.log('date2', date2);

        const formattedDate = `${formatDatePicker(date1)} - ${formatDatePicker(date2)}`
        dateFilterSelector.textContent = formattedDate;

        dateSelectedEvent(date1, date2);
    }

    function onDatePickerClick() {
        console.log("datePicker clicked");
        datePicker.show();
    }

    await addOptionsToSelect(scopeFilterSelector, ["Jobs", "Agents"]);
    await addOptionsToSelect(resultFilterSelector, ["Any", "Passed", "Failed", "Cancelled"]);

    return {
        dateFilterSelector: dateFilterSelector,
        scopeFilterSelector: scopeFilterSelector,
        resultFilterSelector: resultFilterSelector,
        percentageSelector: percentageSelector
    };
}

async function addOptionHeader(settingsDOM) {
    settingsDOM.innerHTML = `
    <div style="position:relative;"><span id="settings-title" style="font-size:18px; font-weight: bold">Settings</span>

    <div id="settings-content">

    <span id="dateFilter"></span>

<span style="float:right">Scope <select id="scopeFilter" >
</select>
</span>

<span style="float:right">Result <select id="resultFilter" >
</select>
</span>

<div>Waiting time is <input type="number" id="percentage"/> % of it's building time</div>

</div>

</div>
<hr>
    `;
}


export default waitBuildTimeRatioHeader;