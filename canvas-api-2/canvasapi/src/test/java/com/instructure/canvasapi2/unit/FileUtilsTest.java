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

import com.instructure.canvasapi2.utils.FileUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


public class FileUtilsTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void deleteAllFilesInDirectory_null() {
        assertEquals(false, FileUtils.deleteAllFilesInDirectory(null));
    }

    @Test
    public void deleteAllFilesInDirectory_singleDir() {
        File rootDir = tempFolder.getRoot();
        FileUtils.deleteAllFilesInDirectory(rootDir);
        assertEquals(false, rootDir.exists());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void deleteAllFilesInDirectory_nestedDirs() throws IOException {
        File rootDir = tempFolder.getRoot();
        ArrayList<File> testDirs = new ArrayList<>();
        ArrayList<File> testFiles = new ArrayList<>();

        testDirs.add(rootDir);
        for (int i = 0; i < 5; i++) {
            File dir = new File(rootDir, "testDir" + i);
            dir.mkdir();
            testDirs.add(dir);
        }

        for (File dir : testDirs) {
            for (int i = 0; i < 5; i++) {
                File file = new File(dir, "testFile" + i);
                file.createNewFile();
                testFiles.add(file);
            }
        }

        FileUtils.deleteAllFilesInDirectory(rootDir);

        testFiles.addAll(testDirs);
        for (File file : testFiles) {
            assertEquals(false, file.exists());
        }
    }

    @Test
    public void getFileExtensionFromMimetype_null() {
        assertEquals("", FileUtils.getFileExtensionFromMimetype(null));
    }

    @Test
    public void getFileExtensionFromMimetype_validInput() {
        assertEquals("png", FileUtils.getFileExtensionFromMimetype("image/png"));
    }

    @Test
    public void getFileExtensionFromMimetype_invalidInput() {
        assertEquals("jpg", FileUtils.getFileExtensionFromMimetype("jpg"));
    }

    @Test
    public void kalturaCodeFromMimeType_null() {
        assertEquals("0", FileUtils.kalturaCodeFromMimeType(null));
    }

    @Test
    public void kalturaCodeFromMimeType_video() {
        assertEquals("1", FileUtils.kalturaCodeFromMimeType("video/mp4"));
    }

    @Test
    public void kalturaCodeFromMimeType_audio() {
        assertEquals("5", FileUtils.kalturaCodeFromMimeType("audio/mp3"));
    }

    @Test
    public void kalturaCodeFromMimeType_invalid() {
        assertEquals("0", FileUtils.kalturaCodeFromMimeType("pdf"));
    }

    @Test
    public void mediaTypeFromKalturaCode_video() {
        assertEquals("video", FileUtils.mediaTypeFromKalturaCode(1));
    }

    @Test
    public void mediaTypeFromKalturaCode_audio() {
        assertEquals("audio", FileUtils.mediaTypeFromKalturaCode(5));
    }

    @Test
    public void mediaTypeFromKalturaCode_other() {
        assertEquals("", FileUtils.mediaTypeFromKalturaCode(123));
    }
}