import * as echarts from "echarts";
import GET_STACKED_AREA_TEMPLATE from "../../santosh/defination/stacked-area";
import {getAreaSeries} from "../../santosh/template";
import moment from "../../lib/moment-humanize-for-gocd";
import {sendLinkRequest} from "../../lib/gocd-link-support";

function getTimeStampsAndTotalTimeSecs(data) {

    console.log('passed data is ', data);

    // const result = Object.values(data).flatMap(items => items.map(i => ({timeStamps: i.last_transition_time, totalTimeSecs: i.total_time_secs})));

    // return {timeStamps: data.map(d => d.last_transition_time), totalTimeSecs: data.map(d => d.total_time_secs)};
    let i = 1;
    return {timeStamps: i++, totalTimeSecs: data.map(d => d.total_time_secs)};
}

function updateChartSize(chart, parentTR, w, h) {

    const tableDOM = document.getElementsByClassName("vsm-trend-table");

    console.log('parentTR.offsetWidth, parentTR.offsetHeight', parentTR.offsetWidth, parentTR.offsetHeight);

    var dynamicWidth = parentTR.offsetWidth * 0.9; // Adjust the multiplier as needed
    var dynamicHeight = parentTR.offsetHeight * 0.9; // Adjust the multiplier as needed

    console.log('updating chart size with w, h = ', dynamicWidth, dynamicHeight);

    chart.resize({
        width: dynamicWidth, height: dynamicHeight
    });
}

function prepareColorData(data) {
    let colorData = [];
    data.forEach(m => {
        colorData.push(m.result);
    });

    return colorData;
}

function getTooltip(name, data, marker) {
    const pipeline = data.find(d => d.name === name);

    // console.log(pipeline);

    const started_at = moment(pipeline.scheduled_at).format("DD MMM YYYY [at] HH:mm:ss [Local Time]");
    const completed_at = moment(pipeline.last_transition_time).format("DD MMM YYYY [at] HH:mm:ss [Local Time]");
    const duration = moment.duration(pipeline.total_time_secs, "seconds").humanizeForGoCD();

    return `
    <b>Pipeline</b>: ${marker} ${pipeline.name}
    <br>
    <b>Pipeline Instance</b>: ${pipeline.counter}
    <br>
    <b>Started At</b>: ${started_at}
    <br>
    <b>Completed At</b>: ${completed_at}
    <br>
    <b>Duration</b>: ${duration}
    <hr>
    <i>Click on data point to navigate to VSM</i>
    `;
}

function getVSMLinkParam(name, data) {
    console.log('getVSMLink name, data = ', name, data);

    const pipeline = data.find(d => d.name === name);

    console.log('got pipeline as ', pipeline);

    return {link_to: "vsm_page", pipeline_counter: pipeline.counter, pipeline_name: name}
}

function renderChart(pipelines, data, workflowId, transport) {

    console.log('data provided: ', data);

    const info = getTimeStampsAndTotalTimeSecs(data);

    const workFlowElement = 'workflow-id-' + workflowId;
    console.log('workflow = ', workFlowElement);

    console.log('info.timeStamps = ', info.timeStamps);
    console.log('info.totalTimeSecs = ', info.totalTimeSecs);

    var chartDom = document.getElementById(workFlowElement);
    // chartDom.style.width = '461px';
    // chartDom.style.height = '60px';

    const colorData = prepareColorData(data);

    var myChart = echarts.init(chartDom);
    var option;

    option = GET_STACKED_AREA_TEMPLATE();

    delete option.color;
    delete option.title;
    delete option.legend;
    delete option.toolbox;
    delete option.xAxis;
    delete option.yAxis;

    const series = getAreaSeries('', info.totalTimeSecs, colorData);
    series.lineStyle = {
        color: 'black',
        width: 1
    }
    series.symbolSize = 6;

    option.tooltip = {
        trigger: 'axis', // axisPointer: {
        //     type: 'shadow',
        //     label: {
        //         backgroundColor: '#6a7985'
        //     }
        // },
        formatter: function (params) {
            const pipeline_name = params[0].name;
            return getTooltip(pipeline_name, data, params[0].marker);
        },

        position: function (point, params, dom, rect, size) {
            // fixed at top
            return [point[0], '10%'];
        },
    };

    option.grid = {
        splitLine: {
            show: false
        },
        show: false,
        // left: '1%',
        top: '1%',
        // right: '1%',
        bottom: '1%',
    };

    option.xAxis = {
        type: 'category',
        data: pipelines,
        boundaryGap: true,
        axisLabel: {
            show: false
        }, axisTick: {
            show: false
        }, axisLine: {
            show: true,
            lineStyle: {
                width: 2,
                color: 'lightgrey'
            }
        }
    };

    option.yAxis = {
        type: 'value', axisLabel: {
            show: false
        }, axisTick: {
            show: false
        }, axisLine: {
            show: false
        }, splitLine: {
            show: false
        }
    };

    option.series = [series];

    option && myChart.setOption(option);
    myChart.hideLoading();

    myChart.on('click', function (params) {
        console.log('click params', params);
        if (params.componentType === 'series') {
            console.log('series type so opening a new window');
            const linkParam = getVSMLinkParam(params.name, data);
            console.log('linkParam = ', linkParam);
            sendLinkRequest(transport, linkParam);
        } else {
            console.log('click is not series, cannot open new window');
        }
    });

    updateChartSize(myChart, chartDom.parentElement);
}

export default renderChart;