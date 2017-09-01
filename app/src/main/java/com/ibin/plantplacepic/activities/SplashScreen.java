package com.ibin.plantplacepic.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.utility.Constants;

public class SplashScreen extends AppCompatActivity {
    TextView versionName ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        versionName = (TextView) findViewById(R.id.version_name);
        PackageInfo pInfo = null;
        try {
            pInfo = SplashScreen.this.getPackageManager().getPackageInfo(SplashScreen.this.getPackageName(), 0);
            String version = pInfo.versionName;
            versionName.setText("Version : "+version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Thread init = new Thread() {
            public void run() {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally
                {
                    SharedPreferences sharedPreferences = SplashScreen.this.getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE);
                    boolean login = sharedPreferences.getBoolean(Constants.KEY_IS_LOGIN, false);
                    if (login) {
                        Intent i = new Intent(SplashScreen.this,AboutActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Intent i = new Intent(SplashScreen.this,SignInActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
            }
        };
        init.start();
    }
}
