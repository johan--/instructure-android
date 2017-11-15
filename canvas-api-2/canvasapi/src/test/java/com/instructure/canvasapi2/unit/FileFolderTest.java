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
 *
 */
package com.instructure.canvasapi2.unit;

import com.instructure.canvasapi2.models.FileFolder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileFolderTest {

    @Test
    public void compareTo_FolderAndFile() {
        FileFolder folder = new FileFolder();
        folder.setFullName("fileFolder");

        FileFolder file = new FileFolder();
        file.setDisplayName("fileFolder");

        assertEquals(-1, folder.compareTo(file));
    }

    @Test
    public void compareTo_FileAndFolder() {
        FileFolder folder = new FileFolder();
        folder.setFullName("fileFolder");

        FileFolder file = new FileFolder();
        file.setDisplayName("fileFolder");

        assertEquals(1, file.compareTo(folder));
    }

    @Test
    public void compareTo_FolderAndFolder() {
        FileFolder folder1 = new FileFolder();
        folder1.setFullName("Folder 1");

        FileFolder folder2 = new FileFolder();
        folder2.setFullName("Folder 2");

        assertEquals(-1, folder1.compareTo(folder2));
    }

    @Test
    public void compareTo_FolderAndFolderReversed() {
        FileFolder folder1 = new FileFolder();
        folder1.setFullName("Folder 1");

        FileFolder folder2 = new FileFolder();
        folder2.setFullName("Folder 2");

        assertEquals(1, folder2.compareTo(folder1));
    }

    @Test
    public void compareTo_FileAndFile() {
        FileFolder file1 = new FileFolder();
        file1.setFullName("File 1");

        FileFolder file2 = new FileFolder();
        file2.setFullName("File 2");

        assertEquals(-1, file1.compareTo(file2));
    }

    @Test
    public void compareTo_FileAndFileReversed() {
        FileFolder file1 = new FileFolder();
        file1.setFullName("File 1");

        FileFolder file2 = new FileFolder();
        file2.setFullName("File 2");

        assertEquals(1, file2.compareTo(file1));
    }

}
