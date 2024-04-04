import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getAreaSeries, getBarSeries} from "../template";
import GET_STACKED_AREA_TEMPLATE from "./stacked-area";
import {timestampToWords} from "../utils";
import {sendLinkRequest} from "../../lib/gocd-link-support";

/**
 * @class
 * @interface {ChartInterface}
 */
class JobBuildTimeOnAgent {

    data = null;

    draw(data) {

        this.data = data.jobs;

        const info = this.prepareData(this.data);

        console.log('info = ', info);

        const option = GET_STACKED_AREA_TEMPLATE(info.categories, info.xData, info.series);
        option.title.text = 'Job Build Time on an Agent';

        // option.tooltip.formatter = this.tooltipFormatter();

        return option;
    }

    prepareData(data) {
        const legend = ['Wait Time', 'Build Time'];
        const xData = [];
        const data1 = [];
        const data2 = [];
        const colorData = [];

        this.data.forEach(m => {
            // xData.push(timestampToWords(m.scheduled_at));
            xData.push(m.scheduled_at);
            data1.push(m.time_waiting_secs);
            data2.push(m.time_building_secs);
            colorData.push(m.result);
        });

        function getSeries() {
            const wt = getAreaSeries("Wait Time", data1);
            const bt = getAreaSeries("Build Time", data2, colorData);
            return [wt, bt]
        }

        return {
            legends: legend,
            xData: xData,
            series: getSeries()
        }
    }


    get_requestParamsPoint(index) {
        return  {
            "job_name": this.data[index].job_name,
            "stage_name": this.data[index].stage_name,
            "pipeline_name": this.data[index].pipeline_name
        }
    }

    getNextGraphName() {
        return null;
    }

    insertBreadcrumb() {
        return true;
    }

    breadcrumbCaption() {
        return this.data[0].pipeline_name + ' >> ' + this.data[0].stage_name + ' >> ' + this.data[0].job_name;
    }

    getSeriesIndex() {
        return 2;
    }

    nativeClickHandler(transport, params) {
        console.log('nativeClickHandler params', params);
        const instance = this.data[params.dataIndex];

        const linkParam = {
            link_to: 'job_details_page',
            pipeline_name: instance.pipeline_name,
            pipeline_counter: instance.pipeline_counter,
            stage_name: instance.stage_name,
            stage_counter: instance.stage_counter,
            job_name: instance.job_name
        };

        console.log('linkParam = ', linkParam);

        sendLinkRequest(transport, linkParam);
    }
}

export default JobBuildTimeOnAgent;