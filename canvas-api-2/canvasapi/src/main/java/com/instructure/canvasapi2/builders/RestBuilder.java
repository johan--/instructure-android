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

package com.instructure.canvasapi2.builders;

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.CanvasRestAdapter;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.Logger;

import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;


public class RestBuilder extends CanvasRestAdapter {

    @Deprecated
    public RestBuilder() {
        super(new StatusCallback() {});
    }

    public RestBuilder(@NonNull StatusCallback callback) {
        super(callback);
    }
    
    public <T> T build(@NonNull Class<T> clazz, @NonNull RestParams params) {
        params = new RestParams.Builder(params).withForceReadFromCache(false).build();
        Retrofit restAdapter = buildAdapter(params);
        return restAdapter.create(clazz);
    }

    public <T> T buildNotorious(@NonNull Class<T> clazz) {
        return new Retrofit.Builder()
                .baseUrl(ApiPrefs.getFullNotoriousDomain() + "/api_v3/")
                .addConverterFactory(SimpleXmlConverterFactory.createNonStrict(new Persister(new AnnotationStrategy())))
                .client(getOkHttpClient())
                .build()
                .create(clazz);
    }

    public <T> T buildSerializeNulls(@NonNull Class<T> clazz, @NonNull RestParams params) {
        params = new RestParams.Builder(params).withForceReadFromCache(false).build();
        Retrofit restAdapter = buildAdapterSerializeNulls(params);
        return restAdapter.create(clazz);
    }

    public <T> T buildNoRedirects(@NonNull Class<T> clazz, @NonNull RestParams params) {
        params = new RestParams.Builder(params).withForceReadFromCache(false).build();
        Retrofit restAdapter = buildAdapterNoRedirects(params);
        return restAdapter.create(clazz);
    }

    public <T> T buildPing(@NonNull Class<T> clazz, @NonNull RestParams params) {
        Retrofit restAdapter = buildPingAdapter(params.getDomain());
        return restAdapter.create(clazz);
    }

    public <T> T buildRollCall(@NonNull Class<T> clazz, @NonNull RestParams params) {
        Retrofit restAdapter = buildRollCallAdapter(params.getDomain());
        return restAdapter.create(clazz);
    }

    public static boolean clearCacheDirectory() {
        try {
            return CanvasRestAdapter.getCacheDirectory().delete();
        } catch (Exception e) {
            Logger.e("Could not delete cache " + e);
            return false;
        }
    }
}
