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

package com.thoughtworks.gocd.analytics;

import com.google.gson.Gson;

public class RequestBodyMother {
    private static final Gson gson = new Gson();

//    public static String forSearch(String searchTerm, String searchBase) {
//        Map<String, Object> requestMap = new HashMap<>();
//        requestMap.put(SearchUserRequest.SEARCH_TERM, searchTerm);
//        requestMap.put("auth_configs", Collections.singletonList(AuthConfig.fromJSON(authConfigJson("ldap", searchBase, ""))));
//        return gson.toJson(requestMap);
//    }
//
//    public static String forSearch(String searchTerm) {
//        Map<String, Object> requestMap = new HashMap<>();
//        requestMap.put(SearchUserRequest.SEARCH_TERM, searchTerm);
//        requestMap.put("auth_configs", Collections.singletonList(AuthConfig.fromJSON(authConfigJson("ldap", "ou=pune,ou=system", ""))));
//        return gson.toJson(requestMap);
//    }
//
//    public static String forSearchWithSearchFilter(String searchTerm, String searchFilter) {
//        Map<String, Object> requestMap = new HashMap<>();
//        requestMap.put(SearchUserRequest.SEARCH_TERM, searchTerm);
//        requestMap.put("auth_configs", Collections.singletonList(AuthConfig.fromJSON(authConfigJson("ldap", "ou=pune,ou=system", searchFilter))));
//        return gson.toJson(requestMap);
//    }
//
//    public static String forSearchWithMultipleAuthConfigs(String searchTerm) {
//        Map<String, Object> requestMap = new HashMap<>();
//        requestMap.put(SearchUserRequest.SEARCH_TERM, searchTerm);
//        requestMap.put("auth_configs", Arrays.asList(
//                AuthConfig.fromJSON(authConfigJson("ldap-1", "ou=pune,ou=system", "")),
//                AuthConfig.fromJSON(authConfigJson("ldap-2", "ou=pune,ou=system", ""))
//        ));
//        return gson.toJson(requestMap);
//    }
//
//    public static String forAuthenticate(String username, String password, String searchBase) {
//        Map<String, Object> requestMap = getRequestBodyMap(username, password, "ldap", searchBase);
//        return gson.toJson(requestMap);
//    }
//
//    public static String forAuthorizeWithAttribute(String username, String password, String searchBase, String roleName, String authConfigId, String attributeName, String attributeValue) {
//        Map<String, Object> requestMap = getRequestBodyMap(username, password, authConfigId, searchBase);
//        requestMap.put("role_configs", Collections.singletonList(RoleConfig.fromJSON(roleConfigJson(roleName, authConfigId, attributeName, attributeValue, null, ""))));
//        return gson.toJson(requestMap);
//    }
//
//    public static String forAuthorizeWithGroupMembershipFilter(String username, String password, String searchBase, String roleName, String authConfigId, String groupMembershipFilter) {
//        Map<String, Object> requestMap = getRequestBodyMap(username, password, authConfigId, searchBase);
//        requestMap.put("role_configs", Collections.singletonList(RoleConfig.fromJSON(roleConfigJson(roleName, authConfigId, null, null, groupMembershipFilter, ""))));
//        return gson.toJson(requestMap);
//    }
//
//    private static Map<String, Object> getRequestBodyMap(String username, String password, String authConfigId, String searchBase) {
//        Map<String, Object> requestMap = new HashMap<>();
//
//        Map<String, String> credentials = new HashMap<>();
//        credentials.put("username", username);
//        credentials.put("password", password);
//        requestMap.put("credentials", credentials);
//        requestMap.put("auth_configs", Collections.singletonList(AuthConfig.fromJSON(authConfigJson(authConfigId, searchBase, ""))));
//        requestMap.put("role_configs", Collections.emptyList());
//        return requestMap;
//    }
//
//
//    private static String authConfigJson(String authConfigId, String searchBase, String searchFilter) {
//        return String.format("{\n" +
//                "  \"id\": \"%s\",\n" +
//                "  \"configuration\": {\n" +
//                "    \"ManagerDN\": \"uid=admin,ou=system\",\n" +
//                "    \"DisplayNameAttribute\": \"displayName\",\n" +
//                "    \"SearchBases\": \"%s\",\n" +
//                "    \"UserSearchFilter\": \"%s\",\n" +
//                "    \"UserLoginFilter\": \"uid\",\n" +
//                "    \"Url\": \"ldap://localhost:10389\",\n" +
//                "    \"Password\": \"secret\"\n" +
//                "  }\n" +
//                "}", authConfigId, searchBase, searchFilter);
//    }
//
//    private static String roleConfigJson(String roleName, String authConfigId, String attributeName, String attributeValue, String groupMembershipFilter, String groupMembershipSearchBase) {
//        return String.format("{\n" +
//                "  \"name\": \"%s\",\n" +
//                "  \"auth_config_id\": \"%s\",\n" +
//                "  \"configuration\": {\n" +
//                "    \"UserGroupMembershipAttribute\": \"%s\",\n" +
//                "    \"GroupIdentifiers\": \"%s\",\n" +
//                "    \"GroupMembershipFilter\": \"%s\",\n" +
//                "    \"GroupSearchBases\": \"%s\"\n" +
//                "  }\n" +
//                "}", roleName, authConfigId, attributeName, attributeValue, groupMembershipFilter, groupMembershipSearchBase);
//    }
//
//    public static RoleConfig roleConfigWith(String roleName, String authConfigId) {
//        return RoleConfig.fromJSON(roleConfigJson(roleName, authConfigId, "memberOf", "ou=pune", "(| (member={dn}))", ""));
//    }
//
//    public static RoleConfig roleConfigWith(String roleName, String authConfigId, String groupMembershipFilter) {
//        return RoleConfig.fromJSON(roleConfigJson(roleName, authConfigId, "memberOf", "ou=pune", groupMembershipFilter, ""));
//    }
//
//    public static RoleConfig roleConfigWith(String roleName, String authConfigId, String groupMembershipFilter, String groupMembershipSearchBase) {
//        return RoleConfig.fromJSON(roleConfigJson(roleName, authConfigId, "memberOf", "ou=pune", groupMembershipFilter, groupMembershipSearchBase));
//    }
//
//    public static AuthConfig authConfigWith(String authConfigId) {
//        return AuthConfig.fromJSON(authConfigJson(authConfigId, "ou=system", ""));
//    }
}
