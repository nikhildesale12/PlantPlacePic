package com.ibin.plantplacepic.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.activities.SpeciesByNameActivity;
import com.ibin.plantplacepic.adapter.ReviewAdapter;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class InformationFragment extends Fragment{
    private OnFragmentInteractionListener mListener;
    DatabaseHelper databaseHelper;
    Context mContext ;
    List<String> speciesList;
    ArrayList<Information> mainDataList = null;
    ArrayList<Information> informationList = null;
    private String selectedSpecies ;
    public RecyclerView recyclerView;
    public ReviewAdapter mAdapter;
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
        mainDataList = new ArrayList<>();
        informationList = new ArrayList<>();
        recyclerView = (RecyclerView) mainView.findViewById(R.id.recycler_view_info_species);
        databaseHelper = DatabaseHelper.getDatabaseInstance(mContext);
        if (getArguments() != null){
            mainDataList = getArguments().getParcelableArrayList("mainDataList");
            selectedSpecies = getArguments().getString("selectedSpecies");

            if(mainDataList != null && mainDataList.size()>0){
                for (int i=0;i< mainDataList.size();i++) {
                    if (mainDataList.get(i).getSpecies().trim().equalsIgnoreCase(selectedSpecies)) {
                        informationList.add(mainDataList.get(i));
                    }
                }
                mAdapter = new ReviewAdapter(informationList,mContext,"SearchByName");
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL));
                recyclerView.setAdapter(mAdapter);
            }
        };

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
