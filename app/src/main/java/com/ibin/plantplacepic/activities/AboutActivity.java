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
    boolean fromDashboard = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        buttonSkipAbout = (Button) findViewById(R.id.buttonSkipAbout);
        if(getIntent() != null){
            if(getIntent().getStringExtra("from") != null){
                if(getIntent().getStringExtra("from").equals("dashboard")){
                    buttonSkipAbout.setText("BACK");
                    fromDashboard = true;
                }
            }
        }
        buttonSkipAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fromDashboard){
                    AboutActivity.this.finish();
                }else{
                    Intent intentKsip = new Intent(AboutActivity.this,TermsActivity.class);
                    startActivity(intentKsip);
                    finish();
                }
            }
        });
    }
}
