import {secondsToHms, updateChartSize} from "../utils";
import {getTooltipWithFormatter} from "../template";

function GET_STACKED_BAR_TEMPLATE(categories = null, series = null) {

    let option;

    option = {
        title: {
            text: null,
            subtext: null,
        },
        tooltip: getTooltipWithFormatter(null),
        legend: {
            // top: 20,
            // right: 30
            bottom: 1,
        },
        toolbox: {
            feature: {
                saveAsImage: {},
                dataZoom: {
                    // yAxisIndex: 'none',
                },
                dataView: {
                    readOnly: false,
                }
            }
        },
        grid: {
            // left: '3%',
            // right: '4%',
            // bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'value',
            axisLabel: {
                formatter: function (value) {
                    return secondsToHms(value);
                },
            }
        },
        yAxis: {
            type: 'category',
            data: categories
        },
        series: series
    };


    return option;
}

export default GET_STACKED_BAR_TEMPLATE;