import {getAreaSeries, getBarSeries} from "../template";
import GET_STACKED_AREA_TEMPLATE from "./stacked-area";

/**
 * @class
 * @interface {ChartInterface}
 */
class PipelineInstances {

    data = null;

    draw(data) {

        this.data = data;

        const info = this.prepareData(this.data.instances);

        const option = GET_STACKED_AREA_TEMPLATE(info.categories, info.xData, info.series);
        // option.title.text = 'Pipelines with the Highest Wait Time';

        // option.tooltip.formatter = this.tooltipFormatter();

        return option;
    }

    prepareData(data) {
        const legend = ['Waiting Time', 'Building Time'];
        const xData = [];
        const data1 = [];
        const data2 = [];
        const colorData = [];

        data.forEach(m => {
            // xData.push(timestampToWords(m.scheduled_at));
            xData.push(m.scheduled_at);
            data1.push(m.time_waiting_secs);
            data2.push(m.total_time_secs);
            colorData.push(m.result);
        });

        function getSeries() {
            const wt = getAreaSeries("Waiting Time", data1);
            const bt = getAreaSeries("Building Time", data2, colorData);
            return [wt, bt]
        }

        return {
            legends: legend,
            xData: xData,
            series: getSeries()
        }
    }

    get_requestParamsPoint(index) {
        return null;
    }

    getNextGraphName() {
        return null;
    }
}

export default PipelineInstances;