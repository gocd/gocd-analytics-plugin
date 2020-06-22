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

package com.thoughtworks.gocd.analytics.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ViewUtils {
    private ViewUtils() {
    }

    /**
     * URI encodes a string in the same way that encodeURIComponent() does
     * in the browser so we can send query parameters through the viewPath.
     *
     * @param subject - the String to encode
     * @return a URI-encoded String
     */
    public static String jsUriEncode(String subject) {
        try {
            return URLEncoder.encode(subject, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException ignored) {
            return null;
        }
    }
}
