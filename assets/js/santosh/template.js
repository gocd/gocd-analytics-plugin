const BAR_SERIES = {
  name: "No name give",
  type: "bar",
  stack: "total",
  label: {
    // show: true
  },
  emphasis: {
    focus: "series",
  },
  data: undefined,
};

const PAIN_BAR_SERIES = {
  name: "No name give",
  type: "bar",
  data: undefined,
};

const AREA_SERIES = {
  name: "",
  type: "line",
  stack: "Total",
  areaStyle: {},
  emphasis: {
    focus: "series",
  },
  data: undefined,
};

const LINE_SERIES = {
  name: "",
  type: "line",
};

const TOOLTIP = {
  trigger: "axis",
  axisPointer: {
    // Use axis to trigger tooltip
    type: "shadow", // 'shadow' as default; can also be 'line' or 'shadow'
  },
  formatter: undefined,
};

function getLineSeries(name, data) {
  let s = structuredClone(LINE_SERIES);
  s.name = name;
  s.data = data;
  return s;
}

function getBarSeries(name, data) {
  let s = structuredClone(BAR_SERIES);
  s.name = name;
  s.data = data;
  return s;
}

function getPlainBarSeries(name, data, colorData = undefined) {
  let s = structuredClone(PAIN_BAR_SERIES);
  s.name = name;
  s.data = data;
  console.log("s.data = ", s.data);
  if (colorData === undefined) {
    s.showSymbol = false;
  } else {
    s.itemStyle = {
      normal: {
        color: function (params) {
          const result = colorData[params.dataIndex];

          switch (result) {
            case "Passed":
              return "lime";
            case "Failed":
              return "red";
            case "Cancelled":
              return "#FFC000";
            default:
              return "";
          }
          // return colorData[params.dataIndex] === "Passed" ? "green" : "red"
        },
        borderColor: "#000",
        borderType: "solid",
        borderWidth: 1,
      },
    };
  }
  return s;
}

function getAreaSeries(name, data, colorData = undefined) {
  let s = structuredClone(AREA_SERIES);
  s.name = name;
  s.data = data;
  console.log("s.data = ", s.data);
  if (colorData === undefined) {
    s.showSymbol = false;
  } else {
    s.itemStyle = {
      normal: {
        color: function (params) {
          const result = colorData[params.dataIndex];

          switch (result) {
            case "Passed":
              return "lime";
            case "Failed":
              return "red";
            case "Cancelled":
              return "#FFC000";
            default:
              return "";
          }
          // return colorData[params.dataIndex] === "Passed" ? "green" : "red"
        },
        borderColor: "#000",
        borderType: "solid",
        borderWidth: 1,
      },
    };
  }
  return s;
}

function getTooltipWithFormatter(formatter) {
  let t = structuredClone(TOOLTIP);
  t.formatter = formatter;
  return t;
}

export {
  getLineSeries,
  getBarSeries,
  getPlainBarSeries,
  getAreaSeries,
  getTooltipWithFormatter,
};
