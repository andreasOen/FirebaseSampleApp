package com.example.kk_mb_007.firebasesampleandroid.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.example.kk_mb_007.firebasesampleandroid.R;
import com.example.kk_mb_007.firebasesampleandroid.activity.MainActivity;
import com.example.kk_mb_007.firebasesampleandroid.app.Const;
import com.example.kk_mb_007.firebasesampleandroid.util.NotificationUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by Andreas on 7/18/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String EXTRA_KEY_MESSSAGE = "message";
    public static final String FIELD_DATA = "data";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_BACKGROUND = "is_background";
    public static final String FIELD_IMAGE_URL = "imageurl";
    public static final String FIELD_TIME_STAMP = "timestamp";
    public static final String FIELD_PAYLOAD = "payload";

    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Timber.d("From: " + remoteMessage.getFrom());

        if (remoteMessage == null) return;

        if (NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            if (remoteMessage.getNotification() != null) {
                sendNotification(remoteMessage.getNotification().getTitle(),
                        remoteMessage.getNotification().getBody());
            }
        } else {
            if (remoteMessage.getData().size() > 0) {
                Timber.d("Data payload: " + remoteMessage.getData().toString());

                try {
                    Intent pushNotification = new Intent(Const.PUSH_NOTIFICATION);
                    pushNotification.putExtra(EXTRA_KEY_MESSSAGE,
                            remoteMessage.getData().get(FIELD_IMAGE_URL));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                } catch (Exception e) {
                    Timber.d("Exception: " + e.getMessage());
                }
            }
        }
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void handleNotification(String message) {
        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
    }

    private void handleDataMessage(JSONObject jsonObject) {
        Timber.d("push json: " + jsonObject.toString());

        try {
            JSONObject data = jsonObject.getJSONObject(FIELD_DATA);

            String title = data.getString(FIELD_TITLE);
            String message = data.getString(FIELD_MESSAGE);
            String imageUrl = data.getString(FIELD_IMAGE_URL);

            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                Intent pushNotification = new Intent(Const.PUSH_NOTIFICATION);
                pushNotification.putExtra(EXTRA_KEY_MESSSAGE, imageUrl);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.playNotificationSound();
            } else {
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra(FIELD_MESSAGE, message);

                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), title, message, "",
                            resultIntent);
                } else {
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, "",
                            resultIntent, imageUrl);
                }
            }
        } catch (JSONException e) {
            Timber.e("Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Timber.e("Exception: " + e.getMessage());
        }
    }

    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    private void showNotificationMessageWithBigImage(Context context, String title, String message,
                                                     String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }
}
