package com.twomuchcar.usedcar.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.twomuchcar.usedcar.R;
import com.twomuchcar.usedcar.activities.MainActivity;

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

    private String getTargetUrl(String title) {
        // title에 따라 URL 매핑
        switch (title) {
            case "등록 차량 심사 결과":
                return "https://autoever.site/my/register";
            case "관심 차종 등록":
            case "관심 차량 가격 인하":
                return "https://autoever.site/wishlist";
            case "추천 차량 업데이트":
                return "https://autoever.site";
            default:
                return "https://autoever.site"; // 기본 URL
        }
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
                    remoteMessage.getNotification().getBody(),
                    getTargetUrl(remoteMessage.getNotification().getTitle())

            );
        }

        // 데이터 메시지 처리
        if (remoteMessage.getData().size() > 0) {
            showNotification(
                    remoteMessage.getData().get("title"),
                    remoteMessage.getData().get("body"),
                    getTargetUrl(remoteMessage.getNotification().getTitle())
            );
        }
    }

    private void showNotification(String title, String body, String targetUrl) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("url", targetUrl);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            Log.d("FCM", "Showing notification: " + title);
            manager.notify((int) System.currentTimeMillis(), builder.build());
        } else {
            Log.e("FCM", "NotificationManager is null");
        }
    }
}
