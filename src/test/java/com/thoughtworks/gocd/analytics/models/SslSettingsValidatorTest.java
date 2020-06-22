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

package com.thoughtworks.gocd.analytics.models;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.gocd.analytics.utils.Util.GSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SslSettingsValidatorTest {
    @Test
    public void shouldNotGetErrMsgIfUsingSSLAndAllSSLFieldsAreNotBlank() throws Exception {
        List<Map<String, String>> results = getValidationResults("true", "1", "1", "1", "1", "1");
        assertThat(results.isEmpty(), is(true));
    }

    @Test
    public void shouldNotGetErrMsgIfUsingSSLAndSSLModeIsNull() throws Exception {
        List<Map<String, String>> results = getValidationResults("true", null, "1", "1", "1", "1");
        assertThat(results.isEmpty(), is(true));
    }

    @Test
    public void shouldNotGetErrMsgIfUsingSSLAndSSLModeIsBlank() throws Exception {
        List<Map<String, String>> results = getValidationResults("true", "", "1", "1", "1", "1");
        assertThat(results.isEmpty(), is(true));
    }

    @Test
    public void shouldGetErrMsgIfUsingSSLAndAnyFieldIsBlank() throws Exception {
        List<Map<String, String>> results = getValidationResults("true", "", "", "", "", "");

        assertThat(results.size(), is(4));
        assertThat(results.get(0).get("message"), is("Root Certificate must be provided if SSL is enabled."));
        assertThat(results.get(0).get("key"), is("root_cert"));
        assertThat(results.get(1).get("message"), is("Client Certificate must be provided if SSL is enabled."));
        assertThat(results.get(1).get("key"), is("client_cert"));
        assertThat(results.get(2).get("message"), is("Client Key must be provided if SSL is enabled."));
        assertThat(results.get(2).get("key"), is("client_key"));
        assertThat(results.get(3).get("message"), is("PKCS8 Client Key must be provided if SSL is enabled."));
        assertThat(results.get(3).get("key"), is("client_pkcs8_key"));
    }

    @Test
    public void shouldNotGetErrMsgIfUseSSLIsNullAndAFieldIsBlank() {
        List<Map<String, String>> results = getValidationResults(null, null, null, null, null, null);
        assertThat(results.isEmpty(), is(true));
    }

    @Test
    public void shouldNotGetErrMsgIfUseSSLIsFalseAndAFieldIsBlank() {
        List<Map<String, String>> results = getValidationResults("false", null, null, null, null, null);
        assertThat(results.isEmpty(), is(true));
    }

    private List<Map<String, String>> getValidationResults(String use_ssl,
                                                           String ssl_mode,
                                                           String root_cert,
                                                           String client_cert,
                                                           String client_key,
                                                           String client_pkcs8_key) {
        HashMap<String, Map<String, String>> map = new HashMap<>();
        map.put("use_ssl", Collections.singletonMap("value", use_ssl));
        map.put("ssl_mode", Collections.singletonMap("value", ssl_mode));
        map.put("root_cert", Collections.singletonMap("value", root_cert));
        map.put("client_cert", Collections.singletonMap("value", client_cert));
        map.put("client_key", Collections.singletonMap("value", client_key));
        map.put("client_pkcs8_key", Collections.singletonMap("value", client_pkcs8_key));
        String json = GSON.toJson(Collections.singletonMap("plugin-settings", map));

        return new SslSettingsValidator().validate(ValidatePluginSettings.fromJSON(json));
    }
}
