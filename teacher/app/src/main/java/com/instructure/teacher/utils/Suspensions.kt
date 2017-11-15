/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.instructure.teacher.utils

import com.instructure.canvasapi2.utils.weave.resumeSafely
import com.instructure.teacher.PSPDFKit.CanvaDocsRedirectAsyncTask
import com.instructure.teacher.PSPDFKit.FetchFileAsyncTask
import com.instructure.teacher.PSPDFKit.FileCache
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import java.io.File

/**
 * Attempts to download a file from a URL and return the resulting [File] object. Internally this
 * uses a size-limited disk cache for quick retrieval of the most frequently-accessed files.
 *
 * @param url The URL of the file to be downloaded
 * @param onProgressChanged A callback for download progress updates. Progress is between 0f (0%)
 * and 1f (100%), and will be updated no more than 30 times per second. Note that this is called from
 * a background thread; if you need to manipulate UI based on these updates, you may wrap your code in
 * a [onUI][com.instructure.canvasapi2.utils.weave.WeaveCoroutine.onUI] block.
 * @return The file if it was successfully downloaded or retrieved from cache, or null if there was an error.
 */
suspend fun FileCache.awaitFileDownload(url: String, onProgressChanged: ((Float) -> Unit)? = null): File? =
        suspendCancellableCoroutine { continuation ->

            val task = this.getInputStream(url, object : FetchFileAsyncTask.FetchFileCallback {
                override fun onProgress(progress: Float) {
                    if (!continuation.isCancelled) onProgressChanged?.invoke(progress)
                }

                override fun onFileLoaded(fileInputStream: File?) {
                    continuation.resumeSafely(fileInputStream)
                }

            })

            continuation.invokeOnCompletion({ if (continuation.isCancelled) task.cancel() }, true)
        }

/**
 * Wraps a [CanvaDocsRedirectAsyncTask] and returns the redirected URL
 *
 * @param url The original URL to be redirected
 * @return The redirected URL
 */
suspend fun getCanvaDocsRedirect(url: String): String =
        suspendCancellableCoroutine { continuation ->
            CanvaDocsRedirectAsyncTask(url, {
                continuation.resumeSafely(it)
            }).execute()
        }

