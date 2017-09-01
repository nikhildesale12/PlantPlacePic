package com.ibin.plantplacepic.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.ConnSupport;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener  {
    Button buttonSignUp;
    Button buttonSignIn;
    //SignInButton buttonGoogleSignIn;
    Button buttonGoogleSignIn;
    EditText editTextEmailSignin ;
    EditText editTextPassSignin ;
    String personPhotoUrl="";
    ProgressDialog dialog;
    public static final int RequestPermissionCode = 1;
    private static final String TAG = SignInActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 007;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sign_in);
        initViews();
        if(android.os.Build.VERSION.SDK_INT >= Constants.API_LEVEL_23){
            if(checkPermission()){
            }else {
                requestPermission();
            }
        }
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent activityChangeIntent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(activityChangeIntent);
                finish();
              /*  Intent intent = new Intent(SignInActivity.this,Dashboard.class);
                intent.putExtra("USERNAME","n");
                intent.putExtra("USERID","1");
                startActivity(intent);*/
            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String USER = editTextEmailSignin.getText().toString();
                String PASS = editTextPassSignin.getText().toString();
                if(Constants.isNetworkAvailable(SignInActivity.this)){
                    if(USER.length() == 0 ){
                        editTextEmailSignin.requestFocus();
                        editTextEmailSignin.setError("Please Enter Email id");
                    }else if(PASS.length() == 0){
                        editTextPassSignin.requestFocus();
                        editTextPassSignin.setError("Please Enter Password");
                    }else{
                        if(Constants.isValidEmailAddress(USER)){
                            executeLoginService(USER,PASS,"N");
                        }else{
                            //Toast.makeText(SignInActivity.this,"Please enter valid email address",Toast.LENGTH_LONG).show();
                            editTextEmailSignin.requestFocus();
                            editTextEmailSignin.setError("Invalid Email Id");
                        }
                        //Constants.dispalyDialogInternet(SignInActivity.this,"Invalid Credentials","Please enter login details...",true,false);
                    }
                }else{
                    Constants.dispalyDialogInternet(SignInActivity.this,"Internet Connection Issue","Please check internet connection ...",false,false);
                }
            }
        });
        buttonGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Constants.isNetworkAvailable(SignInActivity.this)){
                    signIn();
                }else{
                    Constants.dispalyDialogInternet(SignInActivity.this,"Internet Connection Issue","Please check internet connection ...",false,false);
                }
            }
        });
    }/*onCreate End*/

    private void executeLoginService(final String USER , String PASS , String GOOG_ID){
        dialog = new ProgressDialog(SignInActivity.this);
        dialog.setMessage("Please Wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();
//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .create();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
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
                        SharedPreferences.Editor editor1 = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor1.putString(Constants.KEY_USERNAME, USER);
                        editor1.putString(Constants.KEY_USERID, response.body().getUserId());
                        editor1.putBoolean(Constants.KEY_IS_LOGIN,true);
                        editor1.putString(Constants.KEY_PHOTO, personPhotoUrl);
                        editor1.commit();

                        Intent intent = new Intent(SignInActivity.this,AboutActivity.class);
                        startActivity(intent);
                        finish();
                    }else  if(response.body().getSuccess().toString().trim().equals("0")) {
                        if(dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(SignInActivity.this,"Result",response.body().getResult(),true,false);
                    }else {
                        if(dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(SignInActivity.this,"Error","Technical Error !!!",false,false);
                    }
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                //Log.d("response :","");
                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                Constants.dispalyDialogInternet(SignInActivity.this,"Result",t.toString(),false,false);
            }
        });
    }

    private void dispalyToast(String result) {
        Toast.makeText(this,result,Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    /*Google auth starts*/
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
            if(email.length() > 0){
                if(Constants.isNetworkAvailable(SignInActivity.this)){
                    if(email.length() > 0){
                        executeLoginService(email,"","Y");
                    }else{
                        Constants.dispalyDialogInternet(SignInActivity.this,"Invalid Credentials","Invalid Email ID...",true,false);
                    }
                }else{
                    Constants.dispalyDialogInternet(SignInActivity.this,"Internet Connection Issue","Please check internet connection ...",false,false);
                }
            }
          //  Toast.makeText(SignInActivity.this,"Sign In Successfully..",Toast.LENGTH_SHORT).show();
            //updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
            Toast.makeText(SignInActivity.this,"unauthenticated user due to "+result.getStatus(),Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(SignInActivity.this,"Failed to connect",Toast.LENGTH_SHORT).show();
    }
    /*Google auth end*/
    /** Permission code starts*/
    private void requestPermission() {
        ActivityCompat.requestPermissions(SignInActivity.this, new String[]
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
                        Toast toast = Toast.makeText(SignInActivity.this,"Permission Denied,Accept it to use application", Toast.LENGTH_LONG);
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
    /**Permission code end*/
    private void initViews() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        buttonSignUp = (Button)findViewById(R.id.buttonSignUp);
        buttonSignIn = (Button)findViewById(R.id.buttonSignIn);
        buttonGoogleSignIn = (Button)findViewById(R.id.buttonGoogleSignIn);
        editTextEmailSignin = (EditText) findViewById(R.id.editTextEmailSignin);
        editTextPassSignin = (EditText) findViewById(R.id.editTextPassSignin);
    }
}
