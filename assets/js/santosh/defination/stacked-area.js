import {secondsToHms, timestampToWords} from "../utils";

function GET_STACKED_AREA_TEMPLATE(legends, xData, series) {
    var option;

    option = {
        color: ['#FFFFE0', '#4cabce'],
        title: {
            text: null,
            subtext: null,
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                label: {
                    backgroundColor: '#6a7985'
                }
            }
        },
        legend: {
            // top: 10,
            // right: 30,
            bottom: 1,
            data: legends
        },
        toolbox: {
            feature: {
                dataZoom: {
                    // yAxisIndex: 'none',
                },
                saveAsImage: {},
                dataView: {
                    readOnly: true,
                },
                // restore: {},
            }
        },
        grid: {
            // top: 60,
            // left: '1%',
            // right: '1%',
            // bottom: 1,
            containLabel: true,
        },
        xAxis: [
            {
                type: 'category',
                boundaryGap: false,
                data: xData,
                axisLabel: {
                    formatter: function (value) {
                        return timestampToWords(value);
                    },
                }
            }
        ],
        yAxis: [
            {
                type: 'value',
                axisLabel: {
                    formatter: function (value) {
                        return secondsToHms(value);
                    },
                },
            }
        ],
        // dataZoom: [
        //     {
        //         id: 'dataZoomX',
        //         type: 'slider',
        //         xAxisIndex: [0],
        //         filterMode: 'filter'
        //     },
        //     {
        //         id: 'dataZoomY',
        //         type: 'slider',
        //         yAxisIndex: [0],
        //         filterMode: 'empty'
        //     },
        //     {
        //         type: 'inside',
        //         xAxisIndex: [0],
        //         start: 1,
        //         end: 35
        //     },
        //     {
        //         type: 'inside',
        //         yAxisIndex: [0],
        //         start: 29,
        //         end: 36
        //     }
        // ],
        series: series
    };

    return option;
}

export default GET_STACKED_AREA_TEMPLATE;