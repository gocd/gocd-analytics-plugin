/*
 * Copyright 2020 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import _ from "lodash";
import moment from "js/lib/moment-humanize-for-gocd.js";

const NOT_APPLICABLE = "N/A";

function VsmUtils() {
	const self = this;

	function doesWorkflowIncludeDestinationPipeline(workflow, destinationPipeline) {
		return workflow.some((pipeline) => pipeline.name === destinationPipeline);
	}

	function doesWorkflowIncludePassingDestinationPipeline(workflow, destinationPipeline) {
		return workflow.some((pipeline) => pipeline.name === destinationPipeline && "Passed" === pipeline.result);
	}

	function findWorkflowsIncludingPipeline(pipelineToCheck, workflows) {
		return _.filter(workflows, (workflow) => doesWorkflowIncludeDestinationPipeline(workflow, pipelineToCheck));
	}

	function groupWorkflowsIntoWorkflowCycles(destinationPipeline, workflows) {
		const allCycles = [];

		let incompleteCycle = {};

		self.workflowIds(workflows).forEach((workflowId) => {
			if (doesWorkflowIncludePassingDestinationPipeline(workflows[workflowId], destinationPipeline)) {
				incompleteCycle[workflowId] = workflows[workflowId];
				allCycles.push(incompleteCycle);
				incompleteCycle = {};
			} else {
				incompleteCycle[workflowId] = workflows[workflowId];
			}
		});

		return allCycles;
	}

	function findMatchingPipeline(workflow, pipelineName) {
		return workflow.filter((pipeline) => {
			return pipeline.name === pipelineName;
		});
	}

  function toDate(dateString) {
    return moment(dateString).toDate();
  }

	function timeTakenByACycle(workflowCycle, sourcePipeline, destinationPipeline) {
		const workflowIds = self.workflowIds(workflowCycle);

		const firstWorkflowStartTime = findMatchingPipeline(workflowCycle[workflowIds[0]], sourcePipeline).shift().scheduled_at;
		const lastWorkflowCompletedTime = findMatchingPipeline(workflowCycle[workflowIds[workflowIds.length - 1]], destinationPipeline).pop().last_transition_time;

		return moment.duration(toDate(lastWorkflowCompletedTime) - toDate(firstWorkflowStartTime), "ms");
	}

  self.workflowIds = function (workflows) {
    return Object.keys(workflows).sort(function (a, b) {
      return parseInt(a) - parseInt(b);
    });
  };

	self.calculateThroughput = function (pipelines, workflows) {
		if (_.isEmpty(workflows)) {
			return 0;
		}

		const destinationPipeline = pipelines[pipelines.length - 1];
		const workflowsReachingDestinationPipeline = findWorkflowsIncludingPipeline(destinationPipeline, workflows);

		const totalWorkflowCount = Object.keys(workflows).length;
		const completingWorkflowCount = workflowsReachingDestinationPipeline.length;
		return Math.round((completingWorkflowCount / totalWorkflowCount) * 100);
	};

	self.calculateAverageCycleTime = function (pipelines, workflows) {
		if (_.isEmpty(workflows)) {
			return NOT_APPLICABLE;
		}

		const sourcePipeline = pipelines[0];
		const destinationPipeline = pipelines[pipelines.length - 1];
		const allCycles = groupWorkflowsIntoWorkflowCycles(destinationPipeline, workflows);

		if (0 === allCycles.length) {
			return NOT_APPLICABLE;
		}

		const totalTimeTakenByAllWorkflowCycles = allCycles.reduce((totalTime, cycle) => {
			return timeTakenByACycle(cycle, sourcePipeline, destinationPipeline) + totalTime;
		}, moment.duration(0));

		return moment.duration(totalTimeTakenByAllWorkflowCycles / allCycles.length, "ms").humanizeForGoCD();
	};

	self.workflowDuration = function (workflow) {
		if (0 === workflow.length) {
			return moment.duration(toDate(workflow[0].last_transition_time) - toDate(workflow[0].scheduled_at), "ms");
		}

		return moment.duration(toDate(workflow[workflow.length - 1].last_transition_time) - toDate(workflow[0].scheduled_at), "ms");
	};
}

export default new VsmUtils();
