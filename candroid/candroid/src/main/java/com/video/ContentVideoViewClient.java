/*
 * Copyright (C) 2013 The Chromium Authors. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following disclaimer
 *      in the documentation and/or other materials provided with the
 *      distribution.
 *    * Neither the name of Google Inc. nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.video;

import android.view.View;

/**
 *  Main callback class used by ContentVideoView.
 *
 *  This contains the superset of callbacks that must be implemented by the embedder.
 *
 *  onShowCustomView and onDestoryContentVideoView must be implemented,
 *  getVideoLoadingProgressView() is optional, and may return null if not required.
 *
 *  The implementer is responsible for displaying the Android view when
 *  {@link #onShowCustomView(View)} is called.
 */
public interface ContentVideoViewClient {
    /**
     * Called when the video view is ready to be shown. Must be implemented.
     * @param view The view to show.
     */
    public void onShowCustomView(View view);

    /**
     * Called when it's time to destroy the video view. Must be implemented.
     */
    public void onDestroyContentVideoView();

    public boolean isFullscreen();

    /**
     * Allows the embedder to replace the view indicating that the video is loading.
     * If null is returned, the default video loading view is used.
     */
    public View getVideoLoadingProgressView();

    /**
     * Allows the embedder to replace the default playback controls by returning a custom
     * implementation. If null is returned, the default controls are used.
     */
    //public ContentVideoViewControls createControls();
}
