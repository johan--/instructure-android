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
 */
package com.instructure.dataseeding.util

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CanvasRestAdapter {

    val canvasDomain = "mobileqa.test.instructure.com"
    val adminToken = DATA_SEEDING_ADMIN_TOKEN
    val baseUrl = "https://$canvasDomain/api/v1/accounts/self/"
    val otherBaseUrl = "https://$canvasDomain/api/v1/"
    val clientId = DATA_SEEDING_CLIENT_ID
    val clientSecret = DATA_SEEDING_CLIENT_SECRET
    val redirectUri = "urn:ietf:wg:oauth:2.0:oob"

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        OkHttpClient.Builder()
                .addInterceptor { chain ->
                    var request = chain.request()
                    val builder = request.newBuilder()

                    builder.addHeader("Authorization", "Bearer $adminToken")

                    request = builder.build()

                    chain.proceed(request)
                }
                .addInterceptor(loggingInterceptor)
                .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    val enrollmentRetrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(otherBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }
}
