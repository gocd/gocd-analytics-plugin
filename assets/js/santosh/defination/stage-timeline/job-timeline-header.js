import {addOptionsToSelect} from "../../utils";

// const chartMeta = document.getElementById("chart-container-meta");
let resultFilterSelector = undefined;
let dataFilterSelector = undefined;

async function jobTimelineHeader(settingsDOM) {

    await addOptionHeader(settingsDOM);

    resultFilterSelector = document.getElementById("resultFilter");
    dataFilterSelector = document.getElementById("dataFilter");

    await addOptionsToSelect(resultFilterSelector, ["Only Passed", "Only Failed", "Show both"]);
    await addOptionsToSelect(dataFilterSelector, ["time_waiting_secs", "time_building_secs", "total_time_secs"]);

    return {resultFilterSelector, dataFilterSelector};
}

async function addOptionHeader(settingsDOM) {
    settingsDOM.innerHTML = `
    <div style="position:relative;"><span style="font-size:18px"><b>Setting</b></span>

    <input type="checkbox" id="weird" name="weird" value="weird">
    <label for="weird"> Show only if data have delta</label>

    <select id="resultFilter" style="float:right">
</select>

<select id="dataFilter" style="float:right">
</select>

</div>
<hr>
    `;
}


export default jobTimelineHeader;