package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.UnreadCountAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.UnreadConversationCount
import com.instructure.canvasapi2.models.UnreadNotificationCount


object UnreadCountManager : BaseManager() {

    private val mTesting = false

    @JvmStatic
    fun getUnreadConversationCount(callback: StatusCallback<UnreadConversationCount>, forceNetwork: Boolean) {
        if (isTesting() || mTesting) {
            // TODO
        } else {
            val adapter = RestBuilder(callback)
            val params = RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .build()
            UnreadCountAPI.getUnreadConversationCount(adapter, params, callback)
        }
    }

    @JvmStatic
    fun getUnreadNotificationsCount(callback: StatusCallback<List<UnreadNotificationCount>>, forceNetwork: Boolean) {
        if (isTesting() || mTesting) {
            // TODO
        } else {
            val adapter = RestBuilder(callback)
            val params = RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .build()
            UnreadCountAPI.getUnreadNotificationsCount(adapter, params, callback)
        }
    }

    @JvmStatic
    fun getUnreadConversationsCountSynchronous(): String? {
        if (isTesting() || mTesting) {
            // TODO
            return null
        } else {
            val adapter = RestBuilder()
            val params = RestParams.Builder().build()
            return UnreadCountAPI.getUnreadConversationsCountSynchronous(adapter, params)
        }
    }
}
