package com.example.kk_mb_007.firebasesampleandroid.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

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
    private static final String EXTRA_KEY_MESSSAGE = "message";
    private static final String FIELD_DATA = "data";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_MESSAGE = "message";
    private static final String FIELD_BACKGROUND = "is_background";
    private static final String FIELD_IMAGE_URL = "image";
    private static final String FIELD_TIME_STAMP = "timestamp";
    private static final String FIELD_PAYLOAD = "payload";

    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Timber.d("From: " + remoteMessage.getFrom());

        if (remoteMessage == null) return;

        if (remoteMessage.getNotification() != null) {
            Timber.d("Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }

        if (remoteMessage.getData().size() > 0) {
            Timber.d("Data payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);
            }catch (Exception e) {
                Timber.d("Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            Intent pushNotification = new Intent(Const.PUSH_NOTIFICATION);
            pushNotification.putExtra(EXTRA_KEY_MESSSAGE, message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        } else {

        }
    }

    private void handleDataMessage(JSONObject jsonObject) {
        Timber.d("push json: " + jsonObject.toString());

        try {
            JSONObject data = jsonObject.getJSONObject(FIELD_DATA);

            String title = data.getString(FIELD_TITLE);
            String message = data.getString(FIELD_MESSAGE);
            boolean isBackground = data.getBoolean(FIELD_BACKGROUND);
            String imageUrl = data.getString(FIELD_IMAGE_URL);
            String timestamp = data.getString(FIELD_TIME_STAMP);
            JSONObject payload = data.getJSONObject(FIELD_PAYLOAD);

            Timber.d("title: " + title);
            Timber.d("message: " + message);
            Timber.d("isBackground: " + isBackground);
            Timber.d("payload: " + payload.toString());
            Timber.d("imageUrl: " + imageUrl);
            Timber.d("timestamp: " + timestamp);

            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                Intent pushNotification = new Intent(Const.PUSH_NOTIFICATION);
                pushNotification.putExtra(FIELD_MESSAGE, message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.playNotificationSound();
            } else {
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra(FIELD_MESSAGE, message);

                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), title, message, timestamp,
                            resultIntent);
                } else {
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp,
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
