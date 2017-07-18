package com.example.kk_mb_007.firebasesampleandroid.service;

import android.content.Intent;
import android.content.SharedPreferences;

import com.example.kk_mb_007.firebasesampleandroid.app.Const;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import timber.log.Timber;

/**
 * Created by Andreas on 7/18/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    public static final String PREF_FCM_TOKEN_KEY = "fcm_token";
    private static final String EXTRA_FCM_KEY = "fcm_key";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        storeRegIdInPref(refreshedToken);

        sendRegistrationToServer(refreshedToken);

        Intent registrationComplete = new Intent(Const.REGISTRATION_COMPLETE);
        registrationComplete.putExtra(EXTRA_FCM_KEY, refreshedToken);
    }

    private void sendRegistrationToServer(final String token) {
        Timber.e("send registration to server " + token);
    }

    private void storeRegIdInPref(String token) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Const.SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_FCM_TOKEN_KEY, token);
        editor.commit();
    }
}
