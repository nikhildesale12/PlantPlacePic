package com.ibin.plantplacepic.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    Information updateBean;
    Spinner speciesSpinner;
    LinearLayout layoutSpeciesSpinnerUpdate;
    FloatingActionMenu materialDesignFAM;
    com.github.clans.fab.FloatingActionButton floatingActionButtonFlower ;
    com.github.clans.fab.FloatingActionButton floatingActionButtonFruit;
    com.github.clans.fab.FloatingActionButton floatingActionButtonLeaf;
    com.github.clans.fab.FloatingActionButton floatingActionButtonTree;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        initViews();
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
            Picasso.with(UpdateInfoActivity.this)
                    .load(imageFolderPath+updateBean.getImages())
                    .placeholder(R.drawable.pleasewait)   // optional
                    .error(R.drawable.pleasewait)
                    .into(captureImage);
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
                UpdateImageServiceCall();
            }
        });

        floatingActionButtonFlower.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TAG = Constants.TAG_FLOWER;
                //serverFolderPath = Constants.SERVER_FOLDER_PATH_FLOWER;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("TAG : "+TAG);
            }
        });
        floatingActionButtonFruit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TAG = Constants.TAG_FRUIT;
                //serverFolderPath = Constants.SERVER_FOLDER_PATH_FRUIT;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("TAG : "+TAG);
            }
        });
        floatingActionButtonLeaf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TAG = Constants.TAG_LEAF;
                //serverFolderPath = Constants.SERVER_FOLDER_PATH_LEAF;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("TAG : "+TAG);
            }
        });
        floatingActionButtonTree.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TAG = Constants.TAG_TREE;
                //serverFolderPath = Constants.SERVER_FOLDER_PATH_TREE;
                textTeg.setVisibility(View.VISIBLE);
                textTeg.setText("TAG : "+TAG);
            }
        });

        if(SpeciesPhotoFragment.dataListSpeciesNames != null && SpeciesPhotoFragment.dataListSpeciesNames.size()>0)
        {
            layoutSpeciesSpinnerUpdate.setVisibility(View.VISIBLE);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line, SpeciesPhotoFragment.dataListSpeciesNames);
            speciesSpinner.setAdapter(arrayAdapter);
        }else{
            layoutSpeciesSpinnerUpdate.setVisibility(View.GONE);
        }

        speciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    speciesEditText.setText(parent.getItemAtPosition(position).toString());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void UpdateImageServiceCall() {
        if(speciesEditText.getText().toString().length()>0) {
            String TITLE = titleEditText.getText().toString();
            String SPECIES = speciesEditText.getText().toString();
            String REMARK = remarkEditText.getText().toString();
            String USERID = updateBean.getUserId();
            String IMAGE = updateBean.getImages();
            String ADDRESS = cityEditText.getText().toString();
        /*when tag is same no need to move image*/
            if (TAG.equals(updateBean.getTag())) {
                serverFolderPath = "";
            }
            if (Constants.isNetworkAvailable(UpdateInfoActivity.this)) {
                Log.d("In callRetrofitUpdateData", "In callRetrofitUpdateData");
                Gson gson = new GsonBuilder().setLenient().create();
                Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build();
                ApiService service = retrofit.create(ApiService.class);
                Call<LoginResponse> call = service.dataUpdateService(USERID, IMAGE, SPECIES, REMARK, TAG, TITLE, serverFolderPath, serverFolderPathFrom, ADDRESS);
                call.enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response != null && response.body() != null) {
                            if (response.body().getSuccess().toString().trim().equals("1")) {
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

}
