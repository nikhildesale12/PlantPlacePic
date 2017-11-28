package com.ibin.plantplacepic.activities;

import android.net.Uri;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.fragment.DistributionFragment;
import com.ibin.plantplacepic.fragment.ImagesFragment;
import com.ibin.plantplacepic.fragment.InformationFragment;

import java.util.ArrayList;
import java.util.List;

import static com.ibin.plantplacepic.fragment.ImagesFragment.*;

public class SpeciesInfoActivity extends AppCompatActivity implements ImagesFragment.OnFragmentInteractionListner,
        InformationFragment.OnFragmentInteractionListener,DistributionFragment.OnFragmentInteractionListner {

   // public static String speciesName = "";
    //ArrayList<String> speciesArray ;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String speciesToSearch ;
    List<Information> mainDataList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_info);
        mainDataList = new ArrayList<>();
        if(getIntent() != null && getIntent().getStringExtra("speciesNameSearch") != null){
            speciesToSearch = getIntent().getStringExtra("speciesNameSearch");
        }
        /*if(getIntent() != null && getIntent().getParcelableArrayListExtra("mainDataList") != null){
            mainDataList = getIntent().getParcelableArrayListExtra("mainDataList");
        }*/
        mainDataList = SpeciesByNameActivity.mainDataList;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void setupViewPager(ViewPager viewPager) {
        Fragment imagesFragment = new ImagesFragment();
        Fragment informationFragment = new InformationFragment();
        Fragment distribution = new DistributionFragment();

        Bundle data = new Bundle();
        data.putParcelableArrayList("mainDataList", (ArrayList<? extends Parcelable>) mainDataList);
        data.putString("selectedSpecies",speciesToSearch);
        imagesFragment.setArguments(data);
        informationFragment.setArguments(data);
        distribution.setArguments(data);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(imagesFragment, "Images");
        adapter.addFragment(informationFragment, "Information");
        adapter.addFragment(distribution, "Distribution");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
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

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
