import {addOptionsToSelect, formatDatePicker} from "../../utils";
import {DateManager} from "../../DateManager";

let dateFilterSelector = undefined;
let truncateOrderSelector = undefined;

async function longestWaitingPipelinesHeader(settingsDOM, dateSelectedEvent) {

    await addOptionHeader(settingsDOM);

    dateFilterSelector = document.getElementById("dateFilter");
    truncateOrderSelector = document.getElementById("truncateOrder");

    const dm = new DateManager();
    const datePicker = await dm.addDatelitePickerDiv(dateFilterSelector, dateSelectedEvent);
    datePicker.hide();

    dateFilterSelector.addEventListener("click", onDatePickerClick);

    function onDatePickerClick() {
        datePicker.show();
    }

    await addOptionsToSelect(truncateOrder, [{text: "Last", value: "Last"}, {
        text: "Middle",
        value: "Middle"
    }, {text: "None", value: "None"}]);

    return {
        truncateOrderSelector,
        dateFilterSelector,
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
    
    <div style="display: flex; flex-direction: row; flex-grow: 1">
    Truncate Order: <select id="truncateOrder"></select>
</div>

</div>

</div>
<hr>
    `;
}

export default longestWaitingPipelinesHeader;