package com.push.messenger.api.constants;

public interface QueryConstants {
    String STR_QUERY_FCM_KEY = "select file_data, file_password from engage_key_config where key_name = 'FCM_KEY'";
    String STR_QUERY_APN_KEY = "select file_data, file_password from engage_key_config where key_name = 'APN_KEY'";
    String STR_QUERY_APN_KEY_ALL = "select file_data, file_password ,value0 from engage_key_config where key_name = 'APN_KEY'";
//    String ACCOUNT_SID = "AC60ee9aadded370214cc7fe9c870d9447";
//    String AUTH_TOKEN = "82ad8150766fba8fb0e136083086254e";
    String ACCOUNT_SID = "AC9a05c80c3c17d5c83bf71c43cf708233";
    String AUTH_TOKEN = "e55074f2cf4ed1e139cf9e08d3720405";
    
    String FROM_NUMBER = "+17177395248";
}
