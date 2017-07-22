package com.example.kk_mb_007.firebasesampleandroid.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kk_mb_007.firebasesampleandroid.R;
import com.example.kk_mb_007.firebasesampleandroid.app.Const;
import com.example.kk_mb_007.firebasesampleandroid.service.MyFirebaseInstanceIDService;
import com.example.kk_mb_007.firebasesampleandroid.service.MyFirebaseMessagingService;
import com.example.kk_mb_007.firebasesampleandroid.util.NotificationUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String REMOTE_CONFIG_KEY = "background_color";

    @BindView(R.id.img_show)
    ImageView imgShow;

    @BindView(R.id.container)
    LinearLayout container;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.txt_subscribe_to)
    TextView txtSubscribeTo;

    @BindView(R.id.btn_subscribe)
    Button btnSubscribe;

    @BindView(R.id.btn_unsubscribe)
    Button btnUnsubscribe;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private long cacheExpiration = 3600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initFirebaseRemoteConfig();
        fetchData();
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Const.REGISTRATION_COMPLETE)) {
                    displayFirebaseRegId();
                } else if (intent.getAction().equals(Const.PUSH_NOTIFICATION)) {
                    Glide.with(getApplicationContext())
                            .load(intent.getStringExtra(MyFirebaseMessagingService.EXTRA_KEY_MESSSAGE))
                            .into(imgShow);
                }
            }
        };
        displayFirebaseRegId();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Const.REGISTRATION_COMPLETE));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Const.PUSH_NOTIFICATION));

        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                fetchData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_subscribe)
    final void subscribeButtonClicked() {
        FirebaseMessaging.getInstance().subscribeToTopic(Const.TOPIC_UPDATE);
        txtSubscribeTo.setText(getString(R.string.subscribe_on_label));
        txtSubscribeTo.setTextColor(Color.GREEN);
    }

    @OnClick(R.id.btn_unsubscribe)
    final void unSubscribeButtonClicked() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Const.TOPIC_UPDATE);
        txtSubscribeTo.setText(getString(R.string.subscribe_off_label));
        txtSubscribeTo.setTextColor(Color.RED);
    }

    private void initFirebaseRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings remoteConfigSettings
                = new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build();
        mFirebaseRemoteConfig.setConfigSettings(remoteConfigSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.defaults);
    }

    private void fetchData() {
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        progressBar.setVisibility(View.VISIBLE);
        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                            updateView();
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.error_message_fetch),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Const.SHARED_PREF, 0);
        String regId = pref.getString(MyFirebaseInstanceIDService.PREF_FCM_TOKEN_KEY, null);

        Timber.d( "Firebase reg id: " + regId);

        if (!TextUtils.isEmpty(regId))
            Timber.d("Firebase Reg Id: " + regId);
        else
            Timber.e("Firebase Reg Id is not received yet!");
    }

    private void updateView() {
        String colorCode = mFirebaseRemoteConfig.getString(REMOTE_CONFIG_KEY);
        btnSubscribe.setBackgroundColor(Color.parseColor(colorCode));
        btnUnsubscribe.setBackgroundColor(Color.parseColor(colorCode));
    }
}
