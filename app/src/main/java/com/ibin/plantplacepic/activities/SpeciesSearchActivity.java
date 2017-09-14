package com.ibin.plantplacepic.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.ibin.plantplacepic.R;

public class SpeciesSearchActivity extends AppCompatActivity {

    RelativeLayout buttonSpeciesAroundYou;
    RelativeLayout buttonSpeciesByName;
    Button buttonBackSpeciesSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_search);
        initViews();
        buttonSpeciesAroundYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SpeciesSearchActivity.this,SpeciesAroundYouActivity.class);
                startActivity(intent);
                //finish();
            }
        });
        buttonSpeciesByName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(SpeciesSearchActivity.this,SpeciesByNameActivity.class);
                startActivity(intent1);
                //finish();
            }
        });
        buttonBackSpeciesSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(SpeciesSearchActivity.this,Dashboard.class);
                startActivity(intent1);
                //finish();
            }
        });
    }

    private void initViews() {
        buttonSpeciesAroundYou=(RelativeLayout)findViewById(R.id.buttonSpeciesAroundYou);
        buttonSpeciesByName=(RelativeLayout)findViewById(R.id.buttonSpeciesByName);
        buttonBackSpeciesSearch=(Button)findViewById(R.id.buttonBackSpeciesSearch);
    }
}
