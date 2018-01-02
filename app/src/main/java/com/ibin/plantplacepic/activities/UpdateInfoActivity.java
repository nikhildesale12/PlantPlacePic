package com.ibin.plantplacepic.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.bean.SubmitRequest;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.fragment.SpeciesPhotoFragment;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.acra.ACRA.LOG_TAG;

public class UpdateInfoActivity extends AppCompatActivity {
    private Button buttonUpdate;
    private TextView textTeg ;
    private EditText titleEditText;
    private EditText speciesEditText;
    private EditText remarkEditText;
    private AutoCompleteTextView cityEditText;
    private ImageView captureImage;
    private String TAG ="";
    private String serverFolderPath = "";
    private String serverFolderPathFrom="";
    String userId= "0";
    TextView speciesNotLoadedUpdate;
    Information updateBean;
    Spinner speciesSpinner;
    LinearLayout layoutSpeciesSpinnerUpdate;
    FloatingActionMenu materialDesignFAM;
    com.github.clans.fab.FloatingActionButton floatingActionButtonFlower ;
    com.github.clans.fab.FloatingActionButton floatingActionButtonFruit;
    com.github.clans.fab.FloatingActionButton floatingActionButtonLeaf;
    com.github.clans.fab.FloatingActionButton floatingActionButtonTree;
    DatabaseHelper databaseHelper ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        initViews();
        databaseHelper = DatabaseHelper.getDatabaseInstance(UpdateInfoActivity.this);
        cityEditText.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_text_item));
        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
        userId = prefs.getString("USERID", "0");
        Intent intent = getIntent();
        if(intent != null){
            updateBean = (Information) intent.getExtras().get("dataListUpdate");
            String imageFolderPath ="";
            String tag = updateBean.getTag();
            TAG=tag;
            if(tag.equals(Constants.TAG_TREE)){
                //imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH_TAG_TREE;
                //serverFolderPathFrom = Constants.SERVER_FOLDER_PATH_TREE;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("Tag : "+Constants.TAG_TREE);
            }else if(tag.equals(Constants.TAG_LEAF)){
                //imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH_TAG_LEAF;
                //serverFolderPathFrom = Constants.SERVER_FOLDER_PATH_LEAF;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("Tag : "+Constants.TAG_LEAF);
            }else if(tag.equals(Constants.TAG_FLOWER)){
                //imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH_TAG_FLOWER;
                //serverFolderPathFrom = Constants.SERVER_FOLDER_PATH_FLOWER;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("Tag : "+Constants.TAG_FLOWER);
            }else  if(tag.equals(Constants.TAG_FRUIT)){
                //imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH_TAG_FRUIT;
                //serverFolderPathFrom = Constants.SERVER_FOLDER_PATH_FRUIT;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("Tag : "+Constants.TAG_FRUIT);
            }else {
                imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH;
                serverFolderPathFrom = Constants.SERVER_FOLDER_PATH_ALL;
                textTeg.setVisibility(View.GONE);
            }
            imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH;
//            Picasso.with(UpdateInfoActivity.this)
//                    .load(imageFolderPath+updateBean.getImages())
//                    .placeholder(R.drawable.pleasewait)   // optional
//                    .error(R.drawable.pleasewait)
//                    .into(captureImage);
            File file = new File(imageFolderPath+updateBean.getImages());
            if(file != null && file.exists()){
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                // Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);

                Bitmap bmt = getBitmap(file);
                captureImage.setImageBitmap(bmt);

            }else{
                Picasso.with(UpdateInfoActivity.this)
                        .load(imageFolderPath+updateBean.getImages())
                        .placeholder(R.drawable.pleasewait)
                        .error(R.drawable.pleasewait)
                        .into(getTarget(imageFolderPath+updateBean.getImages()));
                Glide.with(UpdateInfoActivity.this)
                        .load(imageFolderPath+updateBean.getImages())
                        .placeholder(R.drawable.pleasewait)
                        .error(R.drawable.pleasewait)
                        .thumbnail(0.5f)
                        .crossFade()
                        .into(captureImage);
            }
            if(updateBean.getTitle() != null && updateBean.getTitle().length() >0){
                titleEditText.setText(updateBean.getTitle());
            }
            if(updateBean.getSpecies() != null && updateBean.getSpecies().length() >0){
                speciesEditText.setText(updateBean.getSpecies());
            }
            if(updateBean.getRemark() != null && updateBean.getRemark().length() >0){
                remarkEditText.setText(updateBean.getRemark());
            }
            if(updateBean.getAddress() != null && updateBean.getAddress().length() >0){
                cityEditText.setText(updateBean.getAddress());
            }
        }
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Species name format states*/
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
                /*Species name format end*/
                UpdateImageServiceCall();
            }
        });

        floatingActionButtonFlower.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TAG = Constants.TAG_FLOWER;
                //serverFolderPath = Constants.SERVER_FOLDER_PATH_FLOWER;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("TAG : "+TAG);
                materialDesignFAM.close(true);
            }
        });
        floatingActionButtonFruit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TAG = Constants.TAG_FRUIT;
                //serverFolderPath = Constants.SERVER_FOLDER_PATH_FRUIT;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("TAG : "+TAG);
                materialDesignFAM.close(true);
            }
        });
        floatingActionButtonLeaf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TAG = Constants.TAG_LEAF;
                //serverFolderPath = Constants.SERVER_FOLDER_PATH_LEAF;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("TAG : "+TAG);
                materialDesignFAM.close(true);
            }
        });
        floatingActionButtonTree.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TAG = Constants.TAG_TREE;
                //serverFolderPath = Constants.SERVER_FOLDER_PATH_TREE;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("TAG : "+TAG);
                materialDesignFAM.close(true);
            }
        });

        if(SpeciesPhotoFragment.dataListSpeciesNames != null && SpeciesPhotoFragment.dataListSpeciesNames.size()>0)
        {
            layoutSpeciesSpinnerUpdate.setVisibility(View.VISIBLE);
//            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
//                    android.R.layout.simple_dropdown_item_1line, SpeciesPhotoFragment.dataListSpeciesNames);
//            speciesSpinner.setAdapter(arrayAdapter);
            SpinnerCustomAdapter adapter = new SpinnerCustomAdapter(UpdateInfoActivity.this,SpeciesPhotoFragment.dataListSpeciesNames);
            speciesSpinner.setAdapter(adapter);
            speciesNotLoadedUpdate.setVisibility(View.GONE);
        }else{
            //layoutSpeciesSpinnerUpdate.setVisibility(View.GONE);
            speciesNotLoadedUpdate.setVisibility(View.VISIBLE);
        }

        speciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
