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
import com.ibin.plantplacepic.adapter.ViewPagerAdapter;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.utility.Constants;

import java.util.List;

public class InformationFragment extends Fragment{
    private OnFragmentInteractionListener mListener;
    DatabaseHelper databaseHelper;
    Context mContext ;
    List<String> speciesList;

    TextView textViewSpeciesName;
    TextView textViewSpeciesAddress;


    public InformationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView= inflater.inflate(R.layout.fragment_information, container, false);

        databaseHelper = DatabaseHelper.getDatabaseInstance(mContext);
        //speciesList = databaseHelper.(SpeciesByNameActivity.speciesName) ;
        final TextView textviewSpeciesInformation = (TextView) mainView.findViewById(R.id.textviewSpeciesInformation);
        TextView textViewSpeciesName = (TextView) mainView.findViewById(R.id.textViewSpeciesName);
        TextView textViewSpeciesAddress = (TextView) mainView.findViewById(R.id.textViewSpeciesAddress);
        ViewPager viewPager = (ViewPager) mainView.findViewById(R.id.pager_images);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(mContext,speciesList, Constants.FROM_SPECIES_INFORMATION);
        viewPager.setAdapter(viewPagerAdapter);

        return mainView;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
