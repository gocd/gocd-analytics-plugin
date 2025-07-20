import {addOptionsToSelect, formatDatePicker} from "../../utils";
import {DateManager} from "../../DateManager";

let dateFilterSelector = undefined;

async function pipelineStateSummaryHeader(settingsDOM, dateSelectedEvent) {

    await addOptionHeader(settingsDOM);

    dateFilterSelector = document.getElementById("dateFilter");

    const dm = new DateManager();
    const datePicker = await dm.addDatelitePickerDiv(dateFilterSelector, dateSelectedEvent);
    datePicker.hide();

    dateFilterSelector.addEventListener("click", onDatePickerClick);

    function onDatePickerClick() {
        datePicker.show();
    }

    return {
        dateFilterSelector,
    };
}

async function addOptionHeader(settingsDOM) {
    settingsDOM.innerHTML = `
    <div style="position:relative; display: flex; flex-direction: column">
    
    <div id="setting-1" style="display: flex; flex-direction: row">
    
    <div style="font-size:18px; flex-grow: 1">
    <b>Pipeline State Summary / Settings</b>
    <span id="dateFilter"></span>
    </div>

</div>

</div>
<hr>
    `;
}

export default pipelineStateSummaryHeader;