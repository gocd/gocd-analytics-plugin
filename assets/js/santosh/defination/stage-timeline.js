import _ from "lodash";

import * as echarts from "echarts";
import { updateChartSize } from "../utils";

let info = null;

function groupByPipelineCounter(objects) {
  // const obj = [{pipeline_counter: 1}];
  // return _.groupBy(objects, 'pipeline_counter');

  const grouped = {};

  for (const obj of objects) {
    const { pipeline_counter } = obj;

    if (!grouped[pipeline_counter]) {
      grouped[pipeline_counter] = [];
    }

    grouped[pipeline_counter].push(obj);
  }

  return grouped;
}

function removeDuplicateStages(grouped) {
  console.log("removeDuplicateStages grouped = ", grouped);

  const groupedStages = [];

  // Group objects by stage_name
  for (const obj of grouped) {
    const { stage_name, scheduled_at } = obj;

    if (
      !groupedStages[stage_name] ||
      scheduled_at > groupedStages[stage_name].scheduled_at
    ) {
      groupedStages[stage_name] = obj;
    }
  }

  // Convert the grouped stages back to an array
  const uniqueStages = Object.values(groupedStages);

  console.log("removeDuplicate result = ", uniqueStages);

  return uniqueStages;
}

function getUniqueStageNames(data) {
  let stage_names = [];
  data.forEach((pipline_counter) => {
    pipline_counter.forEach((stage_info) => {
      stage_names.push(stage_info.stage_name);
    });
  });

  const uniqueStageNameSet = new Set(stage_names);
  console.log(uniqueStageNameSet);

  const uniqueStageNameArray = Array.from(uniqueStageNameSet);

  console.log("unqiue stage names as array ", uniqueStageNameArray);

  return uniqueStageNameArray;
}

function getKeys() {
  console.log("going to get keys from info which is ", info);

  let keys = [];
  keys = Object.keys(info);

  console.log("keys = ", keys);

  return keys;
}

function prepareData(data) {
  console.log("data received by prepareData = ", data);

  const grouped = groupByPipelineCounter(data);

  console.log("grouped: ", grouped);

  const updatedGroup = Object.entries(grouped).map(removeDuplicateStages);

  console.log("updated group = ", updatedGroup);

  info = updatedGroup;

  return updatedGroup;
}

function groupBy(list, key) {
  return list.reduce((acc, cur) => {
    const keyValue = cur[key];
    acc[keyValue] = acc[keyValue] || [];
    acc[keyValue].push(cur);
    return acc;
  }, {});
}

function uniqBy(list, property) {
  const seenValues = new Set();
  return list.filter((item) => {
    const value = item[property];
    if (!seenValues.has(value)) {
      seenValues.add(value);
      return true;
    }
    return false;
  });
}

function uniq(arr) {
  return [...new Set(arr)];
}

function getStageCompletionTime(dataArray, targetStageName) {
  for (const item of dataArray) {
    if (item.stage_name === targetStageName) {
      return item.total_time_secs;
    }
  }
  // If the specified stage name is not found, return an appropriate value (e.g., -1).
  return 0;
}

export default function stageTimeline(data, myChart) {
  //   prepareData(data);

  //   getKeys();

  console.log("stageTimeline data = ", data);

  //   console.log("VERSION", _.VERSION);

  // console.log("lodash now ", _.now());

  // console.log("Math.floor", _.groupBy([6.1, 4.2, 6.3], Math.floor));

  // console.log("length", _.groupBy(["one", "two", "three"], "length"));

  const groupedResult = groupBy(data, "pipeline_counter");

  for (const key in groupedResult) {
    // Sort the array in descending order of scheduled_at
    groupedResult[key].sort(
      (a, b) => new Date(b.scheduled_at) - new Date(a.scheduled_at)
    );

    // Remove duplicates based on stage_name
    groupedResult[key] = uniqBy(groupedResult[key], "stage_name");
  }

  console.log("groupedResult ", groupedResult);

  let keys = Object.keys(groupedResult);
  console.log("keys", keys);

  let stageNames = uniq(
    Object.values(groupedResult).flatMap((item) =>
      item.map((entry) => entry.stage_name)
    )
  );

  console.log("stageNames", stageNames);
  console.log("stageNames type array = ", Array.isArray(stageNames));

  let rdata = [];

  // for (const index in groupedResult) {
  //   const d = [];
  //   const workflowStages = groupedResult[index];
  //   stageNames.forEach((stage) => {
  //     d.push(getStageCompletionTime(workflowStages, stage));
  //   });

  //   console.log("d = ", d);
  //   rdata.push(d);
  // }

  console.log("rdata = ", rdata);

  stageNames.forEach((stage) => {
    const d = [];
    Object.values(groupedResult).flatMap((item) =>
      item.map((entry) => {
        if (entry.stage_name === stage) d.push(entry.time_waiting_secs);
      })
    );
    rdata.push(d);
  });

  // var chartDom = document.getElementById("chart-container");

  // var myChart = echarts.init(chartDom);
  // updateChartSize(myChart, 1, 0.8);

  var option;

  // There should not be negative values in rawData
  const rawData = rdata;

  const totalData = [];
  for (let i = 0; i < rawData[0].length; ++i) {
    let sum = 0;
    for (let j = 0; j < rawData.length; ++j) {
      sum += rawData[j][i];
    }
    totalData.push(sum);
  }
  const grid = {
    left: 100,
    right: 100,
    top: 50,
    bottom: 50,
  };
  const gridWidth = myChart.getWidth() - grid.left - grid.right;
  const gridHeight = myChart.getHeight() - grid.top - grid.bottom;
  const categoryWidth = gridWidth / rawData[0].length;
  const barWidth = categoryWidth * 0.6;
  const barPadding = (categoryWidth - barWidth) / 2;
  const series = stageNames.map((name, sid) => {
    return {
      name,
      type: "bar",
      stack: "total",
      barWidth: "60%",
      label: {
        show: true,
        formatter: (params) => Math.round(params.value * 1000) / 10 + "%",
      },
      data: rawData[sid].map((d, did) =>
        totalData[did] <= 0 ? 0 : d / totalData[did]
      ),
    };
  });
  const color = ["#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de"];
  const elements = [];
  for (let j = 1, jlen = rawData[0].length; j < jlen; ++j) {
    const leftX = grid.left + categoryWidth * j - barPadding;
    const rightX = leftX + barPadding * 2;
    let leftY = grid.top + gridHeight;
    let rightY = leftY;
    for (let i = 0, len = series.length; i < len; ++i) {
      const points = [];
      const leftBarHeight = (rawData[i][j - 1] / totalData[j - 1]) * gridHeight;
      points.push([leftX, leftY]);
      points.push([leftX, leftY - leftBarHeight]);
      const rightBarHeight = (rawData[i][j] / totalData[j]) * gridHeight;
      points.push([rightX, rightY - rightBarHeight]);
      points.push([rightX, rightY]);
      points.push([leftX, leftY]);
      leftY -= leftBarHeight;
      rightY -= rightBarHeight;
      elements.push({
        type: "polygon",
        shape: {
          points,
        },
        style: {
          fill: color[i],
          opacity: 0.25,
        },
      });
    }
  }
  option = {
    legend: {
      selectedMode: false,
    },
    grid,
    yAxis: {
      type: "value",
    },
    xAxis: {
      type: "category",
      data: keys,
    },
    series,
    graphic: {
      elements,
    },
  };

  // option && myChart.setOption(option);

  return option;
}
