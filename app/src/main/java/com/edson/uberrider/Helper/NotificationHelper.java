package com.edson.uberrider.Helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.edson.uberrider.R;

public class NotificationHelper extends ContextWrapper {

    private static final String EDS_CHANNEL_ID = "com.edson.uberrider.EDS";
    private static final String EDS_CHANNEL_NAME = "EDS UBER";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannels();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {

        NotificationChannel edsChannels = new NotificationChannel(EDS_CHANNEL_ID, EDS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);

        edsChannels.enableLights(true);
        edsChannels.enableVibration(true);
        edsChannels.setLightColor(Color.GRAY);
        edsChannels.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(edsChannels);

    }

    public NotificationManager getManager() {

        if (manager == null)

            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getUberNotification(String title, String content, PendingIntent contentIntent, Uri soundUri) {


        //Da api 26 pra frente deve-se usar um icone vindo do drawable e não mais do mipmap, usando do mipmap ira causar um erro de System UI
        return new Notification.Builder(getApplicationContext(), EDS_CHANNEL_ID)
                .setContentText(content)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_car);

    }
}
