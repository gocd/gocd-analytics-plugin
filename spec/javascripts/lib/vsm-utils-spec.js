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

import test from "tape-plus";
import VsmUtils from "js/lib/vsm-utils";

test("VsmUtils.workflowDuration() should return total time taken by a workflow for id 1", (t) => {
	t.is(VsmUtils.workflowDuration(workflows[1]).humanizeForGoCD(), "22m");
});

test("VsmUtils.workflowDuration() should return total time taken by a workflow for id 2", (t) => {
	t.is(VsmUtils.workflowDuration(workflows[2]).humanizeForGoCD(), "1h 6m");
});

test("VsmUtils.calculateAverageCycleTime() should calculate average cycle time", (t) => {
	t.is(VsmUtils.calculateAverageCycleTime(["plugins", "installers"], workflows), "44m");
});

test("VsmUtils.calculateThroughput() should calculate throughput", (t) => {
	t.is(VsmUtils.calculateThroughput(["plugins", "installers"], workflows), 75);
});

const workflows = {
	1: [
		{
			counter: 1749,
			last_transition_time: "2018-07-03T05:10:33.987+0000",
			name: "plugins",
			result: "Passed",
			scheduled_at: "2018-07-03T05:08:26.386+0000",
			time_waiting_secs: 8,
			total_time_secs: 127,
			workflow_id: 1
		},
		{
			counter: 977,
			last_transition_time: "2018-07-03T05:30:18.274+0000",
			name: "installers",
			result: "Passed",
			scheduled_at: "2018-07-03T05:10:36.467+0000",
			time_waiting_secs: 7,
			total_time_secs: 1181,
			workflow_id: 1
		}
	],
	2: [
		{
			counter: 1749,
			last_transition_time: "2018-07-03T05:10:33.987+0000",
			name: "plugins",
			result: "Passed",
			scheduled_at: "2018-07-03T05:08:26.386+0000",
			time_waiting_secs: 8,
			total_time_secs: 127,
			workflow_id: 5
		},
		{
			counter: 1750,
			last_transition_time: "2018-07-03T06:01:34.984+0000",
			name: "plugins",
			result: "Passed",
			scheduled_at: "2018-07-03T05:59:56.570+0000",
			time_waiting_secs: 11,
			total_time_secs: 98,
			workflow_id: 5
		},
		{
			counter: 977,
			last_transition_time: "2018-07-03T05:30:18.274+0000",
			name: "installers",
			result: "Passed",
			scheduled_at: "2018-07-03T05:10:36.467+0000",
			time_waiting_secs: 7,
			total_time_secs: 1181,
			workflow_id: 5
		},
		{
			counter: 978,
			last_transition_time: "2018-07-03T06:14:47.546+0000",
			name: "installers",
			result: "Passed",
			scheduled_at: "2018-07-03T06:01:36.683+0000",
			time_waiting_secs: 41,
			total_time_secs: 790,
			workflow_id: 5
		}
	],
	3: [
		{
			counter: 1751,
			last_transition_time: "2018-07-03T05:10:33.987+0000",
			name: "plugins",
			result: "Passed",
			scheduled_at: "2018-07-03T05:08:26.386+0000",
			time_waiting_secs: 8,
			total_time_secs: 127,
			workflow_id: 1
		},
		{
			counter: 980,
			last_transition_time: "2018-07-03T05:30:18.274+0000",
			name: "installers",
			result: "Failed",
			scheduled_at: "2018-07-03T05:10:36.467+0000",
			time_waiting_secs: 7,
			total_time_secs: 1181,
			workflow_id: 1
		}
	],
	4: [
		{
			counter: 1749,
			last_transition_time: "2018-07-03T05:10:33.987+0000",
			name: "plugins",
			result: "Failed",
			scheduled_at: "2018-07-03T05:08:26.386+0000",
			time_waiting_secs: 8,
			total_time_secs: 127,
			workflow_id: 1
		}
	],
};
