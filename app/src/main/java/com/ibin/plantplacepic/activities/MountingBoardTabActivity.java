package com.ibin.plantplacepic.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
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
import com.ibin.plantplacepic.adapter.MyRecyclerViewAdapter;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.InformationResponseBean;
import com.ibin.plantplacepic.fragment.FragmentAllMountingBoard;
import com.ibin.plantplacepic.fragment.FragmentMyMountingBoard;
import com.ibin.plantplacepic.fragment.PhotosFragment;
import com.ibin.plantplacepic.fragment.SpeciesPhotoFragment;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MountingBoardTabActivity extends AppCompatActivity {

    public TabLayout tabLayout;
    private ViewPager viewPager;
    private List<Information> allMountingData = null;
    private List<Information> myMountingData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mounting_board_tab);

        initView();

        if(Constants.isNetworkAvailable(MountingBoardTabActivity.this)){
            getMountingData();
        }else{
            Toast.makeText(MountingBoardTabActivity.this,"No Internet",Toast.LENGTH_SHORT).show();
        }
    }

    private void getMountingData() {
        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
        final String userId = prefs.getString(Constants.KEY_USERID, "0");
        final ProgressDialog dialog = new ProgressDialog(MountingBoardTabActivity.this);
        dialog.setMessage("Please Wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
//                .client(okHttpClient)
                .build();
        ApiService service = retrofit.create(ApiService.class);
        Call<InformationResponseBean> call = service.getAllSpeciesDetailMountingBoard(userId);
        call.enqueue(new Callback<InformationResponseBean>() {
            @Override
            public void onResponse(Call<InformationResponseBean> call, Response<InformationResponseBean> response) {
                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                if (response != null && response.body() != null) {
                    if (response.body().getSuccess().toString().trim().equals("1")) {
                        if(response.body().getInformation() != null){
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
                                information.setMountingBoard(response.body().getInformation().get(i).getMountingBoard());
                                information.setLike(response.body().getInformation().get(i).getLike());
                                information.setLikeCount(response.body().getInformation().get(i).getLikeCount());
                                information.setDisLike(response.body().getInformation().get(i).getDisLike());
                                information.setDisLikeCount(response.body().getInformation().get(i).getDisLikeCount());
                                information.setUserName(response.body().getInformation().get(i).getUserName());
                                allMountingData.add(information);
                                if(response.body().getInformation().get(i).getUserId().equals(userId)){
                                   myMountingData.add(information);
                                }
                            }
                            setupViewPager(viewPager);
                            tabLayout.setupWithViewPager(viewPager);
                            setupTabIcons();
                        }else{
                            Log.d("NO records", "NO records --->");
//                            textMessageMb.setVisibility(View.VISIBLE);
//                            textMessageMb.setText("No Records Found");
                        }
                    } else if (response.body().getSuccess().toString().trim().equals("0")) {
                        Log.d("NO records", "NO records --->");
//                        textMessageMb.setVisibility(View.VISIBLE);
//                        textMessageMb.setText("No Records Found");
                    } else {
                        Log.d("Technical Error !!!", "Technical Error !!! --->");
//                        textMessageMb.setVisibility(View.VISIBLE);
//                        textMessageMb.setText("Technical Error");
                    }
                }else {
                    Log.d("Technical Error !!!", "Technical Error !!! --->");
//                    textMessageMb.setVisibility(View.VISIBLE);
//                    textMessageMb.setText("Technical Error");
                }
            }
            @Override
            public void onFailure(Call<InformationResponseBean> call, Throwable t) {
                Log.d("error", "error :--->"+t.toString());
                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
//              textMessageMb.setVisibility(View.VISIBLE);
//              textMessageMb.setText("Error");
            }
        });
    }

    private void setupTabIcons() {
        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText("ALL");
        //tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.icobspecies, 0, 0);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText("MY UPLOADS");
       // tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.iconpicture, 0, 0);
        tabLayout.getTabAt(1).setCustomView(tabTwo);
    }

    /**
     * Adding fragments to ViewPager
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        FragmentAllMountingBoard allMountingBoardFragment = new FragmentAllMountingBoard(MountingBoardTabActivity.this,MountingBoardTabActivity.this);
        Bundle data = new Bundle();
        data.putParcelableArrayList("allMountingData", (ArrayList<? extends Parcelable>) allMountingData);
        allMountingBoardFragment.setArguments(data);

        FragmentMyMountingBoard myMountingBoardFragment = new FragmentMyMountingBoard(MountingBoardTabActivity.this,MountingBoardTabActivity.this);
        Bundle dataMb = new Bundle();
        dataMb.putParcelableArrayList("myMountingData", (ArrayList<? extends Parcelable>) myMountingData);
        myMountingBoardFragment.setArguments(dataMb);

        ViewPagerAdapter adapter = new MountingBoardTabActivity.ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(allMountingBoardFragment, "All Mounting Board");
        adapter.addFrag(myMountingBoardFragment, "My Mounting Board");
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

    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewpagerMb);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        allMountingData = new ArrayList<>();
        myMountingData = new ArrayList<>();
    }

}
