package com.ibin.plantplacepic.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.utility.Constants;

public class SpeciesSearchActivity extends AppCompatActivity {

    RelativeLayout buttonSpeciesAroundYou;
    RelativeLayout buttonSpeciesByName;
    Button buttonBackSpeciesSearch;
   // public static String speciesImage="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_search);
        initViews();
        buttonSpeciesAroundYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isGPSEnabled()){
                    showSettingsAlert();
                }else {
                    Intent intent = new Intent(SpeciesSearchActivity.this, SpeciesAroundYouActivity.class);
                    startActivity(intent);
                    //finish();
                }
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
                intent1.putExtra("uploadedCount", Constants.FROM_);
                startActivity(intent1);
                finish();
            }
        });
    }

    private void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SpeciesSearchActivity.this);
        alertDialog.setTitle("GPS Is Disable");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
    protected boolean isGPSEnabled(){
        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean GPSStatus = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return GPSStatus;
    }
    private void initViews() {
        buttonSpeciesAroundYou=(RelativeLayout)findViewById(R.id.buttonSpeciesAroundYou);
        buttonSpeciesByName=(RelativeLayout)findViewById(R.id.buttonSpeciesByName);
        buttonBackSpeciesSearch=(Button)findViewById(R.id.buttonBackSpeciesSearch);
    }

    @Override
    public void onBackPressed() {
        Intent intentdASH = new Intent(this, Dashboard.class);
        intentdASH.putExtra("uploadedCount", Constants.FROM_);
        startActivity(intentdASH);
        finish();
    }
}
