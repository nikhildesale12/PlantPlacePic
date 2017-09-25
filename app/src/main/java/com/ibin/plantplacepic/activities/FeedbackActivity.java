package com.ibin.plantplacepic.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FeedbackActivity extends AppCompatActivity {

    Button button_feedback_submit;
    RatingBar ratingBar;
    EditText editTextUserName;
    EditText editTextComment;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        initViews();
        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
        String username = prefs.getString("USERNAME", "0");
        editTextUserName.setText(username);
        button_feedback_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i=new Intent(FeedbackActivity.this,Dashboard.class);
//                startActivity(i);
                  callFeedbackService();
            }
        });
    }

    private void callFeedbackService(){
        String rating=String.valueOf(ratingBar.getRating());
        final ProgressDialog dialog = new ProgressDialog(FeedbackActivity.this);
        dialog.setMessage("Please Wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        ApiService service = retrofit.create(ApiService.class);
        Call<LoginResponse> call = service.postFeedBackData(editTextUserName.getText().toString(),editTextComment.getText().toString(),rating);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if(response != null && response.body() != null ){
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    if(response.body().getSuccess().toString().trim().equals("1")) {
                        Intent i=new Intent(FeedbackActivity.this,Dashboard.class);
                        startActivity(i);
                        finish();
                    }else  if(response.body().getSuccess().toString().trim().equals("0")) {
                        if(dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(FeedbackActivity.this,"Result",response.body().getResult(),true,false);
                    }else {
                        if(dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(FeedbackActivity.this,"Error","Technical Error !!!",false,false);
                    }
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                //Log.d("response :","");
                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                Constants.dispalyDialogInternet(FeedbackActivity.this,"Result",t.toString(),false,false);
            }
        });
    }


    private void initViews() {
        button_feedback_submit=(Button)findViewById(R.id.button_feedback_submit);
        ratingBar=(RatingBar) findViewById(R.id.ratingBar);
        editTextUserName = (EditText) findViewById(R.id.edittext_username);
        editTextComment = (EditText) findViewById(R.id.edittext_comment);
    }
}