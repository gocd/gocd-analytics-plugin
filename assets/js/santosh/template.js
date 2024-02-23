const BAR_SERIES = {
    name: 'No name give',
    type: 'bar',
    stack: 'total',
    label: {
        // show: true
    },
    emphasis: {
        focus: 'series'
    },
    data: undefined
};

const AREA_SERIES = {
    name: '',
    type: 'line',
    stack: 'Total',
    areaStyle: {},
    emphasis: {
        focus: 'series',
    },
    data: undefined,
}

const TOOLTIP = {
    trigger: 'axis',
    axisPointer: {
        // Use axis to trigger tooltip
        type: 'shadow' // 'shadow' as default; can also be 'line' or 'shadow'
    },
    formatter: undefined,
}


function getBarSeries(name, data) {
    let s = structuredClone(BAR_SERIES);
    s.name = name;
    s.data = data;
    return s;
}

function getAreaSeries(name, data, colorData = undefined) {
    let s = structuredClone(AREA_SERIES);
    s.name = name;
    s.data = data;
    console.log('s.data = ', s.data);
    if (colorData === undefined) {
        s.showSymbol = false;
    } else {
        s.itemStyle = {
            normal: {
                color: function (params) {
                    return colorData[params.dataIndex] === "Passed" ? "green" : "red"
                }
            }
        };
    }
    return s;
}

function getTooltipWithFormatter(formatter) {
    let t = structuredClone(TOOLTIP);
    t.formatter = formatter;
    return t;
}

export {getBarSeries, getAreaSeries, getTooltipWithFormatter};