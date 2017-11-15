package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.UnreadConversationCount
import com.instructure.canvasapi2.models.UnreadNotificationCount
import retrofit2.Call
import retrofit2.http.GET


internal object UnreadCountAPI {
    internal interface UnreadCountsInterface {
        @GET("conversations/unread_count")
        fun getUnreadConversationCount(): Call<UnreadConversationCount>

        @GET("users/self/activity_stream/summary")
        fun getNotificationsCount(): Call<List<UnreadNotificationCount>>
    }

    fun getUnreadConversationCount(adapter: RestBuilder, params: RestParams, callback: StatusCallback<UnreadConversationCount>) {
        callback.addCall(adapter.build(UnreadCountsInterface::class.java, params).getUnreadConversationCount()).enqueue(callback)
    }

    fun getUnreadNotificationsCount(adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<UnreadNotificationCount>>) {
        callback.addCall(adapter.build(UnreadCountsInterface::class.java, params).getNotificationsCount()).enqueue(callback)
    }

    fun getUnreadConversationsCountSynchronous(adapter: RestBuilder, params: RestParams): String? {
        return try {
            adapter.build(UnreadCountsInterface::class.java, params).getUnreadConversationCount().execute()?.body()?.unreadCount
        } catch (E: Exception) {
            null
        }

    }
}
