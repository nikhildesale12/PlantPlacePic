package com.ibin.plantplacepic.fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.activities.MountingBoardActivity;
import com.ibin.plantplacepic.adapter.MyRecyclerViewAdapter;
import com.ibin.plantplacepic.bean.Information;

import java.util.ArrayList;
import java.util.List;

public class FragmentAllMountingBoard extends Fragment {
    Context context;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "FragmentAllMountingBoard";
    private TextView textMessageMb;
    private List<Information> allMountingData = null;
    Activity activity;

    public FragmentAllMountingBoard() {

    }
    public FragmentAllMountingBoard(Context context,Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            allMountingData = new ArrayList<>();
            allMountingData = getArguments().getParcelableArrayList("allMountingData");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_all_mounting_board,
                container, false);

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        initViews(view);
        if(allMountingData != null){
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new MyRecyclerViewAdapter(allMountingData,activity);
            mRecyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    private void initViews(View view) {
        textMessageMb = (TextView) view.findViewById(R.id.alltextMessageMb);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.all_mounting_board_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
    }

}
