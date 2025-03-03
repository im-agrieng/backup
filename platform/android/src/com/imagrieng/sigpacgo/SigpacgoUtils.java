/***************************************************************************
                            QFieldUtils.java
                            -------------------
              begin                : December 6, 2020
              copyright            : (C) 2020 by Mathieu Pellerin
              email                : nirvn dot asia at gmail dot com
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

package com.imagrieng.sigpacgo;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
// ... existing imports ...

public class SigpacgoUtils { // Renamed class

    /**
     * Returns a modified string with attachment filename tags replaced with
     * their values.
     * @param  string   the string to be modified
     * @param  filename the filename from which tags values are derived from
     * @return          modified String
     */
    public static String replaceFilenameTags(String string, String filename) {
        // ... existing method body ...
    }

    /**
     * Returns the first project name found in a given ZIP archive. If
     */
    public static String getArchiveProjectName(InputStream in) {
        // ... existing method body ...
    }

    /**
     * Recursively copies a DocumentFile directory into a specified directory.
     */
    public static boolean documentFileToFolder(DocumentFile directory,
                                               String folder,
                                               ContentResolver resolver) {
        // ... existing method body ...
    }

    /**
     * Copies file(s) into a specified DocumentFile directory.
     */
    public static boolean fileToDocumentFile(File file, DocumentFile directory,
                                             ContentResolver resolver) {
       // ... existing method body ...
    }

    /**
     * Creates a ZIP archive from the provided list of files.
     */
    public static boolean filesToZip(OutputStream out, String[] files) {
        // ... existing method body ...
    }

    /**
     * Extracts the content of a ZIP archive into the provided directory.
     */
    public static boolean zipToFolder(InputStream in, String folder) {
        // ... existing method body ...
    }

    /**
     * Creates a ZIP archive from the content of a provided directory.
     */
    public static boolean folderToZip(String folder, String archivePath) {
        // ... existing method body ...
    }

    /**
     * Adds the content of a given folder into the ZIP archive being created.
     */
    private static boolean addFolderToZip(ZipOutputStream zip, String folder,
                                          String rootFolder) {
        // ... existing method body ...
    }

    /**
     * Copies an InputStream to an OutputStream.
     */
    public static boolean inputStreamToOutputStream(InputStream in,
                                                    OutputStream out,
                                                    long totalBytes) {
        // ... existing method body ...
    }

    /**
     * Copies an InputStream to a provided file path.
     */
    public static boolean inputStreamToFile(InputStream in, String file,
                                            long totalBytes) {
        // ... existing method body ...
    }

    public static boolean copyFile(File src, File dst) {
        // ... existing method body ...
    }

    /**
     * Deletes the provided directory.
     */
    public static boolean deleteDirectory(File file, boolean recursive) {
        // ... existing method body ...
    }

    /**
     * Returns the extension string for a given mime type.
     */
    public static String getExtensionFromMimeType(String type) {
        // ... existing method body ...
    }

    // original script by SANJAY GUPTA
    public static String getPathFromUri(final Context context, final Uri uri) {
        // ... existing method body ...
    }

    public static String getDataColumn(Context context, Uri uri,
                                       String selection,
                                       String[] selectionArgs) {
        // ... existing method body ...
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        // ... existing method body ...
    }

    public static boolean isDownloadsDocument(Uri uri) {
        // ... existing method body ...
    }

    public static boolean isMediaDocument(Uri uri) {
        // ... existing method body ...
    }
}
