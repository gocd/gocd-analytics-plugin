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

package com.thoughtworks.gocd.analytics.pluginhealth;

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PluginHealthMessageNotifierTest {

    private GoApplicationAccessor accessor;

    @BeforeEach
    public void setUp() throws Exception {
        accessor = mock(GoApplicationAccessor.class);
    }

    @Test
    public void notify_shouldSendAServerHealthUpdateMessage() throws Exception {
        ArgumentCaptor<GoApiRequest> captor = ArgumentCaptor.forClass(GoApiRequest.class);
        PluginHealthMessageNotifier notifier = new PluginHealthMessageNotifier(accessor);
        final PluginHealthMessage error = PluginHealthMessage.error("error message");

        when(accessor.submit(captor.capture())).thenReturn(mock(GoApiResponse.class));

        notifier.notify(Collections.singletonList(error));

        GoApiRequest goApiRequest = captor.getValue();

        assertEquals("1.0", goApiRequest.apiVersion());
        assertEquals("go.processor.server-health.add-messages", goApiRequest.api());
        assertEquals("analytics", goApiRequest.pluginIdentifier().getExtension());
        assertEquals(Collections.singletonList("1.0"), goApiRequest.pluginIdentifier().getSupportedExtensionVersions());
        assertEquals("[{\"type\":\"error\",\"message\":\"error message\"}]", goApiRequest.requestBody());
    }

    @Test
    public void notify_shouldSendEmptyServerHealthMessageInAbsenceOfMessages() throws Exception {
        ArgumentCaptor<GoApiRequest> captor = ArgumentCaptor.forClass(GoApiRequest.class);
        PluginHealthMessageNotifier notifier = new PluginHealthMessageNotifier(accessor);

        when(accessor.submit(captor.capture())).thenReturn(mock(GoApiResponse.class));

        notifier.notify(Collections.emptyList());

        GoApiRequest goApiRequest = captor.getValue();

        assertEquals("[]", goApiRequest.requestBody());
    }
}
