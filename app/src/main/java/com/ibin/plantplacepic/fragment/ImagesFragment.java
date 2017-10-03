package com.ibin.plantplacepic.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.adapter.CustomPagerAdapterImage;
import com.ibin.plantplacepic.bean.Information;

import java.util.ArrayList;

public class ImagesFragment extends Fragment{

    private OnFragmentInteractionListner mListener;
    Context mContext ;
    ViewPager viewPager;
    CustomPagerAdapterImage customPagerAdapterImage;
    ArrayList<Information> mainDataList = null;
    ArrayList<Information> imagesDataList = null;
    private String selectedSpecies ="";
    public ImagesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_images, container, false);
        mainDataList = new ArrayList<>();
        imagesDataList = new ArrayList<>();
        if (getArguments() != null){
            mainDataList = getArguments().getParcelableArrayList("mainDataList");
            selectedSpecies = getArguments().getString("selectedSpecies");
        }
        if(mainDataList != null){
            for (int i=0;i< mainDataList.size();i++) {
                if (mainDataList.get(i).getSpecies().trim().equalsIgnoreCase(selectedSpecies)) {
                    imagesDataList.add(mainDataList.get(i));
                }
            }
        }
        viewPager = (ViewPager) mainView.findViewById(R.id.imagesViewPages);
        customPagerAdapterImage = new CustomPagerAdapterImage(mContext, imagesDataList);
        viewPager.setAdapter(customPagerAdapterImage);
        //viewPager.setCurrentItem(selectedPosition);

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