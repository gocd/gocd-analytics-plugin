import {getUniqueDatesFromArray, groupObjectsByDate, secondsToHms, timestampToWords} from "./utils";
import * as echarts from "echarts";

console.log('santosh/simple-stacked-bar.js loaded');

class SimpleStackedBar {

    constructor(name, meta) {
        this.name = name;
        this.meta = meta;

        this.categories = [];
        this.categoriesWithSameDates = [];
        this.series = [];
        this.data = [];

        this.constructGraphData();
        this.generate();
    }

    getAllTransitionTime() {
        let transition_time = [];
        this.meta.transitions.forEach(t => {
            transition_time.push(t.transition_time);
        });
        return transition_time;
    }

    getStateArray(state = undefined) {
        switch (state) {
            case 'Idle':
            case 'Building':
                break;
            default:
                throw new Error("Unknown state: " + state);
        }

        return this.meta.transitions
            .filter(t => t.build_state === state)
            .map(t => t.transition_time)
    }

    setDataForEachCategories(dateGroup) {
        console.log('function setDataForEachCategories');
        var data = [];

        var groupCount = 0;

        Object.entries(dateGroup).forEach(function ([key, value]) {
            console.log("key = ", key, " value = ", value);
            const states = value;

            const compareTimestamps = (a, b) => {
                return new Date(a.transition_time) - new Date(b.transition_time);
            };

            const getTimePartFromTimestamp = (ts) => {
                return parseInt(new Date(ts).toTimeString().substring(0, 5).replace(':', ''));
            }

            states.sort(compareTimestamps);

            for (let i = 0; i < states.length; i++) {
                const state = states[i];

                console.log('state = ', state);

                const transition_time = new Date(state.transition_time).toString();

                // const timePart = parseInt(transition_time.split("T")[1].split(".")[0].slice(0, 5).replace(":", ""));
                const timePart = getTimePartFromTimestamp(state.transition_time);
                console.log("timePart = ", timePart);

                let duration = 0;
                if (i < states.length - 1) {
                    console.log('states.length = ', states.length);
                    console.log('i = ', i);
                    console.log('i + 1 = ', i + 1);

                    const nextState = states[i + 1];
                    console.log('nextState = ', nextState);
                    // const nextTimePart = parseInt(nextState.transition_time.split("T")[1].split(".")[0].slice(0, 5).replace(":", ""));
                    const nextTimePart = getTimePartFromTimestamp(nextState.transition_time);
                    duration = nextTimePart - timePart;
                }
                if (groupCount > 0 && i === 0) {

                }
                console.log('adding data');
                data.push({
                    name: state.agent_state,
                    value: [groupCount, timePart, (timePart + duration), duration],
                    itemStyle: {
                        normal: {
                            color: getAgentStateColor(state.agent_state),
                        }
                    }
                });
            }
            groupCount++;
        });

        // Object.entries(dateGroup).forEach(function ([key, value], index) {
        //     console.log('category = ', key);
        //
        //     let anchor = 0;
        //     let prevBaseTime = 0;
        //     value.forEach(o => {
        //         let baseTime = +new Date(o.transition_time);
        //         console.log('base time is ', baseTime);
        //
        //         const duration = anchor !== 0 ? baseTime - anchor : 0;
        //         console.log('duration is ', duration)
        //
        //         data.push({
        //             name: o.agent_state,
        //             // value: [index, baseTime, (baseTime += duration), duration],
        //             value: [index, prevBaseTime, baseTime, duration],
        //             itemStyle: {
        //                 normal: {
        //                     color: getAgentStates(o.agent_state),
        //                 },
        //             },
        //         });
        //
        //         if (prevBaseTime !== 0) {
        //             prevBaseTime = baseTime;
        //         }
        //
        //         anchor = baseTime;
        //         console.log('anchor is ', anchor);
        //     });
        //
        // });
        console.log('pushing data into global space ', data);
        this.data.push(...data);
        // this.data.push(data.slice(0, 2));

        console.log('data is ', this.data);

        function getAgentStateColor(state) {
            console.log('🎨 getAgentStateColor(state)', state);
            const states = [
                {'Idle': '#ADD8E6'},
                {'Building': '#228C22'},
                {'Cancelled': '#FF0000'},
                {'Missing': '#F9982C'},
                {'LostContact': '#808080'},
                {'Unknown': '#000000'},
            ];

            const stateObj = states.find(s => state in s);

            console.log('about to return ', stateObj[state]);

            return stateObj ? stateObj[state] : 'black';
        }
    }

