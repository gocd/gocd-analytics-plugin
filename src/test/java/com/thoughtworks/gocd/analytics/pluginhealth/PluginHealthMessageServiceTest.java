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

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class PluginHealthMessageServiceTest {

    private PluginHealthMessageNotifier notifier;

    @Before
    public void setUp() throws Exception {
        notifier = mock(PluginHealthMessageNotifier.class);
    }

    @Test
    public void update_shouldAddPluginHealthMessage() throws Exception {
        final PluginHealthMessageService pluginHealthMessageService = new PluginHealthMessageService(mock(PluginHealthMessageNotifier.class));
        final PluginHealthMessage message = PluginHealthMessage.error("error message");

        pluginHealthMessageService.update(new PluginHealthState(message, PluginHealthScope.forPluginSettings()));

        assertThat(pluginHealthMessageService.all().size(), is(1));
        assertTrue(pluginHealthMessageService.all().contains(message));
    }

    @Test
    public void update_shouldNotifyGoCDWithPluginHealthMessages() throws Exception {
        ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        final PluginHealthMessageService pluginHealthMessageService = new PluginHealthMessageService(notifier);
        final PluginHealthMessage error = PluginHealthMessage.error("error message");

        doNothing().when(notifier).notify(captor.capture());

        pluginHealthMessageService.update(new PluginHealthState(error, PluginHealthScope.forPluginSettings()));

        Collection<PluginHealthMessage> collection = captor.getValue();
        assertThat(collection.size(), is(1));
        assertTrue(collection.contains(error));

        final PluginHealthMessage warn = PluginHealthMessage.warn("warn message");

        doNothing().when(notifier).notify(captor.capture());
        pluginHealthMessageService.update(new PluginHealthState(warn, PluginHealthScope.forPluginSettings()));

        Collection<PluginHealthMessage> collection1 = captor.getValue();
        assertThat(collection1.size(), is(2));
        assertTrue(collection1.contains(error));
        assertTrue(collection1.contains(warn));
    }

    @Test
    public void remove_shouldRemovePluginHealthMessageForCorrespondingPluginHealthState() throws Exception {
        final PluginHealthMessageService pluginHealthMessageService = new PluginHealthMessageService(mock(PluginHealthMessageNotifier.class));
        final PluginHealthMessage message = PluginHealthMessage.error("error message");

        pluginHealthMessageService.update(new PluginHealthState(message, PluginHealthScope.forPluginSettings()));

        assertThat(pluginHealthMessageService.all().size(), is(1));
        assertTrue(pluginHealthMessageService.all().contains(message));

        pluginHealthMessageService.remove(new PluginHealthState(message, PluginHealthScope.forPluginSettings()));

        assertTrue(pluginHealthMessageService.all().isEmpty());
    }

    @Test
    public void removeByScope_shouldRemoveAllPluginHealthMessagesForTheGivenScope() throws Exception {
        final PluginHealthMessageService pluginHealthMessageService = new PluginHealthMessageService(mock(PluginHealthMessageNotifier.class));
        final PluginHealthMessage error = PluginHealthMessage.error("error message");
        final PluginHealthMessage warn = PluginHealthMessage.error("warn message");

        pluginHealthMessageService.update(new PluginHealthState(error, PluginHealthScope.forPluginSettings()));
        pluginHealthMessageService.update(new PluginHealthState(warn, PluginHealthScope.forPluginSettings()));

        assertThat(pluginHealthMessageService.all().size(), is(2));

        pluginHealthMessageService.removeByScope(PluginHealthScope.forPluginSettings());

        assertTrue(pluginHealthMessageService.all().isEmpty());
    }

    @Test
    public void removeByScope_shouldNotifyGoCDWithPluginHealthMessages() throws Exception {
        ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        final PluginHealthMessageService pluginHealthMessageService = new PluginHealthMessageService(notifier);
        final PluginHealthMessage error = PluginHealthMessage.error("error message");
        final PluginHealthMessage warn = PluginHealthMessage.error("warn message");
        PluginHealthMessage agentError = PluginHealthMessage.error("agent error");

        pluginHealthMessageService.update(new PluginHealthState(error, PluginHealthScope.forPluginSettings()));
        pluginHealthMessageService.update(new PluginHealthState(warn, PluginHealthScope.forPluginSettings()));
        pluginHealthMessageService.update(new PluginHealthState(agentError, PluginHealthScope.forAgentStatusNotification()));

        pluginHealthMessageService.removeByScope(PluginHealthScope.forPluginSettings());

        verify(notifier, times(4)).notify(captor.capture());

        Collection<PluginHealthMessage> collection = captor.getValue();
        assertThat(collection.size(), is(1));
        assertTrue(collection.contains(agentError));
    }

    @Test
    public void remove_shouldNotifyGoCDWithPluginHealthMessages() throws Exception {
        ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        final PluginHealthMessageService pluginHealthMessageService = new PluginHealthMessageService(notifier);
        final PluginHealthMessage error = PluginHealthMessage.error("error message");
        final PluginHealthMessage warn = PluginHealthMessage.warn("warn message");

        pluginHealthMessageService.update(new PluginHealthState(error, PluginHealthScope.forPluginSettings()));
        pluginHealthMessageService.update(new PluginHealthState(warn, PluginHealthScope.forPluginSettings()));

        pluginHealthMessageService.remove(new PluginHealthState(warn, PluginHealthScope.forPluginSettings()));

        verify(notifier, times(3)).notify(captor.capture());

        Collection<PluginHealthMessage> collection = captor.getValue();
        assertThat(collection.size(), is(1));
        assertTrue(collection.contains(error));
    }
}
