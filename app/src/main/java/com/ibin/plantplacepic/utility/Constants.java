package com.ibin.plantplacepic.utility;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.activities.Dashboard;
import com.ibin.plantplacepic.activities.SignInActivity;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by NN on 27/05/2017.
 */

public class Constants {

    public static final int REQUESTPERMISSIONCODE = 1;
    public static final int RESULT_OPEN_CAMERA = 2 ;
    public static final int REQUEST_CODE_CROP_IMAGE = 3 ;
    public static final int API_LEVEL_23 = 23;
    public static final String IMAGE_NAME = "Upload_" ;
    public static final String FOLDER_PATH = Environment.getExternalStorageDirectory() + File.separator + "PLANTPLACE" ;
    //public static final String BASE_URL = "http://test.xamarin-tools.com";
    public static final String BASE_URL = "http://ibin.plantplacepicture.com";
    public static final String FILE_UPLOAD_URL = "http://ibin.plantplacepicture.com/plantplace/fileUpload.php";
    public static final String LOGIN_SERVICE_URL = "/plantplace/loginService.php";
    public static final String REGISTER_SERVICE_URL = "/plantplace/signupService.php";
    public static final String UPLOAD_SERVICE_URL = "/plantplace/fileUpload.php";
    public static final String UPLOAD_DATA_SERVICE_URL = "/plantplace/uploadData.php";
    public static final String UPDATE_DATA_SERVICE_URL = "/plantplace/updateData.php";
    public static final String MOVE_SPECIES_SERVICE_URL = "/plantplace/moveUpdateData.php";
    public static final String DOWNLOAD_DATA_SERVICE_URL = "/plantplace/getDataByUserId.php";
    public static final String GET_COUNT_SERVICE_URL = "/plantplace/getUploadCount.php";
    public static final String DELET_DATA_SERVICE_URL = "/plantplace/deleteData.php";
    public static final String IMAGE_DOWNLOAD_PATH = "http://ibin.plantplacepicture.com/plantplace/UploadedImages/";
//    public static final String IMAGE_DOWNLOAD_PATH_TAG_TREE = "http://test.xamarin-tools.com/UploadedTree/";
//    public static final String IMAGE_DOWNLOAD_PATH_TAG_FRUIT = "http://test.xamarin-tools.com/UploadedFruit/";
//    public static final String IMAGE_DOWNLOAD_PATH_TAG_LEAF = "http://test.xamarin-tools.com/UploadedLeaf/";
//    public static final String IMAGE_DOWNLOAD_PATH_TAG_FLOWER = "http://test.xamarin-tools.com/UploadedFlower/";
    public static final String TAG_TREE = "TREE";
    public static final String TAG_LEAF = "LEAF";
    public static final String TAG_FLOWER = "FLOWER";
    public static final String TAG_FRUIT = "FRUIT";
//    public static final String SERVER_FOLDER_PATH_TREE = "UploadedTree";
//    public static final String SERVER_FOLDER_PATH_FRUIT = "UploadedFruit";
//    public static final String SERVER_FOLDER_PATH_LEAF = "UploadedLeaf";
//    public static final String SERVER_FOLDER_PATH_FLOWER = "UploadedFlower";
    public static final String SERVER_FOLDER_PATH_ALL = "UploadedImages";
    public static final String MY_PREFS_NAME = "myPref";
    public static final String MY_PREFS_SWIPE = "myPrefSwipe";
    public static final String KEY_IS_LOGIN =  "login";
    public static final String KEY_USERNAME = "USERNAME";
    public static final String KEY_USERID =  "USERID";
    public static final String KEY_PHOTO =  "personPhotoUrl";
    public static final String KEY_HINT_SWAP =  "swap";
    public static int countSelectedPhotoFromGallery = 0;
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    /*No Internet Connection*/
    public static void dispalyDialogInternet(final Context context , String title, String message, boolean cancelDialog , final boolean isFinish) {
        final Dialog interrnetConnection = new Dialog(context);
        interrnetConnection.requestWindowFeature(Window.FEATURE_NO_TITLE);
        interrnetConnection.setContentView(R.layout.dialog_popup);
        interrnetConnection.setCanceledOnTouchOutside(cancelDialog);
        TextView tv = (TextView) interrnetConnection.findViewById(R.id.textMessage);
        tv.setText(message);
        TextView titleText = (TextView) interrnetConnection.findViewById(R.id.dialogHeading);
        titleText.setText(title);
        Button btnLogoutNo = (Button) interrnetConnection.findViewById(R.id.ok);
        btnLogoutNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interrnetConnection.dismiss();
                if(isFinish){
                    Intent i = new Intent(context, Dashboard.class);
                    context.startActivity(i);
                    ((Activity)context).finish();
                }
            }
        });
        interrnetConnection.show();
    }
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidEmailAddress(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(email);
        return matcher.find();
    }

    public static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    public static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    public static final String OUT_JSON = "/json";
    //debug
//    public static final String API_KEY = "AIzaSyCoNT5vDSVQmxhG2kS_JXsozKtym48r_54";
    public static final String API_KEY = "AIzaSyCRIriw_45fLM8_Qa-K2MNj5FC32JfRljQ";

    public static final String UPLOAD_FROM_GALLERY ="Gallery";
    public static final String UPLOAD_FROM_CAMERA ="Camera";
}
