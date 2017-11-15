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
@file:Suppress("EXPERIMENTAL_FEATURE_WARNING", "unused")

package com.instructure.canvasapi2.utils.weave

import android.os.AsyncTask
import com.instructure.canvasapi2.StatusCallback
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.HandlerContext
import kotlinx.coroutines.experimental.android.UI
import retrofit2.Call
import retrofit2.Response
import kotlin.coroutines.experimental.CoroutineContext

/** Because 'Coroutine' is just too long */
typealias WeaveJob = WeaveCoroutine

/** Type alias for the call to begin an API request */
internal typealias ManagerCall<T> = (callback: StatusCallback<T>) -> Unit

/** Type alias for the call invoked on API success */
internal typealias SuccessCall<T> = (payload: T) -> Unit

/** Type alias for the call invoke on API failure */
internal typealias ErrorCall = (error: StatusCallbackError) -> Unit

/** Convenience class to hold the information returned in [StatusCallback.onFailure] */
class StatusCallbackError(val call: Call<*>? = null, val error: Throwable? = null, val response: Response<*>? = null) : Throwable()

/**
 * A partial generator interface usable by code designed to suspend coroutine execution indefinitely
 * while reactively iterating through a potentially infinite number of internal operations. The primary
 * application of [Stitcher] is to support API pagination within [weave].
 */
interface Stitcher {
    /** Called when the next operation is requested */
    fun next()

    /** A reference to the current [Continuation][kotlin.coroutines.experimental.Continuation] */
    var continuation: CancellableContinuation<Unit>

    /** Should be called by implementations when all internal operations have completed */
    var onRelease: () -> Unit
}

/**
 * WeaveCoroutine - A Coroutine class customized to meet the specific needs of our applications. This
 * includes a modular exception handler, Stitcher support, [onUI] and [inBackground] functions.
 */
abstract class WeaveCoroutine(coroutineContext: CoroutineContext) : AbstractCoroutine<Unit>(coroutineContext, true) {
    var onException: ((e: Throwable) -> Unit)? = null
    abstract fun onUI(block: () -> Unit)
    abstract suspend fun <T> inBackground(block: () -> T): T

    // region Stitcher
    private var stitcher: Stitcher? = null

    fun next() = stitcher?.next()

    fun addAndStartStitcher(newStitcher: Stitcher) {
        stitcher = newStitcher
        stitcher?.onRelease = { stitcher = null }
        stitcher?.next()
    }
    // endregion
}

/**
 * An Android-UI-thread-aware implementation of [WeaveCoroutine].
 */
class AndroidCoroutine(private val handlerContext: HandlerContext = UI) : WeaveCoroutine(handlerContext) {
    override fun afterCompletion(state: Any?, mode: Int) {
        if (state is CompletedExceptionally) {
            if (onException != null) {
                if (state.exception is CancellationException) return
                if (context[Job]?.cancel(state.exception) == true) return
                state.exception.printStackTrace()
                onException?.invoke(state.exception)
            } else {
                handleCoroutineException(handlerContext, state.exception)
            }
        }
    }

    override fun onUI(block: () -> Unit) {
        handlerContext.dispatch(handlerContext, Runnable { block() })
    }

    override suspend fun <T> inBackground(block: () -> T): T = suspendCancellableCoroutine { continuation ->
        val task = object : AsyncTask<Void, Void, T>() {
            override fun doInBackground(vararg params: Void?) = block()
            override fun onPostExecute(result: T) = continuation.resumeSafely(result)
        }
        continuation.invokeOnCompletion({ if (continuation.isCancelled) task.cancel(true) }, true)
        task.execute()
    }
}


/**
 * A [WeaveCoroutine] implementation which uses a common thread pool instead of Android's UI thread.
 * This will be used in place of [AndroidCoroutine] if we're running on the JVM (à la unit tests)
 * to avoid invoking Android-specific APIs.
 */
class TestCoroutine : WeaveCoroutine(CommonPool) {
    override fun onUI(block: () -> Unit) = block()
    suspend override fun <T> inBackground(block: () -> T) = block()
}

/**
 * Begins a [WeaveCoroutine]
 */
fun weave(block: suspend WeaveCoroutine.() -> Unit): WeaveCoroutine {
    val coroutine: WeaveCoroutine
    if (isUnitTesting) {
        coroutine = TestCoroutine()
        coroutine.initParentJob(CommonPool[Job])
    } else {
        coroutine = AndroidCoroutine()
        coroutine.initParentJob(UI[Job])
    }
    CoroutineStart.DEFAULT(block, coroutine, coroutine)
    return coroutine
}

/** Resumes the continuation (with the provided exception) if allowed by the current state. */
fun <T> CancellableContinuation<T>.resumeSafelyWithException(e: Throwable) {
    if (isActive && !isCancelled && !isCompleted) resumeWithException(e)
}

/** Resumes the continuation if allowed by the current state. */
fun <T> CancellableContinuation<T>.resumeSafely(payload: T) {
    if (isActive && !isCancelled && !isCompleted) resume(payload)
}

/**
 * A lazy check to see if we're running unit tests, based on the assumption that [AndroidCoroutine]
 * will fail to initialize on the JVM.
 */
internal val isUnitTesting: Boolean by lazy {
    try {
        AndroidCoroutine()
        false
    } catch (ignore: Throwable) {
        true
    }
}
