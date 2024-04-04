import {addOptionsToSelect} from "../../utils";

// const chartMeta = document.getElementById("chart-container-meta");
let ticksFilterSelector = undefined;
let scopeFilterSelector = undefined;
let resultFilterSelector = undefined;

async function priorityDetailsHeader(selectedOptions, settingsDOM) {

    await addOptionHeader(selectedOptions, settingsDOM);

    ticksFilterSelector = document.getElementById("ticksFilter");

    await addOptionsToSelect(ticksFilterSelector, ["Align Y-Axis Ticks", "Don't align Y-Axis Ticks"]);

    return {ticksFilterSelector: ticksFilterSelector};
}

async function addOptionHeader(selectedOptions, settingsDOM) {
    settingsDOM.innerHTML = `
    <div style="position:relative;"><span style="font-size:18px"><b>Setting</b></span>

    <input type="checkbox" id="weird" name="weird" value="weird">
    <label for="weird"> Show only if data have delta</label>

    <select id="ticksFilter" style="float:right">
</select>

<span style="float:right">Scope <b>${selectedOptions.scope}</b>
</span>

<span style="float:right">Result <b>${selectedOptions.result}</b>
</span>

</div>
<hr>
    `;
}

export default priorityDetailsHeader;