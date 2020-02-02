package com.ibin.plantplacepic.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.InformationResponseBean;
import com.ibin.plantplacepic.bean.SubmitRequest;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.services.ImageUploadService;
import com.ibin.plantplacepic.utility.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.acra.ACRA.LOG_TAG;

public class ImageInfoActivity extends AppCompatActivity {
    private Button buttonUpload;
    private TextView textTeg ;
    DatabaseHelper databaseHelper;
    private Button buttonCrop ;
    private Button buttonSaveLater;
    private EditText titleEditText;
    private AutoCompleteTextView speciesEditText;
    private EditText remarkEditText;
    private ImageView captureImage;
    private Uri imageUri = null;
    private String imageName = "";
    private String uploadFrom = "";
    private String TAG ="";
    private double latitude=0;
    private double longitude =0;
    private String address="" ;
    private String cropStatus="";
    TextView speciesNotLoaded;
    private String currentDateTimeString ="";
    String userId= "0";
    Spinner speciesSpinner;
    AutoCompleteTextView cityEditText;
    LinearLayout layoutSpeciesSpinner ;
    ArrayList<String> dataListSpeciesNames = null;
    FloatingActionMenu materialDesignFAM;
    com.github.clans.fab.FloatingActionButton floatingActionButtonFlower ;
    com.github.clans.fab.FloatingActionButton floatingActionButtonFruit;
    com.github.clans.fab.FloatingActionButton floatingActionButtonLeaf;
    com.github.clans.fab.FloatingActionButton floatingActionButtonTree;
    SubmitRequest submitRequest ;
    public static String uploadedSpecies = "";
    CheckBox checkMountainBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_image_info);
        initViews();
        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
        userId = prefs.getString("USERID", "0");
        if (Constants.isNetworkAvailable(ImageInfoActivity.this)) {
            layoutSpeciesSpinner.setVisibility(View.VISIBLE);
            callServiceToGetSpeciesNames(userId);
        }else{
            //layoutSpeciesSpinner.setVisibility(View.GONE);
            speciesNotLoaded.setVisibility(View.VISIBLE);
        }
        //cityEditText.setThreshold(3);
        cityEditText.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_text_item));

        Intent intent = getIntent();
        submitRequest = new SubmitRequest();
        /*if(intent != null){
            imageUri = Uri.parse(intent.getStringExtra("imageUri"));
            captureImage.setImageURI(imageUri);
            latitude = intent.getDoubleExtra("latitude",0);
            longitude = intent.getDoubleExtra("longitude",0);
            address = intent.getStringExtra("finalAddress");
            currentDateTimeString = intent.getStringExtra("currentDateTimeString");
            imageName =  intent.getStringExtra("imageName");
        }*/
        if(intent != null && intent.getSerializableExtra("submitRequest") != null) {
            submitRequest = (SubmitRequest) intent.getSerializableExtra("submitRequest");
            if (submitRequest != null) {
                if(submitRequest.getImageUrl() != null && submitRequest.getImageUrl().length()>0){
                    imageUri = Uri.parse(submitRequest.getImageUrl());
                    captureImage.setImageURI(imageUri);
                }
                if(submitRequest.getLatitude() != null && submitRequest.getLatitude().trim().length()>0){
                    latitude = Double.valueOf(submitRequest.getLatitude());
                }
                if(submitRequest.getLongitude() != null && submitRequest.getLongitude().trim().length()>0){
                    longitude = Double.valueOf(submitRequest.getLongitude());
                }
                if(submitRequest.getAddress() != null && submitRequest.getAddress().length()>0){
                    address = submitRequest.getAddress();
                }
                if(submitRequest.getTime() != null && submitRequest.getTime().length()>0){
                    currentDateTimeString = submitRequest.getTime();
                }
                if(submitRequest.getImageName() != null && submitRequest.getImageName().length()>0){
                    imageName = submitRequest.getImageName();
                }
                if(submitRequest.getUploadedFrom() != null && submitRequest.getUploadedFrom().length()>0){
                    uploadFrom = submitRequest.getUploadedFrom();
                }
            }
        }
        if(uploadFrom.equals(Constants.UPLOAD_FROM_GALLERY)){
            cityEditText.setVisibility(View.VISIBLE);
        }else if(uploadFrom.equals(Constants.UPLOAD_FROM_CAMERA) && (latitude == 0 || longitude == 0)){
            cityEditText.setVisibility(View.VISIBLE);
        }else{
            cityEditText.setVisibility(View.GONE);
        }
        buttonCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropCapturedImage(imageUri);
            }
        });
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String speciesName = speciesEditText.getText().toString();
                String formatedSpeciesName="";
                for(int i=0;i<speciesName.length();i++)
                {
                    if(i==0){
                        String firstLetter = ""+speciesName.charAt(i);
                        formatedSpeciesName += firstLetter.toUpperCase();
                    }else{
                        String restLetter = ""+speciesName.charAt(i);
                        formatedSpeciesName += restLetter.toLowerCase();
                    }
                }
                speciesEditText.setText(formatedSpeciesName);
                if (speciesEditText.getText().toString().trim().length() == 0) {
                    speciesEditText.requestFocus();
                    speciesEditText.setError("Please Enter Species");
                } else if (cityEditText.getVisibility() == View.VISIBLE && cityEditText.getText().toString().trim().length() == 0) {
                    cityEditText.requestFocus();
                    cityEditText.setError("Please Enter Location");
                } else if(textTeg.getText().toString() == null || (textTeg.getText().toString() != null && textTeg.getText().toString().length()==0)) {
                    Toast.makeText(ImageInfoActivity.this,"Please select TAG from right below plus button",Toast.LENGTH_LONG).show();
                } else {
                    UploadImageServiceCall();
                }
            }
        });

        buttonSaveLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (speciesEditText.getText().toString().trim().length() == 0) {
                    speciesEditText.requestFocus();
                    speciesEditText.setError("Please Enter Species");
                } else if (cityEditText.getVisibility() == View.VISIBLE && cityEditText.getText().toString().trim().length() == 0) {
                    cityEditText.requestFocus();
                    cityEditText.setError("Please Enter Location");
                } else {
                    saveInLocalForLaterUpload();
                }
            }
        });

        floatingActionButtonFlower.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TAG = Constants.TAG_FLOWER;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("TAG : "+TAG);
                materialDesignFAM.close(true);
            }
        });
        floatingActionButtonFruit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TAG = Constants.TAG_FRUIT;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("TAG : "+TAG);
                materialDesignFAM.close(true);
            }
        });
        floatingActionButtonLeaf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TAG = Constants.TAG_LEAF;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("TAG : "+TAG);
                materialDesignFAM.close(true);
            }
        });
        floatingActionButtonTree.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TAG = Constants.TAG_TREE;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("TAG : "+TAG);
                materialDesignFAM.close(true);
            }
        });

        speciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    speciesEditText.setText(parent.getAdapter().getItem(position).toString());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        speciesEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String speciesName = speciesEditText.getText().toString();
                    String formatedSpeciesName="";
                    for(int i=0;i<speciesName.length();i++)
                    {
                        if(i==0){
                            String firstLetter = ""+speciesName.charAt(i);
                            formatedSpeciesName += firstLetter.toUpperCase();
                        }else{
                            String restLetter = ""+speciesName.charAt(i);
                            formatedSpeciesName += restLetter.toLowerCase();
                        }
                    }
                    speciesEditText.setText(formatedSpeciesName);
                }
            }
        });

