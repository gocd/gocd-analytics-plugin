import * as echarts from "echarts";
import {secondsToHms, updateChartSize} from "../utils";

// import { getAreaSeries, getBarSeries } from "../template";
import GET_STACKED_AREA_TEMPLATE from "./stacked-area";
import {
    getAreaSeries,
    getBarSeries,
    getLineSeries,
    getPlainBarSeries,
} from "../template";
import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import momentHumanizeForGocd from "../../lib/moment-humanize-for-gocd";
import TooltipManager from "../TooltipManager";
import {
    getPipelinePriorityTooltipFormatterFunction
} from "../TooltipHelper";

/**
 * @class
 * @interface {ChartInterface}
 */
class PipelinePriority {
    data = null;
    tooltipManager = null;

    constructor(settings) {
        this.settings = settings;

        console.log('pipeline-priority constructor settings', settings);
    }

    draw(data) {
        // console.log("PipelinePriority draw with data ", data);

        this.data = data;

        // const info = this.prepareData(this.data);

        // console.log("info is ", info);

        // option.tooltip.formatter = this.tooltipFormatter();

        const option = {
            title: {
                text: 'Pipeline Priority'
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'shadow'
                },
                // formatter: (params) => {
                //     this.tooltipManager = new TooltipManager();
                //     this.tooltipManager.addTitle(params[0].name);
                //     params.forEach(item => {
                //         let value = null;
                //         if(item.seriesName === 'Frequency'){
                //             value = item.value + 'x';
                //         } else {
                //             value = secondsToHms(item.value);
                //         }
                //         this.tooltipManager.addItem(item.marker, item.seriesName, value);
                //     });
                //     this.tooltipManager.addFooter('Click to see the specific details');
                //     return this.tooltipManager.getStandard();
                // },
                // valueFormatter: (value) => secondsToHms(value)
                formatter: getPipelinePriorityTooltipFormatterFunction('Frequency')
            },
            legend: {right: "5%"},
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: [
                {
                    type: 'category',
                    data: data.map(d => d.name),
                }
            ],
            yAxis: [
                {
                    type: 'value',
                    name: 'Frequency',
                    position: 'left',
                    alignTicks: this.settings ? this.settings.alignTicks : true,
                    axisLine: {
                        show: true,
                        lineStyle: {
                            color: 'blue'
                        }
                    },
                    axisLabel: {
                        formatter: '{value} x'
                    }
                },
                {
                    type: 'value',
                    name: 'Time',
                    position: 'right',
                    alignTicks: this.settings ? this.settings.alignTicks : true,
                    offset: 10,
                    axisLine: {
                        show: true,
                        lineStyle: {
                            color: 'green'
                        }
                    },
                    axisLabel: {
                        formatter: function (value) {
                            return secondsToHms(value);
                        },
                    }
                }
            ],
            series: [
                {
                    name: 'Frequency',
                    type: 'bar',
                    emphasis: {
                        focus: 'series'
                    },
                    data: data.map(d => d.times),
                },
                // {
                //     name: 'Email',
                //     type: 'bar',
                //     stack: 'Ad',
                //     emphasis: {
                //         focus: 'series'
                //     },
                //     data: [120, 132, 101, 134, 90, 230, 210]
                // },
                // {
                //     name: 'Union Ads',
                //     type: 'bar',
                //     stack: 'Ad',
                //     emphasis: {
                //         focus: 'series'
                //     },
                //     data: [220, 182, 191, 234, 290, 330, 310]
                // },
                // {
                //     name: 'Video Ads',
                //     type: 'bar',
                //     stack: 'Ad',
                //     emphasis: {
                //         focus: 'series'
                //     },
                //     data: [150, 232, 201, 154, 190, 330, 410]
                // },
                {
                    name: 'Total time spent',
                    type: 'bar',
                    yAxisIndex: 1,
                    data: data.map(d => d.sum_total_time_secs),
                    emphasis: {
                        focus: 'series'
                    },
                    markLine: {
                        lineStyle: {
                            type: 'dashed',
                            color: '#FFC0CB'
                        },
                        data: [[{type: 'min'}, {type: 'max'}]]
                    }
                },
                {
                    name: 'Time Waiting',
                    type: 'bar',
                    yAxisIndex: 1,
                    barWidth: 5,
                    stack: 'Total time spent',
                    emphasis: {
                        focus: 'series'
                    },
                    data: data.map(d => d.sum_time_waiting_secs)
                },
                {
                    name: 'Time building',
                    type: 'bar',
                    yAxisIndex: 1,
                    stack: 'Total time spent',
                    emphasis: {
                        focus: 'series'
                    },
                    data: data.map(d => (d.sum_total_time_secs - d.sum_time_waiting_secs))
                },
            ]
        };

        return option;
    }

    prepareData(data) {

        // console.log('🙋 priority')

        const categories = ['Pass count', 'Fail count', 'Cancel count'];

        return {
            categories: categories,
            series: [getBarSeries('', [data.passcount, data.failcount, data.cancelcount])],
        }
    }

    breadcrumbCaption() {
        return "Pipeline Priority";
    }

    get_requestParamsPoint(index) {
        console.log('get_requestParamsPoint this.data, index', this.data, index);
        return {pipeline_name: this.data[index].name, result: this.settings.result};
    }

    getNextGraphName() {
        return 'PipelinePriorityDetails';
    }

    getSeriesIndex() {
        return 1;
    }
}

export default PipelinePriority;
