package com.ibin.plantplacepic.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.bean.UserDetailResponseBean;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class LoginMainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    CallbackManager callbackManager;
    TextView toLogin ;
    TextView toRegister;
    ImageView signinGmail;
    ImageView signinFacebook;
    CheckBox keepMeSignin;
    String personPhotoUrl="";
    ProgressDialog dialog;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 007;
    private static final String TAG = LoginMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);
        initViews();

        if(android.os.Build.VERSION.SDK_INT >= Constants.API_LEVEL_23){
            if(checkPermission()){
            }else {
                requestPermission();
            }
        }

        keepMeSignin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor2 = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE).edit();
                if(isChecked){
                    editor2.putBoolean(Constants.KEY_IS_LOGIN,true);
                }else{
                    editor2.putBoolean(Constants.KEY_IS_LOGIN,false);
                }
                editor2.commit();
            }
        });

        String lgn="Login";
        SpannableString content = new SpannableString(lgn);
        content.setSpan(new UnderlineSpan(), 0, lgn.length(), 0);
        toLogin.setText(content);
        String rgstr="Register";
        SpannableString content1 = new SpannableString(rgstr);
        content1.setSpan(new UnderlineSpan(), 0, rgstr.length(), 0);
        toRegister.setText(content1);

        toLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginMainActivity.this,SignInActivity.class);
                startActivity(i);
                finish();
            }
        });

        toRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginMainActivity.this,SignUpActivity.class);
                startActivity(i);
                finish();
            }
        });

        signinGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Constants.isNetworkAvailable(LoginMainActivity.this)){
                    signInGoogle();
                }else{
                    Constants.dispalyDialogInternet(LoginMainActivity.this,"Internet Connection Issue","Please check internet connection ...",false,false);
                }
            }
        });

        signinFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Constants.isNetworkAvailable(LoginMainActivity.this)){
                    //signinViaFacebook();
                    Toast.makeText(LoginMainActivity.this, "Coming soon", Toast.LENGTH_SHORT).show();
                }else{
                    Constants.dispalyDialogInternet(LoginMainActivity.this,"Internet Connection Issue","Please check internet connection ...",false,false);
                }
            }
        });
    }

    /*private void signinViaFacebook() {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(LoginMainActivity.this, "Login Success\n"+loginResult.getAccessToken(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginMainActivity.this, "Login Cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginMainActivity.this, "Login Error", Toast.LENGTH_SHORT).show();

            }
        });

    }
*/
    private void signInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       // callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            Log.e(TAG, "display name: " + acct.getDisplayName());

            String personName = acct.getDisplayName();
            if(acct.getPhotoUrl() != null){
                personPhotoUrl = acct.getPhotoUrl().toString();
            }
            String email = "";
            email = acct.getEmail();
            Log.e(TAG, "Name: " + personName + ", email: " + email + ", Image: " + personPhotoUrl);
            if(personPhotoUrl.length()>0){
                Picasso.with(LoginMainActivity.this)
                        .load(personPhotoUrl)
                        .into(getTarget());
            }
            if(email.length() > 0){
                if(Constants.isNetworkAvailable(LoginMainActivity.this)){
                    if(email.length() > 0){
                        executeLoginService(email,"","Y");
                    }else{
                        Constants.dispalyDialogInternet(LoginMainActivity.this,"Invalid Credentials","Invalid Email ID...",true,false);
                    }
                }else{
                    Constants.dispalyDialogInternet(LoginMainActivity.this,"Internet Connection Issue","Please check internet connection ...",false,false);
                }
            }
        } else {
            Toast.makeText(LoginMainActivity.this,"unauthenticated user due to "+result.getStatus(),Toast.LENGTH_SHORT).show();
        }
    }

    private void executeLoginService(final String USER , String PASS , String GOOG_ID){
        dialog = new ProgressDialog(LoginMainActivity.this);
        dialog.setMessage("Please Wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
        ApiService service = retrofit.create(ApiService.class);
        Call<LoginResponse> call = service.loginService(USER,PASS,GOOG_ID);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if(response != null && response.body() != null ){
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    if(response.body().getSuccess().toString().trim().equals("1")) {
                        //dispalyToast(response.body().getResult());
                        SharedPreferences.Editor editor1 = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE).edit();
                        editor1.putString(Constants.KEY_USERNAME, USER);
                        editor1.putString(Constants.KEY_USERID, response.body().getUserId());
                        editor1.putString(Constants.KEY_PHOTO, personPhotoUrl);
                        editor1.putBoolean(Constants.KEY_IS_LOGIN,true);
                        /*if(keepMeSignin.isChecked()){
                            editor1.putBoolean(Constants.KEY_IS_LOGIN,true);
                        }else{
                            editor1.putBoolean(Constants.KEY_IS_LOGIN,false);
                        }*/
                        editor1.commit();
                        if(response.body().getIsNameAvailable().equals("YES")){
                            SharedPreferences.Editor editor2 = getSharedPreferences(Constants.MY_PREFS_USER_INFO, MODE_PRIVATE).edit();
                            editor2.putString(Constants.KEY_FIRSTNAME, response.body().getFirstName());
                            editor2.putString(Constants.KEY_MIDDLENAME, response.body().getMiddleName());
                            editor2.putString(Constants.KEY_LASTNAME, response.body().getLastName());
                            editor2.putString(Constants.KEY_OCCUPATION, response.body().getOccupation());
                            editor2.putString(Constants.KEY_MOBILE, response.body().getMobile());
                            editor2.commit();
                            SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_SWIPE, MODE_PRIVATE);
                            boolean isShowScreen  = prefs.getBoolean(Constants.KEY_ONE_TIME_PAGE,true);
                            if(isShowScreen) {
                                SharedPreferences.Editor editor3 = getSharedPreferences(Constants.MY_PREFS_SWIPE, MODE_PRIVATE).edit();
                                editor3.putBoolean(Constants.KEY_ONE_TIME_PAGE, false);
                                editor3.commit();
                                Intent intent = new Intent(LoginMainActivity.this, AboutActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Intent intent = new Intent(LoginMainActivity.this, Dashboard.class);
                                intent.putExtra("uploadedCount", Constants.FROM_);
                                startActivity(intent);
                                finish();
                            }
                        }else{
                            getUserDetailPopup(response.body().getUserId());
                        }
                    }else  if(response.body().getSuccess().toString().trim().equals("0")) {
                        if(dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(LoginMainActivity.this,"Result",response.body().getResult(),true,false);
                    }else {
                        if(dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(LoginMainActivity.this,"Error","Technical Error !!!",false,false);
                    }
                }else{
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    Constants.dispalyDialogInternet(LoginMainActivity.this,"Error","Technical Error !!!",false,false);
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                //Log.d("response :","");
                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                Constants.dispalyDialogInternet(LoginMainActivity.this,"Result",t.toString(),false,false);
            }
        });
    }

    private void getUserDetailPopup(final String userId){
        final Dialog userDetailPopup = new Dialog(LoginMainActivity.this);
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
        dialog = new ProgressDialog(LoginMainActivity.this);
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
                        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_SWIPE, MODE_PRIVATE);
                        boolean isShowScreen  = prefs.getBoolean(Constants.KEY_ONE_TIME_PAGE,true);
                        if(isShowScreen) {
                            SharedPreferences.Editor editor2 = getSharedPreferences(Constants.MY_PREFS_SWIPE, MODE_PRIVATE).edit();
                            editor2.putBoolean(Constants.KEY_ONE_TIME_PAGE, false);
                            editor2.commit();
                            Intent intent = new Intent(LoginMainActivity.this, AboutActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Intent intent = new Intent(LoginMainActivity.this, Dashboard.class);
                            intent.putExtra("uploadedCount", Constants.FROM_);
                            startActivity(intent);
                            finish();
                        }

                    } else if (response.body().getSuccess().toString().trim().equals("0")) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(LoginMainActivity.this,"Result",response.body().getResult(),true,false);
                    } else {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(LoginMainActivity.this,"Error","Technical Error !!!",false,false);
                    }
                }else{
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Constants.dispalyDialogInternet(LoginMainActivity.this,"Error","Technical Error !!!",false,false);
                }
            }
            @Override
            public void onFailure(Call<UserDetailResponseBean> call, Throwable t) {
                //Log.d("resp
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                Constants.dispalyDialogInternet(LoginMainActivity.this,"Result",t.toString(),false,false);
            }
        });
    }

    private static Target getTarget(){
        Target target = new Target(){
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        //File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + url);
                        File file = new File(Constants.FOLDER_PATH + File.separator + Constants.USER_PHOTO);
                        try {
                            if(file != null && !file.exists()) {
                                file.createNewFile();
                                FileOutputStream ostream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                                ostream.flush();
                                ostream.close();
                            }
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return target;
    }

    private void initViews() {
        callbackManager=CallbackManager.Factory.create();
        toLogin = (TextView)findViewById(R.id.move_to_login);
        toRegister = (TextView)findViewById(R.id.move_to_register);
        signinGmail = (ImageView) findViewById(R.id.signin_gmail);
        signinFacebook = (ImageView) findViewById(R.id.signin_facebook);
        keepMeSignin = (CheckBox) findViewById(R.id.checkboxRememberNew);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    /** Permission code starts*/
    private void requestPermission() {
        ActivityCompat.requestPermissions(LoginMainActivity.this, new String[]
                {
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        INTERNET,
                        WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE
                }, Constants.REQUESTPERMISSIONCODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUESTPERMISSIONCODE:
                if (grantResults.length > 0) {
                    boolean AccessFineLocationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean AccessCoarseLocPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean InternetPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean WriteInternalStoragePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadInternalStoragePermission = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    if (AccessFineLocationPermission && AccessCoarseLocPermission && InternetPermission && WriteInternalStoragePermission && ReadInternalStoragePermission) {
                    }
                    else {
                        Toast toast = Toast.makeText(LoginMainActivity.this,"Permission Denied,Accept it to use application", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        finish();
                    }
                }
                break;
        }
    }
    public boolean checkPermission() {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), INTERNET);
        int ForthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int FifthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ForthPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FifthPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(LoginMainActivity.this,"Failed to connect",Toast.LENGTH_SHORT).show();
    }
    /**Permission code end*/
}
