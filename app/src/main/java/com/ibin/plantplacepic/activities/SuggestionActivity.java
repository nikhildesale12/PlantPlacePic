package com.ibin.plantplacepic.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

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

public class SuggestionActivity extends AppCompatActivity {

    Button button_suggestion_submit;
    EditText editTextEmailId;
    EditText editTextContact;
    EditText editTextSuggestion;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        initViews();
        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
        String username = prefs.getString("USERNAME", "0");
        editTextEmailId.setText(username);
        button_suggestion_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextEmailId.getText().toString().trim().length() == 0){
                    editTextEmailId.requestFocus();
                    editTextEmailId.setError("Please Enter First EmailID");
                }else if(editTextContact.getText().toString().trim().length() == 0) {
                    editTextContact.requestFocus();
                    editTextContact.setError("Please Enter First Contact number");
                }else if(editTextSuggestion.getText().toString().trim().length()==0){
                    editTextSuggestion.requestFocus();
                    editTextSuggestion.setError("Please Enter Your Suggestion here");
                }else{
                    callSuggestionService();
                }
            }
        });
    }

    private void callSuggestionService() {
        final ProgressDialog dialog = new ProgressDialog(SuggestionActivity.this);
        dialog.setMessage("Please Wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        ApiService service = retrofit.create(ApiService.class);
        Call<LoginResponse> call = service.postSuggestionData(editTextEmailId.getText().toString(),editTextContact.getText().toString(),editTextSuggestion.getText().toString());
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if(response != null && response.body() != null ){
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    if(response.body().getSuccess().toString().trim().equals("1")) {
                        Intent i=new Intent(SuggestionActivity.this,Dashboard.class);
                        i.putExtra("uploadedCount",Constants.FROM_);
                        startActivity(i);
                        finish();
                    }else  if(response.body().getSuccess().toString().trim().equals("0")) {
                        if(dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(SuggestionActivity.this,"Result",response.body().getResult(),true,false);
                    }else {
                        if(dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(SuggestionActivity.this,"Error","Technical Error !!!",false,false);
                    }
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                //Log.d("response :","");
                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                Constants.dispalyDialogInternet(SuggestionActivity.this,"Result",t.toString(),false,false);
            }
        });

    }

    private void initViews() {
        button_suggestion_submit=(Button)findViewById(R.id.button_suggestion_submit);
        editTextEmailId = (EditText) findViewById(R.id.edittext_emailid);
        editTextContact = (EditText) findViewById(R.id.edittext_contact);
        editTextSuggestion=(EditText)findViewById(R.id.edittext_suggestion);
        InputMethodManager inputMethodManager = (InputMethodManager) SuggestionActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        inputMethodManager.hideSoftInputFromWindow(SuggestionActivity.this.getCurrentFocus().getWindowToken(), 0);
        inputMethodManager.hideSoftInputFromWindow(editTextEmailId.getWindowToken(), 0);
    }
}
