package com.ibin.plantplacepic.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.adapter.ReviewAdapter;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.InformationResponseBean;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.DividerItemDecoration;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SpeciesByNameActivity extends AppCompatActivity {

    Button buttonSearch;
    Button buttonBack;
    public RecyclerView recyclerView;
    public ReviewAdapter mAdapter;
    public static String speciesName="";
    AutoCompleteTextView autoEnterSpeciesName;
    public ArrayList<String> speciesList;
    public static List<Information> mainDataList = null;
    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_by_name);
        intitViews();
//      SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
//      userId = prefs.getString("USERID", "0");
        mainDataList = new ArrayList<>();
        if (Constants.isNetworkAvailable(SpeciesByNameActivity.this)) {
           callServiceToGetSpeciesNames();
        }else {
                speciesList = new ArrayList<>();
                speciesList = databaseHelper.getSpeciesNames();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,speciesList);
                autoEnterSpeciesName.setThreshold(1);
                autoEnterSpeciesName.setAdapter(adapter);
        }

        autoEnterSpeciesName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               // callServiceToGetSpeciesDetailByName(ACTtEnterSpeciesName.getText().toString());
//                view = getCurrentFocus();
//                if (view != null) {
//                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                }
                Intent intent=new Intent(SpeciesByNameActivity.this,SpeciesInfoActivity.class);
                intent.putExtra("speciesNameSearch", autoEnterSpeciesName.getText().toString().trim());
                //intent.putParcelableArrayListExtra("mainDataList", (ArrayList<? extends Parcelable>) mainDataList);
                startActivity(intent);

            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SpeciesByNameActivity.this,SpeciesInfoActivity.class);
                startActivity(intent);
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

    private void callServiceToGetSpeciesNames() {
//        final ProgressDialog dialog = new ProgressDialog(SpeciesByNameActivity.this);
//        dialog.setMessage("Please Wait...");
//        dialog.setIndeterminate(false);
//        dialog.setCancelable(false);
//        dialog.show();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        ApiService service = retrofit.create(ApiService.class);
        Call<InformationResponseBean> call = service.getAllSpeciesDetail();
        call.enqueue(new Callback<InformationResponseBean>() {
            @Override
            public void onResponse(Call<InformationResponseBean> call, Response<InformationResponseBean> response) {
//                if (dialog != null && dialog.isShowing()) {
//                    dialog.dismiss();
//                }
                if (response != null && response.body() != null) {
                    if (response.body().getSuccess().toString().trim().equals("1")) {
                        if(response.body().getInformation() != null && response.body().getInformation().size()>0){
                            mainDataList = response.body().getInformation();
                            for(int i = 0 ;i<response.body().getInformation().size();i++){
                                if(!speciesList.contains(response.body().getInformation().get(i).getSpecies().trim())){
                                   if(response.body().getInformation().get(i).getSpecies().trim().length()>0){
                                       speciesList.add(response.body().getInformation().get(i).getSpecies().trim());
                                   }
                               }
                           }
                           ArrayAdapter<String> adapter = new ArrayAdapter<String>(SpeciesByNameActivity.this,android.R.layout.simple_list_item_1, speciesList);
                           autoEnterSpeciesName.setThreshold(1);
                           autoEnterSpeciesName.setAdapter(adapter);
                        }
                    }
                    if (response.body().getSuccess().toString().trim().equals("0")) {
                    }
                }
            }
            @Override
            public void onFailure(Call<InformationResponseBean> call, Throwable t) {
                //Log.d("resp
//                if (dialog != null && dialog.isShowing()) {
//                    dialog.dismiss();
//                }
            }
        });
    }

    private void callServiceToGetSpeciesDetailByName(String speciesName) {
        final ProgressDialog dialog = new ProgressDialog(SpeciesByNameActivity.this);
        dialog.setMessage("Please Wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        ApiService service = retrofit.create(ApiService.class);
        Call<InformationResponseBean> call = service.getAllSpeciesDetailBySpeciesName(speciesName);
        call.enqueue(new Callback<InformationResponseBean>() {
            @Override
            public void onResponse(Call<InformationResponseBean> call, Response<InformationResponseBean> response) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                if (response != null && response.body() != null) {
                    if (response.body().getSuccess().toString().trim().equals("1")) {
                        if(response.body().getInformation() != null && response.body().getInformation().size()>0){
                            mAdapter = new ReviewAdapter(response.body().getInformation(),SpeciesByNameActivity.this,"SearchByName");
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(mLayoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            recyclerView.addItemDecoration(new DividerItemDecoration(SpeciesByNameActivity.this, LinearLayoutManager.VERTICAL));
                            recyclerView.setAdapter(mAdapter);
                        }
                    }
                    if (response.body().getSuccess().toString().trim().equals("0")) {

                    }
                }
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
            @Override
            public void onFailure(Call<InformationResponseBean> call, Throwable t) {
                //Log.d("resp
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
    }

    private void intitViews() {
        speciesList = new ArrayList<>();
        databaseHelper = DatabaseHelper.getDatabaseInstance(SpeciesByNameActivity.this);
        buttonSearch=(Button)findViewById(R.id.buttonSearch);
        buttonBack=(Button)findViewById(R.id.buttonBack);
        autoEnterSpeciesName=(AutoCompleteTextView)findViewById(R.id.ACTtEnterSpeciesName);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_all_species);
    }
}
