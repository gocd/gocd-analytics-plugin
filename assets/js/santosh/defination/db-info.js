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

/**
 * @class
 * @interface {ChartInterface}
 */
class DBInfoSummary {
    data = null;

    formatBytes(bytes, decimals = 2) {

        console.log('received bytes: ', bytes);

        if (bytes === 0) return '0 Bytes';

        const k = 1024;
        const sizes = ['Bytes', 'KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));

        // if bytes are less than 1 KB, return in bytes
        if (i === 0) return bytes + ' ' + sizes[i];

        return (bytes / (k ** i)).toFixed(decimals) + ' ' + sizes[i];
    }

    draw(data) {
        console.log("draw with data ", data);

        this.data = data;

        const info = this.prepareData(this.data);

        console.log("info is ", info);

        // const option = GET_STACKED_AREA_TEMPLATE(info.categories, info.xData, info.series);
        // option.title.text = 'Pipelines with the Highest Wait Time';

        // option.tooltip.formatter = this.tooltipFormatter();

        const option = {
            title: {
                text: "DB Info Summary " + this.formatBytes(data.reduce((acc, item) => acc + parseInt(item.total_size), 0)),
            },
            tooltip: {
                trigger: 'item'
            },
            legend: {
                top: '5%',
                left: 'center'
            },
            series: [
                {
                    name: 'DB Info Summary',
                    type: 'pie',
                    bottom: '10%',
                    radius: ['40%', '70%'],
                    center: ['50%', '70%'],
                    // adjust the start and end angle
                    startAngle: 180,
                    endAngle: 360,
                    data: info
                }
            ]
        };

        option.tooltip = {
            valueFormatter: (value) => {
                return this.formatBytes(value);
            }
        }

        return option;
    }

    prepareData(data) {
        const ret = [];

        data.forEach((d) => {
            ret.push({'value': d.total_size, 'name': d.full_table_name});
        });

        return ret;
    }

    get_requestParamsPoint(index) {
        return null;
    }

    getNextGraphName() {
        return null;
    }
}

export default DBInfoSummary;
