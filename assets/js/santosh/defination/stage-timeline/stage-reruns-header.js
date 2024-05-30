import {addOptionsToSelect} from "../../utils";
import SlimSelect from 'slim-select';
import {setOrderSelector} from "../../DomManager";

// const chartMeta = document.getElementById("chart-container-meta");
let pipelineSelector = undefined;
let dateSelector = undefined;
let requestOrderSelector = undefined;
let requestLimitInput = undefined;

async function stageRerunsHeader(pipelines, settingsDOM) {

    await addOptionHeader(settingsDOM);

    pipelineSelector = document.getElementById("pipeline");
    requestOrderSelector = document.getElementById("requestOrder");
    requestLimitInput = document.getElementById("requestLimit");

    await addOptionsToSelect(pipelineSelector, ['*** All ***', ...pipelines]);

    new SlimSelect({
        select: '#pipeline'
    })

    // await addOptionsToSelect(requestOrderSelector, [{text: "Ascending", value: "ASC"}, {
    //     text: "Descending",
    //     value: "DESC"
    // }]);

    await setOrderSelector(requestOrderSelector);

    return {
        pipelineSelector,
        requestOrderSelector,
        requestLimitInput,
    };
}

async function addOptionHeader(settingsDOM) {
    settingsDOM.innerHTML = `
<div style="position:relative; display: flex; flex-direction: column">
   <div id="setting-1" style="display: flex; flex-direction: row">
      <div style="font-size:18px; flex-grow: 1"><b>Settings</b></div>
      Request ->
      <div style="display: flex; flex-direction: row; flex-grow: 1">
         Order: <select id="requestOrder"></select>
      </div>
      <div style="display: flex; flex-direction: row; flex-grow: 1">
         Limit: <input id="requestLimit" type="number" min="1" value="10"/>
      </div>
      <div style="display: flex; flex-direction: row; flex-grow: 7">
        Pipeline: <select id="pipeline"></select>
      </div>
   </div>
</div>
<hr>
    `;
}

export default stageRerunsHeader;