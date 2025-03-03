/**
 * QFieldActivity.java - class needed to copy files from assets to
 * getExternalFilesDir() before starting QtActivity this can be used to perform
 * actions before QtActivity takes over.
 * @author  Marco Bernasocchi - <marco@opengis.ch>
 */
/*
 Copyright (c) 2011, Marco Bernasocchi <marco@opengis.ch>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the  Marco Bernasocchi <marco@opengis.ch> nor the
 names of its contributors may be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY Marco Bernasocchi <marco@opengis.ch> ''AS IS'' AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL Marco Bernasocchi <marco@opengis.ch> BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.imagrieng.sigpacgo;

import android.Manifest;
// ... existing imports ...

public class SigpacgoActivity extends QtActivity { // Renamed class

    private static final int IMPORT_DATASET = 300;
    private static final int IMPORT_PROJECT_FOLDER = 301;
    private static final int IMPORT_PROJECT_ARCHIVE = 302;

    private static final int UPDATE_PROJECT_FROM_ARCHIVE = 400;

    private static final int EXPORT_TO_FOLDER = 500;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferenceEditor;
    private ProgressDialog progressDialog;
    ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static native void openProject(String url);
    public static native void openPath(String path);

    public static native void volumeKeyDown(int volumeKeyCode);
    public static native void volumeKeyUp(int volumeKeyCode);

    public static native void resourceReceived(String path);
    public static native void resourceOpened(String path);
    public static native void resourceCanceled(String message);

    private Intent projectIntent;
    private float originalBrightness;
    private boolean handleVolumeKeys = false;
    private String pathsToExport;
    private String projectPath;

    private static final int CAMERA_RESOURCE = 600;
    private static final int GALLERY_RESOURCE = 601;
    private static final int FILE_PICKER_RESOURCE = 602;
    private static final int OPEN_RESOURCE = 603;
    private String resourcePrefix;
    private String resourceFilePath;
    private String resourceSuffix;
    private String resourceTempFilePath;
    private File resourceFile;
    private File resourceCacheFile;
    private boolean resourceIsEditing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        prepareQtActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction() == Intent.ACTION_VIEW ||
            intent.getAction() == Intent.ACTION_SEND) {
            projectIntent = intent;
            processProjectIntent();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // ... existing method body ...
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // ... existing method body ...
    }

    private boolean isDarkTheme() {
        // ... existing method body ...
    }

    private void vibrate(int milliseconds) {
        // ... existing method body ...
    }

    private void processProjectIntent() {
        showBlockingProgressDialog(getString(R.string.processing_message));

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // ... existing method body ...
            }
        });
    }

    private Insets getSafeInsets() {
        // ... existing method body ...
    }

    private double statusBarMargin() {
        // ... existing method body ...
    }

    private double navigationBarMargin() {
        // ... existing method body ...
    }

    private void dimBrightness() {
       // ... existing method body ...
    }

    private void restoreBrightness() {
        // ... existing method body ...
    }

    private void takeVolumeKeys() {
        // ... existing method body ...
    }

    private void releaseVolumeKeys() {
        // ... existing method body ...
    }

    private void showBlockingProgressDialog(String message) {
        // ... existing method body ...
    }

    private void dismissBlockingProgressDialog() {
        // ... existing method body ...
    }

    private void displayAlertDialog(String title, String message) {
        // ... existing method body ...
    }

    private void initiateSentry() {
       // ... existing method body ...
    }

    private void prepareQtActivity() {
        sharedPreferences =
            getSharedPreferences("SIGPAC-Go", Context.MODE_PRIVATE);  // Updated from "QField"
        sharedPreferenceEditor = sharedPreferences.edit();

        checkAllFileAccess(); // Storage access permission handling for Android
                              // 11+

        List<String> dataDirs = new ArrayList<String>();

        File primaryExternalFilesDir = getExternalFilesDir(null);
        if (primaryExternalFilesDir != null) {
            String dataDir = primaryExternalFilesDir.getAbsolutePath() + "/";
            // create import directories
            new File(dataDir + "Imported Datasets/").mkdir();
            new File(dataDir + "Imported Projects/").mkdir();

            dataDir = dataDir + "SIGPAC-Go/";  // Updated from "QField/"
            // create directories
            new File(dataDir).mkdir();
            new File(dataDir + "basemaps/").mkdir();
            new File(dataDir + "fonts/").mkdir();
            new File(dataDir + "proj/").mkdir();
            new File(dataDir + "auth/").mkdir();
            new File(dataDir + "logs/").mkdirs();
            new File(dataDir + "plugins/").mkdirs();

            dataDirs.add(dataDir);
        }

        String storagePath =
            Environment.getExternalStorageDirectory().getAbsolutePath();
        String rootDataDir = storagePath + "/SIGPAC-Go/";  // Updated from "/QField/"
        File storageFile = new File(rootDataDir);
        storageFile.mkdir();
        if (storageFile.canWrite()) {
            // create directories
            new File(rootDataDir + "basemaps/").mkdir();
            new File(rootDataDir + "fonts/").mkdir();
            new File(rootDataDir + "proj/").mkdir();
            new File(rootDataDir + "auth/").mkdir();
            new File(rootDataDir + "logs/").mkdirs();

            dataDirs.add(rootDataDir);
        }

        File[] externalFilesDirs = getExternalFilesDirs(null);
        for (File file : externalFilesDirs) {
            if (file != null) {
                // Don't duplicate primary external files directory
                if (file.getAbsolutePath().equals(
                        primaryExternalFilesDir.getAbsolutePath())) {
                    continue;
                }

                // create QField directories
                String dataDir = file.getAbsolutePath() + "/SIGPAC-Go/";
                new File(dataDir + "basemaps/").mkdirs();
                new File(dataDir + "fonts/").mkdirs();
                new File(dataDir + "proj/").mkdirs();
                new File(dataDir + "auth/").mkdirs();
                new File(dataDir + "logs/").mkdirs();

                dataDirs.add(dataDir);
            }
        }

        Intent intent = new Intent();
        intent.setClass(SigpacgoActivity.this, QtActivity.class); // Updated class name
        try {
            ActivityInfo activityInfo = getPackageManager().getActivityInfo(
                getComponentName(), PackageManager.GET_META_DATA);
            intent.putExtra("GIT_REV", activityInfo.metaData.getString(
                                           "android.app.git_rev"));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            finish();
            return;
        }

        StringBuilder appDataDirs = new StringBuilder();
        for (String dataDir : dataDirs) {
            appDataDirs.append(dataDir);
            appDataDirs.append("--;--");
        }
        intent.putExtra("QFIELD_APP_DATA_DIRS", appDataDirs.toString());

        Intent sourceIntent = getIntent();
        if (sourceIntent.getAction() == Intent.ACTION_VIEW ||
            sourceIntent.getAction() == Intent.ACTION_SEND) {
            projectIntent = sourceIntent;
            intent.putExtra("QGS_PROJECT", "trigger_load");
        }
        setIntent(intent);
    }

    private String getApplicationDirectory() {
        // ... existing method body ...
    }

    private String getAdditionalApplicationDirectories() {
        // ... existing method body ...
    }

    private String getRootDirectories() {
        // ... existing method body ...
    }

    private void triggerImportDatasets() {
        // ... existing method body ...
    }

    private void triggerImportProjectFolder() {
        // ... existing method body ...
    }

    private void triggerImportProjectArchive() {
        // ... existing method body ...
    }

    private void triggerUpdateProjectFromArchive(String path) {
        // ... existing method body ...
    }

    private void sendDatasetTo(String paths) {
        // ... existing method body ...
    }

    private void exportToFolder(String paths) {
        // ... existing method body ...
    }

    private void removeDataset(String path) {
        // ... existing method body ...
    }

    private void sendCompressedFolderTo(String path) {
        // ... existing method body ...
    }

    private void removeProjectFolder(String path) {
        // ... existing method body ...
    }

    private void getCameraResource(String prefix, String filePath,
                                   String suffix, boolean isVideo) {
        // ... existing method body ...
    }

    private void getGalleryResource(String prefix, String filePath,
                                    String mimeType) {
        // ... existing method body ...
    }

    private void getFilePickerResource(String prefix, String filePath,
                                       String mimeType) {
        // ... existing method body ...
    }

    private void openResource(String filePath, String mimeType,
                              boolean isEditing) {
        // ... existing method body ...
    }

    void importDatasets(Uri[] datasetUris) {
        // ... existing method body ...
    }

    void importProjectFolder(Uri folderUri) {
        // ... existing method body ...
    }

    void importProjectArchive(Uri archiveUri) {
       // ... existing method body ...
    }

    void updateProjectFromArchive(Uri archiveUri) {
        // ... existing method body ...
    }

    private void checkStoragePermissions() {
        // ... existing method body ...
    }

    private void checkAllFileAccess() {
       // ... existing method body ...
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        // ... existing method body ...
    }
}
