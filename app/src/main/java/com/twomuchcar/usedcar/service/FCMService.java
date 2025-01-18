package com.twomuchcar.usedcar.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.twomuchcar.usedcar.R;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class FCMService extends FirebaseMessagingService {
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                "default",
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Default notification channel");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
//        Log.d("FCM", "From: " + remoteMessage.getFrom());
//        Log.d("FCM", "Message data: " + remoteMessage.getData());
//        Log.d("FCM", "Message notification: " + remoteMessage.getNotification());

        Log.d("FCM", "Title: " + remoteMessage.getNotification().getTitle());
        Log.d("FCM", "Body: " + remoteMessage.getNotification().getBody());

        // 알림 데이터 처리
        if (remoteMessage.getNotification() != null) {
            showNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody()
            );
        }

        // 데이터 메시지 처리
        if (remoteMessage.getData().size() > 0) {
            showNotification(
                    remoteMessage.getData().get("title"),
                    remoteMessage.getData().get("body")
            );
        }
    }

    private void showNotification(String title, String body) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            Log.d("FCM", "Showing notification: " + title);
            manager.notify((int) System.currentTimeMillis(), builder.build());
        } else {
            Log.e("FCM", "NotificationManager is null");
        }
    }
}
