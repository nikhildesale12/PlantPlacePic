package com.ibin.plantplacepic.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.adapter.ReviewAdapter;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.InformationResponseBean;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.DividerItemDecoration;
import com.ibin.plantplacepic.utility.RecyclerItemClickListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReviewMyUpload extends AppCompatActivity {

    private List<Information> dataListSameSpecies = new ArrayList<>();
    public static RecyclerView recyclerView;
    public static ReviewAdapter mAdapter;
    private String userId="";
    TextView textNoRecords;
    TextView textSpeciesAlbum;
    Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_my_upload);
        textNoRecords = (TextView) findViewById(R.id.textNoRecords);
        textSpeciesAlbum = (TextView) findViewById(R.id.textSpeciesAlbum);
        back = (Button) findViewById(R.id.back);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE);
        //userId = prefs.getString("USERID", "0");
        //executeGetMyReviewData(userId);
        if(getIntent() != null){
            dataListSameSpecies = getIntent().getExtras().getParcelableArrayList("dataListSameSpecies");
            textSpeciesAlbum.setText(dataListSameSpecies.get(0).getSpecies());
        }
        mAdapter = new ReviewAdapter(dataListSameSpecies,ReviewMyUpload.this,"");
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(ReviewMyUpload.this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(ReviewMyUpload.this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
//                        Intent intent = new Intent(ReviewMyUpload.this,LargeZoomActivity.class);
//                        intent.putExtra("imageName",reviewList.get(position).getImages());
//                        intent.putExtra("tag",reviewList.get(position).getTag());
//                        startActivity(intent);
                    }
                })
        );
    }

    /*private void executeGetMyReviewData(String userId) {
            final ProgressDialog dialog = new ProgressDialog(ReviewMyUpload.this);
            dialog.setMessage("Please Wait...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
           // Gson gson = new GsonBuilder().setLenient().create();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
            ApiService service = retrofit.create(ApiService.class);
            Call<InformationResponseBean> call = service.downloadDataById(userId);
            call.enqueue(new Callback<InformationResponseBean>() {
                @Override
                public void onResponse(Call<InformationResponseBean> call, Response<InformationResponseBean> response) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (response != null && response.body() != null) {
                        if (response.body().getSuccess().toString().trim().equals("1")) {
                            if(response.body().getInformation() != null){
                                for(int i = 0 ;i<response.body().getInformation().size();i++){
                                    Information information = new Information();
                                    information.setUserId(response.body().getInformation().get(i).getUserId());
                                    information.setImages(response.body().getInformation().get(i).getImages());
                                    information.setSpecies(response.body().getInformation().get(i).getSpecies());
                                    information.setRemark(response.body().getInformation().get(i).getRemark());
                                    information.setTag(response.body().getInformation().get(i).getTag());
                                    information.setStatus(response.body().getInformation().get(i).getStatus());
                                    information.setTitle(response.body().getInformation().get(i).getTitle());
                                    information.setLat(response.body().getInformation().get(i).getLat());
                                    information.setLng(response.body().getInformation().get(i).getLng());
                                    information.setAddress(response.body().getInformation().get(i).getAddress());
                                    information.setCrop(response.body().getInformation().get(i).getCrop());
                                    information.setTime(response.body().getInformation().get(i).getTime());
                                    dataListSameSpecies.add(information);
                                }
                            }else{
                                dataListSameSpecies = new ArrayList<Information>();
                            }

                            mAdapter = new ReviewAdapter(dataListSameSpecies,ReviewMyUpload.this);
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(mLayoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            recyclerView.addItemDecoration(new DividerItemDecoration(ReviewMyUpload.this, LinearLayoutManager.VERTICAL));
                            recyclerView.setAdapter(mAdapter);
                        } else if (response.body().getSuccess().toString().trim().equals("0")) {
                            Constants.dispalyDialogInternet(ReviewMyUpload.this,"Result","No Records Found",true);
                            textNoRecords.setVisibility(View.VISIBLE);
                            //backFromReview.setVisibility(View.VISIBLE);
                        } else {
                            Constants.dispalyDialogInternet(ReviewMyUpload.this,"Error","Technical Error !!!",false);
                        }
                    }else {
                        Constants.dispalyDialogInternet(ReviewMyUpload.this,"Error","Technical Error !!!",false);
                    }
                }
                @Override
                public void onFailure(Call<InformationResponseBean> call, Throwable t) {
                    //Log.d("resp
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Constants.dispalyDialogInternet(ReviewMyUpload.this,"Result",t.toString(),false);
                }
            });
        }*/
}
