package com.ibin.plantplacepic.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.ibin.plantplacepic.R;

public class AboutActivity extends AppCompatActivity {
    Button buttonSkipAbout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        buttonSkipAbout = (Button) findViewById(R.id.buttonSkipAbout);
        buttonSkipAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentKsip = new Intent(AboutActivity.this,TermsActivity.class);
                startActivity(intentKsip);
                finish();
            }
        });

    }
}
