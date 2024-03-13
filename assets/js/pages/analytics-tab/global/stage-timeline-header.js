import {updateChartSize} from "../../../santosh/utils";
import stageTimeline from "../../../santosh/defination/stage-timeline";

const chartMeta = document.getElementById("chart-container-meta");
let pipelineSelector = undefined;

async function stageTimelineHeader(pipelines) {

    console.log('✅ 11 stage-timeline-header now I will add pipelines to select ', pipelines);

    await addPipelineSelect();
    pipelineSelector = document.getElementById("pipeline");

    await addPipelineNamesToSelect(pipelines);

    return pipelineSelector;
}

async function addPipelineSelect() {
    chartMeta.innerHTML = `
    <div style="position:relative;"><span style="font-size:18px"><b>Stage timeline across workflow</b></span>

    <input type="checkbox" id="weird" name="weird" value="weird">
    <label for="weird"> Weird proof</label>

    <select id="pipeline" style="float:right">
</select>
</div>
<hr>
    `;
}

function prepareStageTimeline(transport) {
    // option = pipelineTimeline();
    // option && myChart.setOption(option);

    const graphManager = new GraphManager("standalone", null);
    graphManager.initStandalone("pipeline-timeline", data);
}

async function addPipelineNamesToSelect(pipelines) {



    pipelines.forEach((pipeline) => {
        const selectOption = document.createElement("option");
        selectOption.setAttribute("value", pipeline.name);
        selectOption.text = pipeline.name;

        pipelineSelector.appendChild(selectOption);
    });

    pipelineSelector.selectedIndex = 0;
}

export default stageTimelineHeader;