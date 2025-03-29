import {addOptionsToSelect, formatDatePicker} from "../../utils";
import {DateManager} from "../../DateManager";
import {init} from "../../../../settings";

// const chartMeta = document.getElementById("chart-container-meta");
let dateFilterSelector = undefined;
let occurenceFilterSelector = undefined;
let metricFilterSelector = undefined;

async function agentMetricsHeader(settingsDOM, dateSelectedEvent) {

    await addOptionHeader(settingsDOM);
    let is_settings_header_visible = true;
    await init(is_settings_header_visible);

    dateFilterSelector = document.getElementById("dateFilter");
    occurenceFilterSelector = document.getElementById("occurenceFilter");
    metricFilterSelector = document.getElementById("metricFilter");

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

    await addOptionsToSelect(occurenceFilterSelector, ["Most", "Least"]);
    await addOptionsToSelect(metricFilterSelector, ["Wanted", "Popular", "Awaited", "Busy"]);

    return {
        dateFilterSelector: dateFilterSelector,
        occurenceFilterSelector: occurenceFilterSelector,
        metricFilterSelector: metricFilterSelector,
    };
}

async function addOptionHeader(settingsDOM) {
    settingsDOM.innerHTML = `
    <div style="position:relative;"><span id="settings-title" style="font-size:18px; font-weight: bold">Settings</span>

    <div id="settings-content">

    <span id="dateFilter"></span>

<span style="float:right">Metric <select id="metricFilter" >
</select>
</span>

<span style="float:right">Occurence <select id="occurenceFilter" >
</select>
</span>

</div>

</div>
<hr>
    `;
}


export default agentMetricsHeader;