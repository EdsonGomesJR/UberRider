package com.edson.uberrider.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.edson.uberrider.Helper.NotificationHelper;
import com.edson.uberrider.R;
import com.edson.uberrider.RateActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification().getTitle().equals("Cancel")) {

            //Because this is outside of main thread, so if want to  run Toast, you'll need to create a Handler to do that
            //choose Handler from android.os

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(MyFirebaseMessaging.this, "" + remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (remoteMessage.getNotification().getTitle().equals("Arrived")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showArrivedNotificationAPI26(remoteMessage.getNotification().getBody());
            } else {

                showArrivedNotification(remoteMessage.getNotification().getBody());
            }

        } else if (remoteMessage.getNotification().getTitle().equals("DropOff")) {

            openRateActivity(remoteMessage.getNotification().getBody());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationAPI26(String body) {

        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext()
                , 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getUberNotification("Arrived", body, contentIntent, defaultSound);

        notificationHelper.getManager().notify(1, builder.build());
    }

    private void openRateActivity(String body) {

        Intent intent = new Intent(this, RateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showArrivedNotification(String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "com.edson.uberrider.Service.test";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("EDS Channel");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableLights(true);
            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_car)
                .setContentTitle("Arrived")

                .setContentText(body)
                .setContentInfo("Info");
        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }

    private void showDropOffNotification(String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "com.edson.uberrider.Service.test";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("EDS Channel");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.YELLOW);
            notificationChannel.canShowBadge();
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableLights(true);
            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_car)
                .setContentTitle("DropOff")
                .setContentText(body)
                .setContentInfo("Info");
        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }

    /**  private void showArrivedNotification(String body) {

        //this method only works for api 25 and lower, for api 26+ i'll have to use Notification Channel

        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                0,new Intent(), PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder =  new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,builder.build());
    }*/
}

