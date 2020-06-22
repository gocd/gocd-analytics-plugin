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

const LINK_TO                    = "link_to";
const JOB_DETAILS_LINK_KEY       = "job_details_page";
const STAGE_DETAILS_LINK_KEY     = "stage_details_page";
const PIPELINE_INSTANCE_LINK_KEY = "pipeline_instance_page";
const VSM_LINK_KEY               = "vsm_page";

const JOB_NAME_KEY         = "job_name";
const STAGE_NAME_KEY       = "stage_name";
const PIPELINE_NAME_KEY    = "pipeline_name";
const STAGE_COUNTER_KEY    = "stage_counter";
const PIPELINE_COUNTER_KEY = "pipeline_counter";

function sendLinkRequest(transport, params) {
  // at this moment, success => no-op; only handle failure
  transport.request("link-to", params).fail((err) => {
    // as of now, we print all the errors on console
    // TODO: need to think of a better way to deal with it
    console.error(err); // eslint-disable-line no-console
  });
}

const linkTo = {
  jobDetailsPage: (config, transport) => {
    _.each(config.series, (s) => {
      _.set(s, "point.events.click", (evt) => {
        const params = _.pick(evt.point, [
          PIPELINE_NAME_KEY,
          PIPELINE_COUNTER_KEY,
          STAGE_NAME_KEY,
          STAGE_COUNTER_KEY,
          JOB_NAME_KEY
        ]);

        params[LINK_TO] = JOB_DETAILS_LINK_KEY;
        sendLinkRequest(transport, params);
      });
    });
  },

  stageDetailsPage: (config, transport) => {
    _.each(config.series, (s) => {
      _.set(s, "point.events.click", (evt) => {
        const params = _.pick(evt.point.stage, [
          PIPELINE_NAME_KEY,
          PIPELINE_COUNTER_KEY,
          STAGE_NAME_KEY,
          STAGE_COUNTER_KEY
        ]);

        params[LINK_TO] = STAGE_DETAILS_LINK_KEY;
        sendLinkRequest(transport, params);
      });
    });
  },

  pipelineInstance: (config, transport) => {
    _.each(config.series, (s) => {
      _.set(s, "point.events.click", (evt) => {
        const params    = _.pick(evt.point, [PIPELINE_NAME_KEY, PIPELINE_COUNTER_KEY]);
        params[LINK_TO] = PIPELINE_INSTANCE_LINK_KEY;
        sendLinkRequest(transport, params);
      });
    });
  },

  vsmPage: (config, transport) => {
    _.each(config.series, (s) => {
      _.set(s, "point.events.click", (evt) => {
        const params = _.pick(evt.point, [PIPELINE_NAME_KEY, PIPELINE_COUNTER_KEY]);
        params[LINK_TO] = VSM_LINK_KEY;
        sendLinkRequest(transport, params);
      });
    });
  }
};

/** Public API to add link behavior to any chart. */
function GoCDLinkSupport() {
  this.linkToJobDetailsPage = (config, transport) => {
    linkTo.jobDetailsPage(config, transport);
  };

  this.linkToStageDetailsPage = (config, transport) => {
    linkTo.stageDetailsPage(config, transport);
  };

  this.linkToPipelineInstance = (config, transport) => {
    linkTo.pipelineInstance(config, transport);
  };

  this.linkToVSMPage = (config, transport) => {
    linkTo.vsmPage(config, transport);
  };
}

export default new GoCDLinkSupport();
