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
package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.*
import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.dataseeding.util.Randomizer
import okhttp3.HttpUrl
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.FormElement
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Contains an interface that defines APIs for User endpoints
 * as well as methods for making the Retrofit calls to those APIs
 */
object UserApi {

    interface UserService {

        // TODO: this is probably better named as createUser / createRandomUser
        @POST("users")
        fun createTeacher(@Body createUser: CreateUser): Call<CanvasUser>

        @POST("/login/oauth2/token")
        fun getToken(
                @Query("client_id") clientId: String,
                @Query("client_secret") clientSecret: String,
                @Query("code") authCode: String,
                @Query(value = "redirect_uri", encoded = true) redirectURI: String
        ): Call<OAuthToken>
    }

    private val userService: UserService by lazy {
        CanvasRestAdapter.retrofit.create(UserService::class.java)
    }

    fun createRandomTeacher(): Response<CanvasUser> {
        val teacherName = Randomizer.randomName()
        val user = User(teacherName.fullName, teacherName.firstName, teacherName.sortableName)

        val pseudonym = Pseudonym(
                Randomizer.randomEmail(),
                Randomizer.randomPassword()
        )

        val communicationChannel = CommunicationChannel(true)

        val createUser = CreateUser(user, pseudonym, communicationChannel)

        val createdUser = UserApi.userService.createTeacher(createUser).execute()

        // Add extra data to the CanvasUser
        with(createdUser.body()) {
            loginId = createUser.pseudonym.uniqueId
            password = createUser.pseudonym.password
            token = getToken(createdUser.body())
        }

        return createdUser
    }

    /**
     * Gets an access token for the user as described [here](https://canvas.instructure.com/doc/api/file.oauth_endpoints.html)
     * @param[user] A [CanvasUser]
     * @return An [String] access token for the user. NOTE: the token has an expiration of 1 hour.
     */
    private fun getToken(user: CanvasUser): String {
        val authCode = getAuthCode(user)
        val response = userService.getToken(
                CanvasRestAdapter.clientId,
                CanvasRestAdapter.clientSecret,
                authCode,
                CanvasRestAdapter.redirectUri
        ).execute()
        return response.body().accessToken
    }

    /**
     * Gets an authentication code for the user as described [here](https://canvas.instructure.com/doc/api/file.oauth_endpoints.html)
     * @param[user] A [CanvasUser]
     * @return The [String] auth code to be used to acquire the user's access token
     */
    private fun getAuthCode(user: CanvasUser): String {
        val loginPageResponse = Jsoup.connect("https://${CanvasRestAdapter.canvasDomain}/login/oauth2/auth")
                .method(Connection.Method.GET)
                .data("client_id", CanvasRestAdapter.clientId)
                .data("response_type", "code")
                .data("redirect_uri", CanvasRestAdapter.redirectUri)
                .execute()
        val loginForm = loginPageResponse.parse().select("form").first() as FormElement
        loginForm.getElementById("pseudonym_session_unique_id").`val`(user.loginId)
        loginForm.getElementById("pseudonym_session_password").`val`(user.password)
        val authFormResponse = loginForm.submit().cookies(loginPageResponse.cookies()).execute()
        val authForm = authFormResponse.parse().select("form").first() as FormElement
        val responseUrl = authForm.submit().cookies(authFormResponse.cookies()).execute().url().toString()
        return HttpUrl.parse(responseUrl).queryParameter("code") ?: throw RuntimeException("/login/oauth2/auth failed!")
    }
}
