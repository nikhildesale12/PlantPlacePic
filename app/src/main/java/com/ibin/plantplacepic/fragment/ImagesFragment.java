package com.ibin.plantplacepic.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.activities.SpeciesSearchActivity;
import com.ibin.plantplacepic.adapter.ViewPagerAdapter;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.utility.Constants;

import java.util.List;

public class ImagesFragment extends Fragment{

    DatabaseHelper databaseHelper;
    private OnFragmentInteractionListner mListener;
    Context mContext ;
    private List<String> imagesArray;

    public ImagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  Bundle extras = getActivity().getIntent().getExtras();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_images, container, false);
        /*databaseHelper = DatabaseHelper.getDatabaseInstance(mContext);
        imagesArray = databaseHelper.getImagesFromSpecies(SpeciesSearchActivity.speciesImage);
        final TextView totalCountText = (TextView) mainView.findViewById(R.id.textviewTotalImages);
        final TextView textViewScroll = (TextView) mainView.findViewById(R.id.textviewScrollNext);
        ViewPager viewPager = (ViewPager) mainView.findViewById(R.id.pager_images);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(mContext, imagesArray, Constants.FROM_SPECIES_IMAGE);
        viewPager.setAdapter(viewPagerAdapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float v, int i2) {
                int selectednumber = position + 1;
                if (imagesArray != null) {
                    if (Constants.isNetworkAvailable(mContext)) {
                        totalCountText.setText("Total Images found - " + selectednumber + " out of " + imagesArray.size());
                    } else {
                        totalCountText.setText("Connect to internet to view images");
                    }
                    if (imagesArray.size() > 1) {
                        textViewScroll.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (Constants.isNetworkAvailable(mContext)) {
                        totalCountText.setText("Total Images found - 0");
                    } else {
                        totalCountText.setText("Connect to internet to view images");
                    }
                    textViewScroll.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageSelected(int i) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });*/
        return mainView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListner) {
            mListener = (OnFragmentInteractionListner) context;
            mContext = context ;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListner {
        void onFragmentInteraction(Uri uri);
    }
}