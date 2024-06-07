import {addOptionsToSelect} from "../../utils";

let truncateOrderSelector = undefined;

async function longestWaitingPipelinesHeader(settingsDOM) {

    await addOptionHeader(settingsDOM);
    truncateOrderSelector = document.getElementById("truncateOrder");

    await addOptionsToSelect(truncateOrder, [{text: "Last", value: "Last"}, {
        text: "Middle",
        value: "Middle"
    }, {text: "None", value: "None"}]);

    return {
        truncateOrderSelector
    };
}

async function addOptionHeader(settingsDOM) {
    settingsDOM.innerHTML = `
    <div style="position:relative; display: flex; flex-direction: column">
    
    <div id="setting-1" style="display: flex; flex-direction: row">
    
    <div style="font-size:18px; flex-grow: 1"><b>Settings</b></div>

    <div style="display: flex; flex-direction: row; flex-grow: 1">
    Truncate Order: <select id="truncateOrder"></select>
</div>

</div>

</div>
<hr>
    `;
}

export default longestWaitingPipelinesHeader;