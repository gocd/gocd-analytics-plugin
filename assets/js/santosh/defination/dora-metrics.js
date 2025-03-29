import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries} from "../template";
import {groupBy, secondsToHms, truncateString, uniqBy} from "../utils";
import {getLabelTruncateAwareTooltipFormatterFunction} from "../TooltipHelper";
import utils from "../../lib/utils";

/**
 * @class
 * @interface {ChartInterface}
 */
class DoraMetrics {

    data = null;
    c = null;

    constructor(settings) {
        this.settings = settings;

        console.log('dora metrics settings = ', settings);
    }

    draw(data, c) {
        this.data = data;
        this.c = c;

        const info = this.prepareData(this.data, this.settings);

        const dora_metric_chart = document.getElementById("chart-container");

        const mainHeading = document.createElement("h4");
        mainHeading.textContent = "DORA Metrics for " + this.settings.selectedPipeline;

        const deploymentFrequency = document.createElement("div");

        const deploymentFrequencyHeading = document.createElement("div");
        deploymentFrequencyHeading.classList.add("heading");
        deploymentFrequencyHeading.textContent = "Deployment Frequency";

        const deploymentFrequencyContent = document.createElement("div");
        deploymentFrequencyContent.classList.add("metric");
        deploymentFrequencyContent.innerHTML =
            "<b>Attempts: </b>" + info.deployment_frequency.attempts
            + "<b> Passed: </b>" + info.deployment_frequency.passed
            + "<b> Failed: </b>" + info.deployment_frequency.failed;

        deploymentFrequency.append(deploymentFrequencyHeading);
        deploymentFrequency.append(deploymentFrequencyContent);

        const failureFrequency = document.createElement("div");

        const failureFrequencyHeading = document.createElement("div");
        failureFrequencyHeading.classList.add("heading");
        failureFrequencyHeading.textContent = "Failure Frequency";

        const failureFrequencyContent = document.createElement("div");
        failureFrequencyContent.classList.add("metric");
        failureFrequencyContent.innerHTML =
            "Proportion: " + info.failure_frequency.failure_proportion + "%" + "<br>" +
            "Highest consecutive failure: " + info.failure_frequency.highest_consecutive_failures + "<br>" +
            "Average fails per pass: " + info.failure_frequency.averageFailsPerPass + "<br>" +
            "Agent commonly scheduled: " + info.failure_frequency.common_agent + "<br>" +
            "Total time spent - failure vs pass (" +
            secondsToHms(info.failure_frequency.pass_fail_time[1]) + " / " + secondsToHms(info.failure_frequency.pass_fail_time[0]) + ")" + "<br>" +
            "<div class=\"wrapper\">\n" +
            "\t\t\t<div class=\"progress-bar\">\n" +
            "\t\t\t\t<span class=\"progress-bar-fill\" style=\"width: "
            + info.failure_frequency.pass_fail_time[1] / (info.failure_frequency.pass_fail_time[0] + info.failure_frequency.pass_fail_time[1]) * 100
            + "%;\"></span>\n" +
            "\t\t\t</div>\n" +
            "\t\t</div>";

        failureFrequency.append(failureFrequencyHeading);
        failureFrequency.append(failureFrequencyContent);

        const mtr = document.createElement("div");

        const mtrHeading = document.createElement("div");
        mtrHeading.classList.add("heading");
        mtrHeading.textContent = "Mean time to recovery";

        const mtrContent = document.createElement("div");
        mtrContent.classList.add("metric");
        mtrContent.innerHTML =
            "Needed to recover: " + info.mean_time_to_recovery.needed_to_pass + " times" + "<br>" +
            "Time taken to recover: " + secondsToHms(info.mean_time_to_recovery.total_time_to_recover) + "<br>" +
            "Human time taken to recover: " + secondsToHms(info.mean_time_to_recovery.total_human_time_to_recover) + "<br>" +
            "Wasted efforts to recovery: " + info.mean_time_to_recovery.wasted_efforts_to_recovery + "<br>" +
            "Machine time taken for wasted efforts to recovery: " + secondsToHms(info.mean_time_to_recovery.machine_time_taken_for_wasted_efforts) + "<br>" +
            "Human time taken for wasted efforts to recovery: " + secondsToHms(info.mean_time_to_recovery.human_time_taken_for_wasted_efforts);

        mtr.append(mtrHeading);
        mtr.append(mtrContent);

        dora_metric_chart.innerHTML = "";
        dora_metric_chart.appendChild(mainHeading);
        dora_metric_chart.appendChild(deploymentFrequency);
        dora_metric_chart.appendChild(failureFrequency);
        dora_metric_chart.appendChild(mtr);

        console.log("dora metric text content added");

        return null;
    }

