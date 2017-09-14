package com.ibin.plantplacepic.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ibin.plantplacepic.R;

public class SpeciesByNameActivity extends AppCompatActivity {

    Button buttonSearch;
    Button buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_by_name);
        intitViews();
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(SpeciesByNameActivity.this,SpeciesSearchActivity.class);
                startActivity(i);
                finish();

            }
        });
    }

    private void intitViews() {
        buttonSearch=(Button)findViewById(R.id.buttonSearch);
        buttonBack=(Button)findViewById(R.id.buttonBack);
    }
}
