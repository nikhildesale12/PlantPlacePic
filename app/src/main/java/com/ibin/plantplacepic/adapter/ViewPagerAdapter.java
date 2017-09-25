package com.ibin.plantplacepic.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.widget.ImageView;

import com.ibin.plantplacepic.utility.Constants;

import java.util.List;

/**
 * Created by NN on 19/09/2017.
 */

public class ViewPagerAdapter extends PagerAdapter {

    Context context ;
    List<String> speciesList;
    String Information = "";
    public ViewPagerAdapter(Context mContext, List<String> speciesList, String fromSpeciesInformation) {

        this.context = context ;
        this.speciesList = speciesList ;
        if(fromSpeciesInformation.equals(Constants.FROM_SPECIES_INFORMATION)) {
            //Information = Constants.HOST_PLANT_IMAGE_URL;
        }
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }
}
