import * as echarts from "echarts";
import {secondsToHms, timestampToWords} from "./utils";

console.log('this is santosh/stacked-area.js');

const StackedArea = {

    generate: (title, legends, xData, series) => {

        console.log('StackedArea generate() legends, xData, series', legends, xData, series);

        var chartDom = document.getElementById('chart-container');
        var myChart = echarts.init(chartDom, {
            width: '700px',
            height: '300px',
        });
        var option;

        option = {
            color: ['#FFFFE0', '#4cabce'],
            title: {
                text: title.text,
                subtext: title.subtext,
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
                        readOnly: false,
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


        option && myChart.setOption(option);
        myChart.hideLoading();
    }
}

export default StackedArea;