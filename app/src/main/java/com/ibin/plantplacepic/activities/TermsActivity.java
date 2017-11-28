package com.ibin.plantplacepic.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ibin.plantplacepic.R;

public class TermsActivity extends AppCompatActivity {
    Button buttonAcceptsTerms;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        buttonAcceptsTerms = (Button) findViewById(R.id.buttonAcceptsTerms);
        buttonAcceptsTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentTerms = new Intent(TermsActivity.this,Dashboard.class);
                intentTerms.putExtra("uploadedCount","BYSERVICE");
                startActivity(intentTerms);
                finish();
            }
        });
    }
}
