/**
 * QFieldCloudService.java
 * @author  Mathieu Pellerin - <mathieu@opengis.ch>
 */
/*
 Copyright (c) 2021, Mathieu Pellerin <mathieu@opengis.ch>
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

import android.app.Notification;
// ... existing imports ...

public class SigpacgoCloudService extends QtService { // Renamed class

    private NotificationManager notificationManager;
    private NotificationChannel notificationChannel;

    private final String CHANNEL_ID = "sigpacgo_service_01"; // Updated channel ID
    private final int NOTIFICATION_ID = 101;

    public static void startSigpacgoCloudService(Context context) { // Updated class name
        Log.v("SigpacgoCloudService", "Starting SigpacgoCloudService"); // Updated class name
        Intent intent = new Intent(context, SigpacgoCloudService.class); // Updated class name
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        Log.v("SigpacgoCloudService", "onCreate triggered"); // Updated class name
        super.onCreate();

        notificationManager =
            (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationChannel =
                new NotificationChannel(CHANNEL_ID, "SIGPAC-Go Cloud", // Updated channel name
                                        NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(
                "SIGPAC-Go Cloud background synchronization"); // Updated description
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    @Override
    public void onDestroy() {
        Log.v("SigpacgoCloudService", "onDestroy triggered"); // Updated class name
        notificationManager.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        showNotification();
        return ret;
    }

    private void showNotification() {
        Notification.Builder builder =
            new Notification.Builder(this)
                .setSmallIcon(R.drawable.qfield_logo)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("SIGPAC-Go Cloud") // Updated title
                .setContentText(getString(R.string.upload_pending_attachments))
                .setProgress(0, 0, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setChannelId(CHANNEL_ID);
        }

        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
