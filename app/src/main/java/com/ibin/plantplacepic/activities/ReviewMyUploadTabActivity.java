package com.ibin.plantplacepic.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.InformationResponseBean;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.fragment.PhotosFragment;
import com.ibin.plantplacepic.fragment.SpeciesPhotoFragment;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.services.GetUploadedDataService;
import com.ibin.plantplacepic.utility.Constants;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReviewMyUploadTabActivity extends AppCompatActivity implements MaterialTabListener {
    private List<Information> reviewList = null;
    public TabLayout tabLayout;
    private ViewPager viewPager;
    public String userId = "";
    private TextView textNoDataAvailable;
    DatabaseHelper databaseHelper;
    int totalUploadedCount = 0;
    BroadcastReceiver receiver = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_my_upload_tab);
        initviews();
        reviewList = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
        userId = prefs.getString(Constants.KEY_USERID, "0");
        totalUploadedCount = databaseHelper.getTotalUploadedData(userId);
        if(Constants.isNetworkAvailable(this)){
            if(totalUploadedCount == 0){
                callServiceToGetPhotoesData(userId);
            } if(totalUploadedCount > 0){
                setupViewPager(viewPager);
                tabLayout.setupWithViewPager(viewPager);
                setupTabIcons();

                Intent intentService = new Intent(ReviewMyUploadTabActivity.this, GetUploadedDataService.class);
                startService(intentService);
            }
        }else{
            if(totalUploadedCount == 0) {
                Constants.dispalyDialogInternet(this, "Internet Unavailable", "Please connect internet once", false, true);
            }else{
                setupViewPager(viewPager);
                tabLayout.setupWithViewPager(viewPager);
                setupTabIcons();
            }
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.ibin.plantplacepic.CUSTOM_INTENT");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "Updating information...", Toast.LENGTH_LONG).show();
                if (intent.getExtras().getParcelableArrayList("reviewList") != null) {
                    reviewList = intent.getExtras().getParcelableArrayList("reviewList");
                    setupViewPager(viewPager);
                    tabLayout.setupWithViewPager(viewPager);
                    setupTabIcons();
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    private void initviews() {
        textNoDataAvailable = (TextView) findViewById(R.id.textNoDataAvailable);
        textNoDataAvailable.setVisibility(View.GONE);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        databaseHelper = DatabaseHelper.getDatabaseInstance(getApplicationContext());
    }

    private void callServiceToGetPhotoesData(String userId) {
        final ProgressDialog dialog = new ProgressDialog(ReviewMyUploadTabActivity.this);
        if(totalUploadedCount==0){
            dialog.setMessage("Please Wait...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
        }
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
                            boolean isInsert = false;
                            if(response.body().getInformation().size() != databaseHelper.getTotalUploadedData(response.body().getInformation().get(0).getUserId())) {
                                isInsert = true;
                                databaseHelper.removeAllSaveDataFromTable();
                            }
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
                                reviewList.add(information);
                                long insertedToLater = -1;
                                if(isInsert) {
                                    insertedToLater = databaseHelper.insertDataInTableInformationToSave(information);
                                }
                                if (insertedToLater != -1) {
                                    Log.d("Done", "Inserted ---> "+i +" == "+ insertedToLater);
                                }
                            }
                            setupViewPager(viewPager);
                            tabLayout.setupWithViewPager(viewPager);
                            setupTabIcons();
                        }else{
                            reviewList = new ArrayList<>();
                        }
                    } else if (response.body().getSuccess().toString().trim().equals("0")) {
                        textNoDataAvailable.setVisibility(View.VISIBLE);
                        Constants.dispalyDialogInternet(ReviewMyUploadTabActivity.this,"Result","No Records Found",false,true);
                    } else {
                        Constants.dispalyDialogInternet(ReviewMyUploadTabActivity.this,"Error","Technical Error !!!",false,true);
                    }
                }else {
                    Constants.dispalyDialogInternet(ReviewMyUploadTabActivity.this,"Error","Technical Error !!!",false,true);
                }
            }
            @Override
            public void onFailure(Call<InformationResponseBean> call, Throwable t) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                //Log.d("resp
                Constants.dispalyDialogInternet(ReviewMyUploadTabActivity.this,"Result",t.toString(),false,true);
            }
        });
    }

    @Override
    public void onTabSelected(MaterialTab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(MaterialTab tab) {

    }

    @Override
    public void onTabUnselected(MaterialTab tab) {

    }

    private void setupTabIcons() {

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText("Album");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.icobspecies, 0, 0);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText("PHOTOS");
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.iconpicture, 0, 0);
        tabLayout.getTabAt(1).setCustomView(tabTwo);

    }

    /**
     * Adding fragments to ViewPager
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        SpeciesPhotoFragment speciesFrag = new SpeciesPhotoFragment(ReviewMyUploadTabActivity.this);
        Bundle data = new Bundle();
        data.putParcelableArrayList("reviewList", (ArrayList<? extends Parcelable>) reviewList);
        speciesFrag.setArguments(data);

        PhotosFragment photoFragment = new PhotosFragment(ReviewMyUploadTabActivity.this);
        Bundle dataPhoto = new Bundle();
        dataPhoto.putParcelableArrayList("reviewList", (ArrayList<? extends Parcelable>) reviewList);
        photoFragment.setArguments(dataPhoto);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(speciesFrag, "Species");
        adapter.addFrag(photoFragment, "Photos");
        viewPager.setAdapter(adapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intentdASH = new Intent(this, Dashboard.class);
        startActivity(intentdASH);
        finish();
    }

    /*class DataReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Intent Detected hhhh....", Toast.LENGTH_LONG).show();
            if (intent.getExtras().getParcelableArrayList("reviewList") != null) {
                reviewList = intent.getExtras().getParcelableArrayList("reviewList");
                setupViewPager(viewPager);
                tabLayout.setupWithViewPager(viewPager);
                setupTabIcons();
            }
        }
    }*/
    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onStop();
    }
}