    getDeploymentFrequency(data) {

        console.log("agent metrics getDeploymentFrequency");

        let attempts = 0;

        let failed = 0;

        let passed = 0;

        data.forEach(item => {
            attempts++;

            if (item.result === 'Passed') {
                passed++;
            } else {
                failed++;
            }
        })

        // return {category: category, data: data};

        return {attempts: attempts, passed: passed, failed: failed};
    }

    findCommonAgents(data) {

        console.log("findCommonAgents data = ", data);

        if (!Array.isArray(data) || data.length === 0) {
            return null;
        }

        const valueCounts = {};
        for (const value of data) {
            valueCounts[value.type] = (valueCounts[value] || 0) + 1;
        }

        console.log("findCommonAgents valueCounts = ", valueCounts);

        let mostCommonValue = null;
        let maxCount = 0;

        for (const value in valueCounts) {
            if (valueCounts[value] > maxCount) {
                mostCommonValue = value;
                maxCount = valueCounts[value];
            }
        }

        console.log("findCommonAgents mostCommonValues = ", mostCommonValue);

        return mostCommonValue;
    }

    getDeploymentFailures(data) {

        console.log("agent metrics getDeploymentFailures data", data);

        // const agentArray = Object.entries(groupedAgents);
        //
        // console.log("agent metrics agentArray = ", agentArray);
        //


        // const sortedAgents = Object.fromEntries(agentArray);

        console.log("getDeploymentFailures finding first passed");

        const passedIndexes = [];
        const failedIndexes = [];


        let flag = false;
        let highest_consecutive_failures = 0;
        let consecutive_failures = 0;
        let failure_agents = [];

        for (let i = 0; i < data.length; ++i) {
            const agent = data[i];

            if (agent.result === 'Passed') {
                // console.log("getDeploymentFailures first passed found. index, data", i, agent);
                passedIndexes.push(i);
                flag = true;
                if (consecutive_failures > highest_consecutive_failures) {
                    highest_consecutive_failures = consecutive_failures;
                }
                consecutive_failures = 0;
                console.log("getDeploymentFailures passed");
            } else if (agent.result === 'Failed') {
                failedIndexes.push(i);
                flag = false;
                consecutive_failures++;
                console.log("getDeploymentFailures adding agent = ", agent.agent_uuid);
                failure_agents.push(agent.agent_uuid);
                console.log("getDeploymentFailures failed");
            }
        }

        // in case where it fails till the end
        if (consecutive_failures !== 0 && highest_consecutive_failures === 0) {
            highest_consecutive_failures = consecutive_failures;
        }

        console.log("getDeploymentFailures passedIndexes, failedIndexes = ", passedIndexes, failedIndexes);

        console.log("getDeploymentFailures calculating failure proportions");

        const failureProportion = (failedIndexes.length / data.length) * 100;

        console.log("getDeploymentFailures failure proportion = ", failureProportion);

        console.log("getDeploymentFailures highest consecutive failure = ", highest_consecutive_failures);

        console.log("getDeploymentFailures calculating most common agents with failure_agents = ", failure_agents);

        const mostCommonAgent = this.findCommonAgents(failure_agents);

        console.log("getDeploymentFailures mostCommonAgent = ", mostCommonAgent);

        // calculate total time for failure

        let total_time_spent_failure = 0;

        for (let i = 0; i < failedIndexes.length; i++) {
            total_time_spent_failure += data[i].duration_secs;
        }

        let total_time_spent_pass = 0;

        for (let i = 0; i < passedIndexes.length; i++) {
            total_time_spent_pass += data[i].duration_secs;
        }

        console.log("getDeploymentFailures total_time_spent_pass = ", total_time_spent_pass);
        console.log("getDeploymentFailures total_time_spent_fail = ", total_time_spent_failure);

        const averageFailsPerPass = failedIndexes.length / passedIndexes.length;
        console.log("getDeploymentFailures average number of fails per pass: ", averageFailsPerPass);


        return {
            failure_proportion: failureProportion,
            highest_consecutive_failures: highest_consecutive_failures,
            common_agent: mostCommonAgent,
            pass_fail_time: [total_time_spent_pass, total_time_spent_failure],
            averageFailsPerPass: averageFailsPerPass
        };
    }

