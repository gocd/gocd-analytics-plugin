import {addOptionsToSelect, formatDatePicker} from "../../utils";
import {DateManager} from "../../DateManager";

// const chartMeta = document.getElementById("chart-container-meta");
let dateFilterSelector = undefined;
let ticksFilterSelector = undefined;
let scopeFilterSelector = undefined;
let resultFilterSelector = undefined;

async function pipelinePriorityHeader(settingsDOM, dateSelectedEvent) {

    await addOptionHeader(settingsDOM);

    dateFilterSelector = document.getElementById("dateFilter");
    ticksFilterSelector = document.getElementById("ticksFilter");
    scopeFilterSelector = document.getElementById("scopeFilter");
    resultFilterSelector = document.getElementById("resultFilter");

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

    await addOptionsToSelect(ticksFilterSelector, [{
        text: "Align Y-Axis Ticks",
        value: "true"
    }, {text: "Don't align Y-Axis Ticks", value: "false"}]);
    await addOptionsToSelect(scopeFilterSelector, ["Pipelines", "Stages", "Jobs"]);
    await addOptionsToSelect(resultFilterSelector, ["Passed", "Failed", "Cancelled"]);

    return {
        dateFilterSelector: dateFilterSelector,
        ticksFilterSelector: ticksFilterSelector,
        scopeFilterSelector: scopeFilterSelector,
        resultFilterSelector: resultFilterSelector
    };
}

async function addOptionHeader(settingsDOM) {
    settingsDOM.innerHTML = `
    <div style="position:relative;"><span style="font-size:18px"><b>Settings</b></span>

    <span id="dateFilter"></span>

    <select id="ticksFilter" style="float:right">
</select>

<span style="float:right">Scope <select id="scopeFilter" >
</select>
</span>

<span style="float:right">Result <select id="resultFilter" >
</select>
</span>

</div>
<hr>
    `;
}


export default pipelinePriorityHeader;