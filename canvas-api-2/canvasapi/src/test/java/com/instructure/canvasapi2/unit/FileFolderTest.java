package com.instructure.canvasapi2.unit;

import com.instructure.canvasapi2.models.FileFolder;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2017 Instructure. All rights reserved.
 */
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