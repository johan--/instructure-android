/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.loginapi.login.api;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.canvasapi2.utils.RetrofitCounter;
import com.instructure.loginapi.login.model.GlobalDataSync;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class GlobalDataSyncAPI {

    public enum NAMESPACE {
        MOBILE_CANVAS_DATA, MOBILE_POLLS_DATA, MOBILE_SPEEDGRADER_DATA,
        MOBILE_CANVAS_COLORS, MOBILE_POLLS_COLORS, MOBILE_SPEEDGRADER_COLORS,
        MOBILE_CANVAS_USER_BACKDROP_IMAGE, MOBILE_CANVAS_USER_NOTIFICATION_STATUS_SETUP}

    public interface DataSyncInterface {

        @GET("/users/{userId}/custom_data/data_sync")
        Call<JsonElement> getGlobalData(@Path("userId") long userId, @Query("ns") String nameSpace);

        @PUT("/users/{userId}/custom_data/data_sync")
        Call<GlobalDataSync> setGlobalData(@Path("userId") long userId, @Query("ns") String nameSpace, @Query("data") String json, @Body String body);
    }

    public static GlobalDataSync getGlobalData(Context context, NAMESPACE namespace){
        RetrofitCounter.increment();
        try {
            User user = ApiPrefs.getUser();
            RestBuilder adapter = new RestBuilder();
            RestParams params = new RestParams.Builder().withShouldIgnoreToken(false).build();
            JsonElement element = adapter.build(DataSyncInterface.class, params).getGlobalData(user.getId(), namespace.toString()).execute().body();

            if(element != null) {
                String json = cleanJson(element.toString());
                return new Gson().fromJson(json, GlobalDataSync.class);
            }
            RetrofitCounter.decrement();
            return null;
        } catch (Exception e){
            RetrofitCounter.decrement();
            Logger.e("===> GLOBAL DATA SYNC ERROR *GET*: " + e);
            return null;
        }
    }

    public static void setGlobalData(Context context, NAMESPACE namespace, GlobalDataSync data){
        RetrofitCounter.increment();
        try {
            User user = ApiPrefs.getUser();
            RestBuilder adapter = new RestBuilder();
            RestParams params = new RestParams.Builder().withShouldIgnoreToken(false).build();
            Response response = adapter.build(DataSyncInterface.class, params).setGlobalData(user.getId(), namespace.toString(), GlobalDataSync.toJsonString(data), "").execute();
            if (!response.isSuccessful()) {
                Logger.e("===> GLOBAL DATA SYNC ERROR *SET*: " + response.errorBody().string());
            }
        } catch (Exception e){
            Logger.e("===> GLOBAL DATA SYNC ERROR *SET*: " + e);
        }
        RetrofitCounter.decrement();
    }

    //Cleans our responses
    private static String cleanJson(String json) {

        if(TextUtils.isEmpty(json)) {
            return null;
        }

        Logger.d("JSON BEFORE: " + json);

        json = json.replaceAll("\\\\", "");

        if(json.startsWith("{\"data\":\"")) {
            String pattern = "\\{\"data\":\"";
            json = json.replaceFirst(pattern, "");
            if(json.endsWith("\"}")) {
                json = json.substring(0, json.length() -2)+json.substring((json.length()));
            }
        }

        Logger.d("JSON AFTER: " + json);
        return json;
    }
}