//                    speciesEditText.setText(parent.getItemAtPosition(position).toString());
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
    }

    //target to save
    private static Target getTarget(final String imageName){
        Target target = new Target(){
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        //File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + url);
                        File rootDirectory = new File(Constants.FOLDER_PATH);
                        if (!rootDirectory.exists()) {
                            rootDirectory.mkdir();
                        }
                        File file = new File(Constants.FOLDER_PATH + File.separator + imageName);
                        try {
                            if(file != null && !file.exists()) {
                                file.createNewFile();
                                FileOutputStream ostream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, ostream);
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

    private Bitmap getBitmap(File path) {
        String TAG = "";
        Uri uri = Uri.fromFile(path);
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();

            // int scale = 1;
//            while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) >
//                    IMAGE_MAX_SIZE) {
//                scale++;
//            }
//            Log.d(TAG, "scale = " + scale + ", orig-width: " + options.outWidth + ",orig-height: " + options.outHeight);

            Bitmap resultBitmap = null;
            in = getContentResolver().openInputStream(uri);
            // scale to max possible inSampleSize that still yields an image
            // larger than target
            options = new BitmapFactory.Options();
            options.inSampleSize = calculateInSampleSize(options,options.outWidth,options.outHeight);
            resultBitmap = BitmapFactory.decodeStream(in, null, options);

            // resize to desired dimensions
            int height = resultBitmap.getHeight();
            int width = resultBitmap.getWidth();
            Log.d(TAG, "1th scale operation dimenions - width: " + width + ",height: " + height);

            double y = Math.sqrt(IMAGE_MAX_SIZE
                    / (((double) width) / height));
            double x = (y / height) * width;

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(resultBitmap, (int) x,
                    (int) y, true);
            resultBitmap.recycle();
            resultBitmap = scaledBitmap;

            System.gc();
            in.close();

            Log.d(TAG, "bitmap size - width: " +resultBitmap.getWidth() + ", height: " +
                    resultBitmap.getHeight());
            return resultBitmap;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(),e);
            return null;
        }
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void UpdateImageServiceCall() {
        if(speciesEditText.getText().toString().length()>0) {
            final String TITLE = titleEditText.getText().toString();
            final String SPECIES = speciesEditText.getText().toString();
            final String REMARK = remarkEditText.getText().toString();
            final String IMAGE = updateBean.getImages();
            final String ADDRESS = cityEditText.getText().toString();
        /*when tag is same no need to move image*/
            if (TAG.equals(updateBean.getTag())) {
                serverFolderPath = "";
            }
            if (Constants.isNetworkAvailable(UpdateInfoActivity.this)) {
                Log.d("In callRetrofitUpdateData", "In callRetrofitUpdateData");
                Gson gson = new GsonBuilder().setLenient().create();
                Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build();
                ApiService service = retrofit.create(ApiService.class);
                Call<LoginResponse> call = service.dataUpdateService(userId, IMAGE, SPECIES, REMARK, TAG, TITLE, serverFolderPath, serverFolderPathFrom, ADDRESS);
                call.enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response != null && response.body() != null) {
                            if (response.body().getSuccess().toString().trim().equals("1")) {
                                //update it in local db
                                int updateResult = databaseHelper.updateInfoInLocal(userId, IMAGE, SPECIES, REMARK, TAG, TITLE, serverFolderPath, serverFolderPathFrom, ADDRESS);
                                Toast.makeText(UpdateInfoActivity.this, "Data will update soon...", Toast.LENGTH_LONG).show();
                                Intent intentdASH = new Intent(UpdateInfoActivity.this, Dashboard.class);
                                startActivity(intentdASH);
                                finish();
                            } else if (response.body().getSuccess().toString().trim().equals("0")) {
                                if (response.body().getResult().toString().trim().equals("Failed to move")) {
                                    Toast.makeText(UpdateInfoActivity.this, "Fail to change tag...", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(UpdateInfoActivity.this, "Fail to update data...", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Log.d("Error dataUploadService ", "Error dataUploadService : Technical Error !!!");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Log.d("Exception dataUploadService: ", "Exception dataUploadService: " + t.toString());
                    }
                });
            } else {
                Toast.makeText(UpdateInfoActivity.this, "Internet Unavailable,Unable to update data please checck internet connection...", Toast.LENGTH_LONG).show();
            }
        }else{
//            Toast toast = Toast.makeText(UpdateInfoActivity.this,"Please Enter Species", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 0, 0);
//            toast.show();
            speciesEditText.requestFocus();
            speciesEditText.setError("Please Enter Species");
        }
    }
    private void initViews() {
        speciesNotLoadedUpdate = (TextView) findViewById(R.id.speciesNotLoadedUpdate);
        speciesSpinner = (Spinner) findViewById(R.id.spinnerSpecies);
        layoutSpeciesSpinnerUpdate = (LinearLayout) findViewById(R.id.layoutSpeciesSpinnerUpdate);
        updateBean = new Information();
        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu_update);
        materialDesignFAM.setClosedOnTouchOutside(true);
        floatingActionButtonFlower = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_flower_update);
        floatingActionButtonFruit = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_fruit_update);
        floatingActionButtonLeaf = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_leaf_update);
        floatingActionButtonTree = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_tree_update);

        buttonUpdate = (Button)findViewById(R.id.buttonUpdate);
        textTeg = (TextView)findViewById(R.id.textTagUpdate);
        captureImage=(ImageView)findViewById(R.id.captureImageInUpdate);
        titleEditText=(EditText)findViewById(R.id.titleEditTextInUpdate);
        speciesEditText=(EditText)findViewById(R.id.speciesEditTextInUpdate);
        remarkEditText=(EditText)findViewById(R.id.remarkEditTextInUpdate);
        cityEditText = (AutoCompleteTextView) findViewById(R.id.cityEditTextInUpdate);
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

}
