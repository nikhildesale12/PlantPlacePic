package com.ibin.plantplacepic.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.adapter.MyRecyclerViewAdapter;
import com.ibin.plantplacepic.bean.DataObject;
import com.ibin.plantplacepic.bean.InformationResponseBean;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.services.GetAllUploadedDataService;
import com.ibin.plantplacepic.utility.Constants;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.ibin.plantplacepic.activities.SpeciesAroundYouActivity.view;

public class MountingBoardActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "MountingBoardActivity";
    private TextView textMessageMb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mounting_board);
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        initViews();
        if(Constants.isNetworkAvailable(MountingBoardActivity.this)){
            SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
            String userId = prefs.getString(Constants.KEY_USERID, "0");
            final ProgressDialog dialog = new ProgressDialog(MountingBoardActivity.this);
                dialog.setMessage("Please Wait...");
                dialog.setIndeterminate(false);
                dialog.setCancelable(false);
                dialog.show();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
//                .client(okHttpClient)
                    .build();
            ApiService service = retrofit.create(ApiService.class);
            Call<InformationResponseBean> call = service.getAllSpeciesDetailMountingBoard(userId);
            call.enqueue(new Callback<InformationResponseBean>() {
                @Override
                public void onResponse(Call<InformationResponseBean> call, Response<InformationResponseBean> response) {
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    if (response != null && response.body() != null) {
                        if (response.body().getSuccess().toString().trim().equals("1")) {
                            if(response.body().getInformation() != null){
                                textMessageMb.setVisibility(View.GONE);
                                mRecyclerView.setLayoutManager(mLayoutManager);
                                mAdapter = new MyRecyclerViewAdapter(response.body().getInformation(),MountingBoardActivity.this);
                                mRecyclerView.setAdapter(mAdapter);
                            }else{
                                Log.d("NO records", "NO records --->");
                                textMessageMb.setVisibility(View.VISIBLE);
                                textMessageMb.setText("No Records Found");
                            }
                        } else if (response.body().getSuccess().toString().trim().equals("0")) {
                            Log.d("NO records", "NO records --->");
                            textMessageMb.setVisibility(View.VISIBLE);
                            textMessageMb.setText("No Records Found");
                        } else {
                            Log.d("Technical Error !!!", "Technical Error !!! --->");
                            textMessageMb.setVisibility(View.VISIBLE);
                            textMessageMb.setText("Technical Error");
                        }
                    }else {
                        Log.d("Technical Error !!!", "Technical Error !!! --->");
                        textMessageMb.setVisibility(View.VISIBLE);
                        textMessageMb.setText("Technical Error");
                    }
                }
                @Override
                public void onFailure(Call<InformationResponseBean> call, Throwable t) {
                    Log.d("error", "error :--->"+t.toString());
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    textMessageMb.setVisibility(View.VISIBLE);
                    textMessageMb.setText("Error");
                }
            });

            /*((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter
                    .MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    Log.i(LOG_TAG, " Clicked on Item " + position);
                }
            });*/

        }else{
            textMessageMb.setVisibility(View.VISIBLE);
            textMessageMb.setText("No Internet Available");
        }

    }

    private void initViews() {
        textMessageMb = (TextView) findViewById(R.id.textMessageMb);
        mRecyclerView = (RecyclerView) findViewById(R.id.mounting_board_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
