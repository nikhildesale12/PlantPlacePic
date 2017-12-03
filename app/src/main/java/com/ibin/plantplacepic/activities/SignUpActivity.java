package com.ibin.plantplacepic.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.bean.UserDetailResponseBean;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;

import retrofit2.Retrofit;
import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignUpActivity extends AppCompatActivity {

    Button buttonSignUp;
    EditText editTextUserSignUp ;
    EditText editTextPassSignUp;
    EditText editTextConfirmPassSignUp;
    ProgressDialog dialog;
    public static final int RequestPermissionCode = 1;
    private static final String TAG = SignUpActivity.class.getSimpleName();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sign_up);
        initViews();

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String USER = editTextUserSignUp.getText().toString();
                String PASS = editTextPassSignUp.getText().toString();
                String PASSConfirm = editTextConfirmPassSignUp.getText().toString();
                if (Constants.isNetworkAvailable(SignUpActivity.this)) {
                    if(USER.length() == 0){
                        editTextUserSignUp.requestFocus();
                        editTextUserSignUp.setError("Please Enter Email id");
                    }else if(PASS.length() == 0){
                        editTextPassSignUp.requestFocus();
                        editTextPassSignUp.setError("Please Enter Password");
                    }else if(PASSConfirm.length() == 0){
                        editTextConfirmPassSignUp.requestFocus();
                        editTextConfirmPassSignUp.setError("Please Confirm Password");
                    }else if(!Constants.isValidEmailAddress(USER)){
                        editTextUserSignUp.requestFocus();
                        editTextUserSignUp.setError("Invalid Email Id");
                    }else if(PASS.length()<6){
                        editTextPassSignUp.requestFocus();
                        editTextPassSignUp.setError("Password should be minimum 6 character");
                    }else if(PASSConfirm.length() < 6){
                        editTextConfirmPassSignUp.requestFocus();
                        editTextConfirmPassSignUp.setError("Password should be minimum 6 character");
                    } else if (!PASS.equals(PASSConfirm)) {
                        editTextConfirmPassSignUp.requestFocus();
                        editTextConfirmPassSignUp.setError("Password Mismatch");
                    } else{
                        executeRegisterService(USER, PASS, "N");
                    }

//                    if(USER.length()>0 && PASS.length()>0 &&PASSConfirm.length()>0) {
//                        if(!Constants.isValidEmailAddress(USER)){
//                            Toast.makeText(SignUpActivity.this,"Please enter valid email address",Toast.LENGTH_LONG).show();
//                        }else if(PASS.length()<6){
//                            Toast.makeText(SignUpActivity.this,"Password should be minimum 6 character",Toast.LENGTH_LONG).show();
//                        }else if(PASSConfirm.length() < 6) {
//                            Toast.makeText(SignUpActivity.this,"Confirm Password should be minimum 6 character",Toast.LENGTH_LONG).show();
//                        } else if (PASS.equals(PASSConfirm)) {
//                            executeRegisterService(USER, PASS, "N");
//                        }else{
//                            Constants.dispalyDialogInternet(SignUpActivity.this, "Invalid Credentials", "Password and confirm password does not match...", true,false);
//                        }
//                    }
//                    else {
//                        Constants.dispalyDialogInternet(SignUpActivity.this, "Invalid Credentials", "Please enter signup details...", true,false);
//                    }
                }else {
                    Constants.dispalyDialogInternet(SignUpActivity.this, "Internet Connection Issue", "Please check internet connection ...", false ,false);
                }
            }
        });
    }
    private void executeRegisterService(final String USER, String PASS, String PASSConfirm) {
        dialog = new ProgressDialog(SignUpActivity.this);
        dialog.setMessage("Please Wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        ApiService service = retrofit.create(ApiService.class);
        Call<LoginResponse> call = service.registerUserService(USER, PASS,PASSConfirm);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response != null && response.body() != null) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (response.body().getSuccess().toString().trim().equals("1")) {
                        SharedPreferences.Editor editor1 = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE).edit();
                        editor1.putString(Constants.KEY_USERNAME, USER);
                        editor1.putString(Constants.KEY_USERID, response.body().getUserId());
                        editor1.putBoolean(Constants.KEY_IS_LOGIN,false);
                        editor1.commit();

                        getUserDetailPopup(response.body().getUserId());

                    } else if (response.body().getSuccess().toString().trim().equals("0")) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(SignUpActivity.this,"Result",response.body().getResult(),true,false);
                    } else {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(SignUpActivity.this,"Error","Technical Error !!!",false,false);
                    }
                }else{
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Constants.dispalyDialogInternet(SignUpActivity.this,"Error","Technical Error !!!",false,false);
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                //Log.d("resp
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                Constants.dispalyDialogInternet(SignUpActivity.this,"Result",t.toString(),false,false);
            }
        });
    }

    private void getUserDetailPopup(final String userId){
        final Dialog userDetailPopup = new Dialog(SignUpActivity.this);
        userDetailPopup.requestWindowFeature(Window.FEATURE_NO_TITLE);
        userDetailPopup.setContentView(R.layout.popup_user_detail);
        userDetailPopup.setCanceledOnTouchOutside(false);
        final EditText editFirstName = (EditText) userDetailPopup.findViewById(R.id.editFirstName);
        final EditText editMiddleName = (EditText) userDetailPopup.findViewById(R.id.editMiddleName);
        final EditText editLastName = (EditText) userDetailPopup.findViewById(R.id.editLastName);
        final EditText editOccupation = (EditText) userDetailPopup.findViewById(R.id.editOccupation);
        final EditText editMobile = (EditText) userDetailPopup.findViewById(R.id.editMobile);
        Button submitBtton = (Button) userDetailPopup.findViewById(R.id.submitUserInfo);
        submitBtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editFirstName.getText().toString().length() == 0 ){
                    editFirstName.requestFocus();
                    editFirstName.setError("Please Enter First Name");
                }else if(editLastName.getText().toString().length() == 0 ){
                    editLastName.requestFocus();
                    editLastName.setError("Please Enter Last Name");
                }else if(editOccupation.getText().toString().length() == 0 ){
                    editOccupation.requestFocus();
                    editOccupation.setError("Please Enter Occupation");
                }else{
                    userDetailPopup.dismiss();
                    updateUserInformation(userId,editFirstName.getText().toString(),editMiddleName.getText().toString(),editLastName.getText().toString(),
                            editOccupation.getText().toString(),editMobile.getText().toString());
                }
            }
        });
        userDetailPopup.show();
    }

    private void updateUserInformation(String userId,String firstName,String middleName,String LastName, String occupation,String mobileNum){
        //update Info
        dialog = new ProgressDialog(SignUpActivity.this);
        dialog.setMessage("Please Wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        ApiService service = retrofit.create(ApiService.class);
        Call<UserDetailResponseBean> call = service.updateUserDetail(userId,firstName, middleName,LastName,occupation,mobileNum);
        call.enqueue(new Callback<UserDetailResponseBean>() {
            @Override
            public void onResponse(Call<UserDetailResponseBean> call, Response<UserDetailResponseBean> response) {
                if (response != null && response.body() != null) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (response.body().getSuccess().toString().trim().equals("1")) {
                        SharedPreferences.Editor editor1 = getSharedPreferences(Constants.MY_PREFS_USER_INFO, MODE_PRIVATE).edit();
                        editor1.putString(Constants.KEY_FIRSTNAME, response.body().getFirstName());
                        editor1.putString(Constants.KEY_MIDDLENAME, response.body().getMiddleName());
                        editor1.putString(Constants.KEY_LASTNAME, response.body().getLastName());
                        editor1.putString(Constants.KEY_OCCUPATION, response.body().getOccupation());
                        editor1.putString(Constants.KEY_MOBILE, response.body().getMobile());
                        editor1.commit();
                        Intent intent = new Intent(SignUpActivity.this, AboutActivity.class);
                        startActivity(intent);
                        finish();

                    } else if (response.body().getSuccess().toString().trim().equals("0")) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(SignUpActivity.this,"Result",response.body().getResult(),true,false);
                    } else {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(SignUpActivity.this,"Error","Technical Error !!!",false,false);
                    }
                }else{
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Constants.dispalyDialogInternet(SignUpActivity.this,"Error","Technical Error !!!",false,false);
                }
            }
            @Override
            public void onFailure(Call<UserDetailResponseBean> call, Throwable t) {
                //Log.d("resp
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                Constants.dispalyDialogInternet(SignUpActivity.this,"Result",t.toString(),false,false);
            }
        });
    }

    private void initViews() {
        buttonSignUp = (Button)findViewById(R.id.buttonRegister);
        editTextUserSignUp = (EditText) findViewById(R.id.editTextUserSignUp);
        editTextPassSignUp = (EditText) findViewById(R.id.editTextPassSignUp);
        editTextConfirmPassSignUp = (EditText) findViewById(R.id.editTextConfirmPassSignUp);
    }
}
