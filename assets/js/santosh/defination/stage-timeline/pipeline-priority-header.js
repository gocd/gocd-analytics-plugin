import {addOptionsToSelect} from "../../utils";

// const chartMeta = document.getElementById("chart-container-meta");
let ticksFilterSelector = undefined;
let scopeFilterSelector = undefined;
let resultFilterSelector = undefined;

async function pipelinePriorityHeader(settingsDOM) {

    await addOptionHeader(settingsDOM);

    ticksFilterSelector = document.getElementById("ticksFilter");
    scopeFilterSelector = document.getElementById("scopeFilter");
    resultFilterSelector = document.getElementById("resultFilter");

    await addOptionsToSelect(ticksFilterSelector, [{
        text: "Align Y-Axis Ticks",
        value: "true"
    }, {text: "Don't align Y-Axis Ticks", value: "false"}]);
    await addOptionsToSelect(scopeFilterSelector, ["Pipelines", "Stages", "Jobs"]);
    await addOptionsToSelect(resultFilterSelector, ["Passed", "Failed", "Cancelled"]);

    return {
        ticksFilterSelector: ticksFilterSelector,
        scopeFilterSelector: scopeFilterSelector,
        resultFilterSelector: resultFilterSelector
    };
}

async function addOptionHeader(settingsDOM) {
    settingsDOM.innerHTML = `
    <div style="position:relative;"><span style="font-size:18px"><b>Setting</b></span>

    <input type="checkbox" id="weird" name="weird" value="weird">
    <label for="weird"> Show only if data have delta</label>

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