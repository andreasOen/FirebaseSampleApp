package com.example.kk_mb_007.firebasesampleandroid;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String REMOTE_CONFIG_KEY = "background_color";

    @BindView(R.id.img_show)
    ImageView imgShow;

    @BindView(R.id.container)
    LinearLayout container;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private long cacheExpiration = 3600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initFirebaseRemoteConfig();
        fetchData();
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

    private void updateView() {
        String colorCode = mFirebaseRemoteConfig.getString(REMOTE_CONFIG_KEY);
        container.setBackgroundColor(Color.parseColor(colorCode));
    }
}