    getMeanTimeToRecovery(data) {

        console.log("getMeanTimeToRecovery data = ", data);

        let ready_to_recover_flag = false;

        let needed_to_pass = 0;

        let total_time_to_recover = 0;

        let total_human_time_to_recover = 0;

        let wasted_efforts_to_recovery = 0;

        let machine_time_taken_for_wasted_efforts = 0;

        let human_time_taken_for_wasted_efforts = 0;

        // find first fail

        for (let i = 0; i < data.length; i++) {
            const agent = data[i];

            console.log("getMeanTimeToRecovery for loop result = ", agent.result);

            if (agent.result === 'Failed') {
                if (ready_to_recover_flag === true) {
                    wasted_efforts_to_recovery += 1;
                    machine_time_taken_for_wasted_efforts += data[i].duration_secs;
                    // calculate human time
                    human_time_taken_for_wasted_efforts += ((new Date(data[i - 1].completed_at) - new Date(agent.completed_at))) / 1000;
                }
                ready_to_recover_flag = true;
                console.log("getMeanTimeToRecovery marking ready_to_recover_flag = true");
            } else if (agent.result === 'Passed') {
                if (ready_to_recover_flag === true) {
                    needed_to_pass += 1;
                    total_time_to_recover += data[i].duration_secs;
                    // calculate human time
                    total_human_time_to_recover += ((new Date(data[i - 1].completed_at) - new Date(agent.completed_at))) / 1000;
                }
                ready_to_recover_flag = false;
                console.log("getMeanTimeToRecovery marking ready_to_recover_flag = false");
            }
        }

        return {
            needed_to_pass: needed_to_pass,
            total_time_to_recover: total_time_to_recover,
            wasted_efforts_to_recovery: wasted_efforts_to_recovery,
            total_human_time_to_recover: total_human_time_to_recover,
            machine_time_taken_for_wasted_efforts: machine_time_taken_for_wasted_efforts,
            human_time_taken_for_wasted_efforts: human_time_taken_for_wasted_efforts
        };
    }

    prepareData(data, settings) {

        console.log("dora metrics prepareData data, settings", data, settings);

        data.forEach(item => {
            item['unique_name'] = item['stage_name'] + '-' + item['job_name'];
        });

        const groupedPipelineCounter = groupBy(data, 'pipeline_counter');

        const deployment_frequency = this.getDeploymentFrequency(data);
        console.log("dora metrics deployment_frequency", deployment_frequency);

        const failure_frequency = this.getDeploymentFailures(data);
        console.log("dora metrics failure_frequency", failure_frequency);

        const mtr = this.getMeanTimeToRecovery(data);
        console.log("dora metrics mtr", mtr);

        return {
            deployment_frequency: deployment_frequency,
            failure_frequency: failure_frequency,
            mean_time_to_recovery: mtr
        };
    }


    get_requestParamsPoint(index) {
        return {
            "agent_uuid": this.data[index].uuid, "agent_host_name": this.data[index].agent_host_name,
        }
    }

    getNextGraphName() {
        return "LongestWaitingJobsOnAgent";
    }

    insertBreadcrumb() {
        return false;
    }

    breadcrumbCaption() {
        return "Agents";
    }

    getSeriesIndex() {
        return 0;
    }
}

export default DoraMetrics;