    updateChartSize(chart, parentTR, w, h) {

        console.log('parentTR.offsetWidth, parentTR.offsetHeight', parentTR.offsetWidth, parentTR.offsetHeight);

        var dynamicWidth = parentTR.offsetWidth * 0.9; // Adjust the multiplier as needed
        var dynamicHeight = parentTR.offsetHeight * 0.7; // Adjust the multiplier as needed

        console.log('updating chart size with w, h = ', dynamicWidth, dynamicHeight);

        chart.resize({
            width: dynamicWidth, height: dynamicHeight
        });
    }

    constructGraphData() {

        const categories = [];
        const dataIdle = [];
        const dataBuilding = [];
        const dataCancelled = [];
        const dataMissing = [];
        const dataLostContact = [];
        const dataUnknown = [];

        console.log("meta passed is ", this.meta);

        // not required
        const transitionTimes = this.getAllTransitionTime();

        const dateGroup = groupObjectsByDate(this.meta.transitions);

        console.log('dateGroup ', dateGroup);

        const uniqueTransitionTimes = Object.keys(dateGroup);

        categories.push(...uniqueTransitionTimes);

        console.log('categories =', categories);

        // dataIdle.push(this.getStateArray('Idle'));
        // dataBuilding.push(this.getStateArray('Building'));

        // console.log('dataIdle, dateBuilding = ', dataIdle, dataBuilding);

        this.categories = getCategories();

        // this.setCategoriesWithSameDates(this.categories, this.meta.transitions);
        // console.log('categoriesWithSameDates = ', this.categoriesWithSameDates);

        this.setDataForEachCategories(dateGroup);

        console.log('getting series...');
        this.series = getSeries(this.data);

        console.log('series is ', this.series);

        function getCategories() {
            return categories;
        }

        function renderItem(params, api) {
            var categoryIndex = api.value(0);
            var start = api.coord([api.value(1), categoryIndex]);
            var end = api.coord([api.value(2), categoryIndex]);
            var height = api.size([0, 1])[1] * 0.6;
            var rectShape = echarts.graphic.clipRectByRect(
                {
                    x: start[0],
                    y: start[1] - height / 2,
                    width: end[0] - start[0],
                    height: height,
                },
                {
                    x: params.coordSys.x,
                    y: params.coordSys.y,
                    width: params.coordSys.width,
                    height: params.coordSys.height,
                }
            );
            return (
                rectShape && {
                    type: 'rect',
                    transition: ['shape'],
                    shape: rectShape,
                    style: api.style(),
                }
            );
        }

        function getSeries(data) {
            return [
                {
                    type: 'custom',
                    renderItem: renderItem,
                    itemStyle: {
                        opacity: 0.8,
                    },
                    encode: {
                        x: [1, 2],
                        y: 0,
                    },
                    data: data,
                },
            ]
        }
    }

    test(params) {
        console.log(params);

        const duration = params.value[3];

        let hours = Math.floor(duration / 100);
        let minutes = duration % 100;
        if (hours === 24) hours = 0;

        const r = hours * 60 + minutes;

        return params.marker + params.name + ': ' + r + ' m' + '<hr> Total this day: ';
    }

    generate() {

        console.log('generating graph with columns, series = ', this.categories, this.series);

        const chartDom = document.getElementById('chart-container');
        const myChart = echarts.init(chartDom);
        var option;

        option = {
            tooltip: {
                formatter: this.test,
            },
            grid: {
                // height: 100,
                // show: true
            },
            xAxis: {
                type: "value",
                min: 0,
                max: 2400,
                splitNumber: 12,
                scale: true,
                axisLabel: {
                    formatter: function (val) {
                        if (val === 0) {
                            return '00:00';
                        } else if (val.toString().length === 3) {
                            val = '0' + val;
                        }
                        return val.toString().replace(/(\d{2})(\d{2})/, '$1:$2');
                    },
                },
            },
            yAxis: {
                data: this.categories,
                axisLabel: {
                    formatter: function (value) {
                        return timestampToWords(value);
                    },
                }
            },
            series: this.series,
            // toolbox: {
            //     feature: {
            // dataView: {},
            // restore: {},
            // saveAsImage:{}
            // },
            // },
            dataZoom: [
                {
                    type: 'inside',
                    filterMode: 'weakFilter',
                    showDataShadow: false,
                    // top: 290,
                    labelFormatter: '',
                },
                {
                    type: 'inside',
                    filterMode: 'weakFilter',
                },
            ],
        };

        option && myChart.setOption(option, true);

        console.log("returning myChart");

        myChart.hideLoading();

        this.updateChartSize(myChart, chartDom.parentElement);

        return myChart;
    }
}
;

export default SimpleStackedBar;