package com.ibin.plantplacepic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ibin.plantplacepic.activities.AboutActivity;
import com.ibin.plantplacepic.activities.Dashboard;
import com.ibin.plantplacepic.activities.TermsActivity;

public class NotificationActivity extends AppCompatActivity {

    Button buttonBackNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        buttonBackNotification = (Button) findViewById(R.id.buttonBackNotification);

        buttonBackNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intentKsip = new Intent(NotificationActivity.this,Dashboard.class);
                    startActivity(intentKsip);
                    finish();
                }
        });


    }
}
