/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.canvasapi2.unit;

import com.instructure.canvasapi2.models.FileUploadParams;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.RequestBody;

import static org.junit.Assert.*;


public class FileUploadParamsTest {

    @Test
    public void getPlainTextUploadParams() {
        LinkedHashMap<String, String> uploadParams = new LinkedHashMap<>();
        uploadParams.put("param1", "value1");
        uploadParams.put("param2", "value2");
        uploadParams.put("param3", "value3");

        FileUploadParams fileUploadParams = new FileUploadParams();
        fileUploadParams.setUploadParams(uploadParams);

        LinkedHashMap<String, RequestBody> plainUploadParams = fileUploadParams.getPlainTextUploadParams();

        for (Map.Entry<String, String> entry : uploadParams.entrySet()) {
            RequestBody requestBody = plainUploadParams.get(entry.getKey());
            assertNotNull(requestBody);
            assertEquals("text", requestBody.contentType().type());
            assertEquals("plain", requestBody.contentType().subtype());
        }
    }

}