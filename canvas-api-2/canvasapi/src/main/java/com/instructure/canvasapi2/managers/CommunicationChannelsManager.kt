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
 */    package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.CommunicationChannelsAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CommunicationChannel
import retrofit2.Response

object CommunicationChannelsManager : BaseManager() {

    private val mIsTesting = false

    @JvmStatic
    fun getCommunicationChannels(userId: Long, callback: StatusCallback<List<CommunicationChannel>>, forceNetwork: Boolean) {
        if (isTesting() || mIsTesting) {
            // TODO
        } else {
            val adapter = RestBuilder(callback)
            val params = RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .build()
            CommunicationChannelsAPI.getCommunicationChannels(userId, adapter, params, callback)
        }
    }

    @JvmStatic
    fun addNewPushCommunicationChannelSynchronous(registrationId: String): Response<Void>? {
        if (isTesting() || mIsTesting) {
            // TODO
        } else {
            val adapter = RestBuilder()
            val params = RestParams.Builder().build()
            return CommunicationChannelsAPI.addNewPushCommunicationChannelSynchronous(registrationId, adapter, params)
        }
        return null
    }

}
