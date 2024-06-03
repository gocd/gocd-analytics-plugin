import * as echarts from "echarts";
import {
    getDateFromTimestampString,
    getTimeFromTimestampString,
    secondsToHms,
    timestampToWords,
    updateChartSize
} from "../utils";
import {sendLinkRequest} from "../../lib/gocd-link-support";

/**
 * @class
 * @interface {ChartInterface}
 */
class JobPriorityDetailsCompare {
    data = null;
    chartWidth = null;
    chartHeight = null;

    constructor(settings, width, height) {
        this.settings = settings;

        this.chartWidth = width;
        this.chartHeight = height;


        console.log('job-priority-details-compare constructor settings, chartWidth, chartHeight', settings, this.chartWidth, this.chartHeight);
    }

    draw(data) {
        console.log("JobPriorityDetailsCompare draw with data ", data);

        this.data = data;

        const {keys, grid, series, elements} = this.prepareDate(this.data, this.chartWidth, this.chartHeight);


        var option;
        option = {
            legend: {
                selectedMode: false
            },
            grid,
            yAxis: {
                type: 'value'
            },
            xAxis: {
                type: 'category',
                data: keys
            },
            series,
            graphic: {
                elements
            }
        };

        option.tooltip = {
            axisPointer: {
                type: "shadow"
            },
            formatter: (param) => {
                const d = this.data[param.dataIndex];

                let ret = param.name + ' (' + timestampToWords(d.scheduled_at) + ')' + '<br>';
                ret += param.seriesName + ' ';
                ret += '<b>';
                ret += param.seriesName === 'Waiting time' ? secondsToHms(d.time_waiting_secs) : secondsToHms(d.time_building_secs);
                ret += '</b>';
                ret += '<hr>🤖 ' + d.agent_uuid;

                return ret;
            },
        }

        return option;
    }

    prepareDate(data, chartWidth, chartHeight) {
        const keys = [];
        // data.map(d => getTimeFromTimestampString(d.scheduled_at));

        const rawData = [[],[]];

        data.forEach((d) => {
            keys.push(getTimeFromTimestampString(d.scheduled_at));
            rawData[0].push(d.time_waiting_secs);
            rawData[1].push(d.time_building_secs);
        });

        console.log('rawData', rawData);

        const totalData = [];
        for (let i = 0; i < rawData[0].length; ++i) {
            let sum = 0;
            for (let j = 0; j < rawData.length; ++j) {
                sum += rawData[j][i];
            }
            totalData.push(sum);
        }
        const grid = {
            left: 100,
            right: 100,
            top: 50,
            bottom: 50
        };
        const gridWidth = chartWidth - grid.left - grid.right;
        const gridHeight = chartHeight - grid.top - grid.bottom;
        const categoryWidth = gridWidth / rawData[0].length;
        const barWidth = categoryWidth * 0.6;
        const barPadding = (categoryWidth - barWidth) / 2;
        const series = [
            'Waiting time',
            'Building time'
        ].map((name, sid) => {
            return {
                name,
                type: 'bar',
                stack: 'total',
                barWidth: '60%',
                label: {
                    show: true,
                    formatter: (params) => Math.round(params.value * 1000) / 10 + '%'
                },
                data: rawData[sid].map((d, did) =>
                    totalData[did] <= 0 ? 0 : d / totalData[did]
                )
            };
        });
        const color = ['#5470c6', '#91cc75']//, '#fac858', '#ee6666', '#73c0de'];
        const elements = [];
        for (let j = 1, jlen = rawData[0].length; j < jlen; ++j) {
            const leftX = grid.left + categoryWidth * j - barPadding;
            const rightX = leftX + barPadding * 2;
            let leftY = grid.top + gridHeight;
            let rightY = leftY;
            for (let i = 0, len = series.length; i < len; ++i) {
                const points = [];
                const leftBarHeight = (rawData[i][j - 1] / totalData[j - 1]) * gridHeight;
                points.push([leftX, leftY]);
                points.push([leftX, leftY - leftBarHeight]);
                const rightBarHeight = (rawData[i][j] / totalData[j]) * gridHeight;
                points.push([rightX, rightY - rightBarHeight]);
                points.push([rightX, rightY]);
                points.push([leftX, leftY]);
                leftY -= leftBarHeight;
                rightY -= rightBarHeight;
                elements.push({
                    type: 'polygon',
                    shape: {
                        points
                    },
                    style: {
                        fill: color[i],
                        opacity: 0.25
                    }
                });
            }
        }

        return {keys: keys, grid: grid, series: series, elements: elements}
    }


    breadcrumbCaption() {
        return "Job Details Compare";
    }

    get_requestParamsPoint(index) {
        return null;
    }

    getNextGraphName() {
        return null;
    }

    getSeriesIndex() {
        return 3;
    }

    nativeClickHandler(transport, params) {
        console.log('nativeClickHandler params', params);

        const d = this.data[params.dataIndex];

        const linkParam = {
            link_to: 'job_details_page',
            pipeline_name: d.pipeline_name,
            pipeline_counter: d.pipeline_counter,
            stage_name: d.stage_name,
            stage_counter: d.stage_counter,
            job_name: d.job_name
        };

        console.log('linkParam = ', linkParam);

        sendLinkRequest(transport, linkParam);
    }
}

export default JobPriorityDetailsCompare;
