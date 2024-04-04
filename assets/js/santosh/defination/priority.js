import * as echarts from "echarts";
import {updateChartSize} from "../utils";

import {
    getAreaSeries,
    getBarSeries,
    getLineSeries,
    getPlainBarSeries,
} from "../template";
import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";

/**
 * @class
 * @interface {ChartInterface}
 */
class Priority {
    data = null;

    draw(data) {
        console.log("draw with data ", data);

        this.data = data;

        const info = this.prepareData(this.data);

        console.log("info is ", info);

        // option.tooltip.formatter = this.tooltipFormatter();

        // const option = GET_STACKED_BAR_TEMPLATE(info.categories, info.series);
        const option = {
            title: {
                text: 'Pipeline overall',
            },
            xAxis: {
                type: 'category',
                data: ['Passed', 'Failed', 'Canceled']
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    data: [data.passcount, data.failcount, data.cancelcount],
                    type: 'bar'
                }
            ]
        };
        option.series[0].itemStyle = {
            color: function (params) {
                switch (params.name) {
                    case 'Passed':
                        return 'green';
                    case 'Failed':
                        return 'red';
                    case 'Canceled':
                        return 'yellow';
                }
            }
        };

        return option;
    }

    prepareData(data) {

        console.log('🙋 priority')

        const categories = ['Pass count', 'Fail count', 'Cancel count'];

        return {
            categories: categories,
            series: [getBarSeries('', [data.passcount, data.failcount, data.cancelcount])],
        }
    }

    get_requestParamsPoint(index) {
        let result;
        switch (index) {
            case 0:
                result = 'Passed';
                break;
            case 1:
                result = 'Failed';
                break;
            case 2:
                result = 'Cancelled';
                break;
        }
        return {
            "result": result
        };
    }

    getNextGraphName() {
        return "PipelinePriority";
    }

    getSeriesIndex() {
        return 0;
    }

    breadcrumbCaption() {
        return "All priorities";
    }
}

export default Priority;