//        checkMountainBoard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked){
//                    Toast.makeText(ImageInfoActivity.this,"True",Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(ImageInfoActivity.this,"False",Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }// end oncreate

    private void callServiceToGetSpeciesNames(String userId) {
//        final ProgressDialog dialog = new ProgressDialog(ImageInfoActivity.this);
//        dialog.setMessage("Please Wait...");
//        dialog.setIndeterminate(false);
//        dialog.setCancelable(false);
//        dialog.show();
        // Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        ApiService service = retrofit.create(ApiService.class);
        Call<InformationResponseBean> call = service.downloadDataById(userId);
        call.enqueue(new Callback<InformationResponseBean>() {
            @Override
            public void onResponse(Call<InformationResponseBean> call, Response<InformationResponseBean> response) {
//                if (dialog != null && dialog.isShowing()) {
//                    dialog.dismiss();
//                }
                if (response != null && response.body() != null) {
                    if (response.body().getSuccess().toString().trim().equals("1")) {
                        if(response.body().getInformation() != null){
                            dataListSpeciesNames.add(0,"Select Species");
                            for(int i = 0 ;i<response.body().getInformation().size();i++){
                                if(!dataListSpeciesNames.contains(response.body().getInformation().get(i).getSpecies().trim())){
                                    if(response.body().getInformation().get(i).getSpecies().trim().length()>0){
                                        dataListSpeciesNames.add(response.body().getInformation().get(i).getSpecies().trim());
                                    }
                                }
                            }
                            if(uploadedSpecies.length() > 0 && !dataListSpeciesNames.contains(uploadedSpecies)){
                                dataListSpeciesNames.add(uploadedSpecies);
                            }
                            if(dataListSpeciesNames.size() > 1){
                                layoutSpeciesSpinner.setVisibility(View.VISIBLE);
                                SpinnerCustomAdapter adapter;
                                adapter = new SpinnerCustomAdapter(ImageInfoActivity.this,dataListSpeciesNames);
                                speciesSpinner.setAdapter(adapter);
                                speciesNotLoaded.setVisibility(View.GONE);

                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ImageInfoActivity.this,
                                        android.R.layout.simple_list_item_1, dataListSpeciesNames);
                                speciesEditText.setThreshold(2);
                                speciesEditText.setAdapter(arrayAdapter);

                            }else{
                                //layoutSpeciesSpinner.setVisibility(View.GONE);
                                speciesNotLoaded.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    if (response.body().getSuccess().toString().trim().equals("0")) {
                            //layoutSpeciesSpinner.setVisibility(View.GONE);
                            speciesNotLoaded.setVisibility(View.VISIBLE);
                            if(uploadedSpecies.length() > 0){
                                dataListSpeciesNames.add(0,"Select Folder");
                                dataListSpeciesNames.add(uploadedSpecies);
                            }
                            if(dataListSpeciesNames.size() > 1){
                                layoutSpeciesSpinner.setVisibility(View.VISIBLE);
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ImageInfoActivity.this,
                                        android.R.layout.simple_list_item_1, dataListSpeciesNames);
                                speciesSpinner.setAdapter(arrayAdapter);
                                speciesNotLoaded.setVisibility(View.GONE);
                            }else{
                                //layoutSpeciesSpinner.setVisibility(View.GONE);
                                speciesNotLoaded.setVisibility(View.VISIBLE);
                            }
                    }
                }
            }
            @Override
            public void onFailure(Call<InformationResponseBean> call, Throwable t) {
                //Log.d("resp
//                if (dialog != null && dialog.isShowing()) {
//                    dialog.dismiss();
//                }
                speciesNotLoaded.setVisibility(View.VISIBLE);
            }
        });
    }



    public class SpinnerCustomAdapter extends BaseAdapter {
        Context context;
        LayoutInflater inflter;
        ArrayList<String> dataListSpeciesNames;

        public SpinnerCustomAdapter(Context applicationContext,ArrayList<String> dataListSpeciesNames) {
            this.context = applicationContext;
            this.dataListSpeciesNames = dataListSpeciesNames;
//            inflter = (LayoutInflater.from(applicationContext));
            inflter = (LayoutInflater)applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return dataListSpeciesNames.size();
        }

        public Object getItem(int position) {
            return dataListSpeciesNames.get(position);
        }
        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflter.inflate(R.layout.custom_spinner_items, null);
            TextView names = (TextView) convertView.findViewById(R.id.textViewSpinnerItem);
            names.setText(dataListSpeciesNames.get(position));
            return convertView;
        }
    }

    private void UploadImageServiceCall(){
            SubmitRequest submitReq = new SubmitRequest();
            submitReq.setUserId(userId);
            if (imageUri != null) {
                submitReq.setImageUrl(imageUri.getPath());
            }
            submitReq.setImageName(imageName);
            submitReq.setTitle(titleEditText.getText().toString());
            submitReq.setSpecies(speciesEditText.getText().toString());
            uploadedSpecies = speciesEditText.getText().toString();
            submitReq.setRemark(remarkEditText.getText().toString());
            submitReq.setTag(TAG);
            if (cityEditText.getText().toString().length() > 0) {
                submitReq.setAddress(cityEditText.getText().toString());
            } else {
                submitReq.setAddress(address);
            }
            if(latitude == 0.0 && longitude == 0.0){
                //convert lat lng from address
                getAddressOnMap(submitReq);
            }else {
                submitReq.setLatitude(String.valueOf(latitude));
                submitReq.setLongitude(String.valueOf(longitude));
            }
            submitReq.setCrop(cropStatus);
            submitReq.setStatus("false");
            submitReq.setTime(currentDateTimeString);
            submitReq.setUploadedFrom(uploadFrom);
            if(checkMountainBoard.isChecked()){
                submitReq.setMountingBoard("Y");
            }else{
                submitReq.setMountingBoard("N");
            }
            //call Service and pass data
            if (Constants.isNetworkAvailable(ImageInfoActivity.this)) {
                if(userId.equals("0")){
                    Constants.dispalyDialogInternet(ImageInfoActivity.this,"Invalid User","Please login again to upload information",false,false);
                }else {
                        Toast.makeText(ImageInfoActivity.this, "Uploading Data...", Toast.LENGTH_LONG).show();
                        Intent intentService = new Intent(this, ImageUploadService.class);
                        //intentService.putExtra("submitRequest", submitReq);
                        List<SubmitRequest> submitReqList = new ArrayList<>();
                        submitReqList.add(submitReq);
                        intentService.putExtra("submitRequest", (Serializable) submitReqList);
                        startService(intentService);
                }
            } else {
                //save in local db
                Toast.makeText(ImageInfoActivity.this, "Internet Unavailable,Data will automatically upload once device connect to internet ...", Toast.LENGTH_LONG).show();
                if(!databaseHelper.isDataAvialableInLocalDb(submitReq)) {
                    databaseHelper.insertDataInTableInformation(submitReq);
                }
            }
            //if(imageName.contains(submitRequest.getImagesPathList().get(submitRequest.getImagesPathList().size() - 1))) {
            Constants.countSelectedPhotoFromGallery--;
            if (Constants.countSelectedPhotoFromGallery == 0) {
                Intent intentdASH = new Intent(this, Dashboard.class);
                startActivity(intentdASH);
                finish();
            } else {
                finish();
            }
    }


    private void getAddressOnMap(SubmitRequest submitReq) {
        Geocoder geocode = new Geocoder(this);
        double latitude = 0.0;
        double longitude = 0.0;
        Address address = null;
        List<Address> addressList = null;
        String addressStr = submitReq.getAddress();
        try {
            if (!TextUtils.isEmpty(addressStr)) {
                addressList = geocode.getFromLocationName(addressStr, 5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != addressList && addressList.size() > 0) {
            address = addressList.get(0);
        }
        if (null != address && address.hasLatitude()
                && address.hasLongitude()) {
            latitude = address.getLatitude();
            longitude = address.getLongitude();
        }
        submitReq.setLatitude(String.valueOf(latitude));
        submitReq.setLongitude(String.valueOf(longitude));
    }

    private void saveInLocalForLaterUpload(){
            SubmitRequest submitReq = new SubmitRequest();
            submitReq.setUserId(userId);
            if (imageUri != null) {
                submitReq.setImageUrl(imageUri.getPath());
            }
            submitReq.setImageName(imageName);
            submitReq.setTitle(titleEditText.getText().toString());
            submitReq.setSpecies(speciesEditText.getText().toString());
            uploadedSpecies = speciesEditText.getText().toString();
            submitReq.setRemark(remarkEditText.getText().toString());
            submitReq.setTag(TAG);
            submitReq.setLatitude(String.valueOf(latitude));
            submitReq.setLongitude(String.valueOf(longitude));
            if (cityEditText.getText().toString().length() > 0) {
                submitReq.setAddress(cityEditText.getText().toString());
            } else {
                submitReq.setAddress(address);
            }
            submitReq.setCrop(cropStatus);
            submitReq.setStatus("false");
            submitReq.setUploadedFrom(uploadFrom);
            submitReq.setTime(currentDateTimeString);
            if(!databaseHelper.isDataAvialableInLocalDb(submitReq)){
                long insertedToLater = databaseHelper.insertDataInTableInformation(submitReq);
                if (insertedToLater != -1) {
                    Log.d("Done", "Inserted : " + insertedToLater);
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to save", Toast.LENGTH_SHORT).show();
                }
            }
            //if(imageName.contains(submitRequest.getImagesPathList().get(submitRequest.getImagesPathList().size() - 1))) {
            Constants.countSelectedPhotoFromGallery--;
            if (Constants.countSelectedPhotoFromGallery == 0) {
                Toast.makeText(ImageInfoActivity.this, "Data Saved...", Toast.LENGTH_LONG).show();
                Intent intentdASH = new Intent(this, Dashboard.class);
                startActivity(intentdASH);
                finish();
            } else {
                Toast.makeText(ImageInfoActivity.this, "Data Saved...", Toast.LENGTH_LONG).show();
                finish();
            }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case Constants.REQUEST_CODE_CROP_IMAGE:
                if(resultCode== Activity.RESULT_OK) {
                    captureImage.setImageURI(imageUri);
                    cropStatus = "true";
                }
                else if(resultCode==Activity.RESULT_CANCELED) {
                    captureImage.setImageURI(imageUri);
                    Toast.makeText(this, "Image not Cropped", Toast.LENGTH_SHORT).show();
                    cropStatus = "false";
                }
                break;
            default:
                break;
        }
    }

    public void cropCapturedImage(Uri picUri){
        captureImage.setImageURI(null);
//        Intent cropIntent = new Intent("com.android.camera.action.CROP");
//        cropIntent.setData(picUri);
//        cropIntent.setDataAndType(picUri, "image/*");
//        cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        Uri photoURI = FileProvider.getUriForFile(ImageInfoActivity.this, BuildConfig.APPLICATION_ID+ ".provider", new File(picUri.toString()));
//        String path = picUri.toString() ;
//        File mFileTemp = new File(path);
//        Uri photoURI = FileProvider.getUriForFile(ImageInfoActivity.this, BuildConfig.APPLICATION_ID+ ".provider", mFileTemp);
//        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//        cropIntent.putExtra("crop", "true");
//        cropIntent.putExtra("return-data", true);
//        startActivityForResult(cropIntent, Constants.REQUEST_CODE_CROP_IMAGE);


        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties here
            cropIntent.putExtra("crop", true);
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 128);
            cropIntent.putExtra("outputY", 128);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, Constants.REQUEST_CODE_CROP_IMAGE);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    private void initViews() {
        layoutSpeciesSpinner = (LinearLayout) findViewById(R.id.layoutSpeciesSpinner);
        dataListSpeciesNames = new ArrayList<>();
        speciesSpinner = (Spinner) findViewById(R.id.spinnerSpeciesInfo);
        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        materialDesignFAM.setClosedOnTouchOutside(true);
        floatingActionButtonFlower = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_flower);
        floatingActionButtonFruit = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_fruit);
        floatingActionButtonLeaf = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_leaf);
        floatingActionButtonTree = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_tree);
        TAG = "";
        cropStatus ="false";
        buttonUpload = (Button)findViewById(R.id.buttonUpload);
        textTeg = (TextView)findViewById(R.id.textTag);
        buttonSaveLater =(Button)findViewById(R.id.buttonSaveLater);
        buttonCrop =(Button)findViewById(R.id.buttonCrop);
        captureImage=(ImageView)findViewById(R.id.captureImage);
        titleEditText=(EditText)findViewById(R.id.titleEditText);
        speciesEditText=(AutoCompleteTextView)findViewById(R.id.speciesEditText);
        remarkEditText=(EditText)findViewById(R.id.remarkEditText);
        cityEditText=(AutoCompleteTextView) findViewById(R.id.cityEditText);
        databaseHelper = DatabaseHelper.getDatabaseInstance(getApplicationContext());
        speciesNotLoaded = (TextView) findViewById(R.id.speciesNotLoaded);
        checkMountainBoard= (CheckBox) findViewById(R.id.checkMountainBoard);
    }


    @Override
    public void onBackPressed() {
        //if(imageName.contains(submitRequest.getImagesPathList().get(submitRequest.getImagesPathList().size() - 1))) {
        Log.d(" Constants.countSelectedPhotoFromGallery "," Constants.countSelectedPhotoFromGallery : "+Constants.countSelectedPhotoFromGallery);
        Constants.countSelectedPhotoFromGallery--;
        if(Constants.countSelectedPhotoFromGallery == 0){
            Toast.makeText(ImageInfoActivity.this, "Task Cancel...", Toast.LENGTH_LONG).show();
            Intent intentdASH = new Intent(this, Dashboard.class);
            startActivity(intentdASH);
            finish();
        }else{
            Toast.makeText(ImageInfoActivity.this, "Task Cancel...", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /*place code starts*/
    public static ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(Constants.PLACES_API_BASE + Constants.TYPE_AUTOCOMPLETE + Constants.OUT_JSON);
            sb.append("?key=" + Constants.API_KEY);
            sb.append("&components=");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());

            System.out.println("URL: "+url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
    /*place coed end*/

    public List<Address> getAddress(double lat ,double lng) {
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this);
            if (lat != 0 || lng != 0) {
                addresses = geocoder.getFromLocation(lat ,lng , 1);
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getAddressLine(1);
                String country = addresses.get(0).getAddressLine(2);
                System.out.println(address+" - "+city+" - "+country);

                return addresses;

            } else {
//                Toast.makeText(this, "latitude and longitude are null",
//                        Toast.LENGTH_LONG).show();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
        userId = prefs.getString("USERID", "0");
    }
}
