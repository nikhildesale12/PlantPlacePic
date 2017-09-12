package com.ibin.plantplacepic.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.activities.ReviewMyUpload;
import com.ibin.plantplacepic.adapter.UploadedSpeciesAdapter;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.serviceinterface.RecyclerViewItemClickListener;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.CustomRVItemTouchListener;
import com.ibin.plantplacepic.utility.ItemOffsetDecoration;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SpeciesPhotoFragment extends Fragment implements UploadedSpeciesAdapter.MyViewHolder.ClickListener {
    Context context;
    ArrayList<Information> dataList = null;
    ArrayList<Information> dataListWithAllSpecies = null;
    public static ArrayList<String> dataListSpeciesNames = null;
    ArrayList<Information> dataListSameSpecies = null;
    RecyclerView recyclerViewSpecies;
    UploadedSpeciesAdapter mAdapter;
    Toolbar topToolBar;
    MenuItem deleteMenuFolder;
    MenuItem moveMenuFolder;
    private SparseBooleanArray selectedItems ;
    DatabaseHelper databaseHelper;

    public SpeciesPhotoFragment() {
    }
    public SpeciesPhotoFragment(Context context) {
        this.context = context;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
           dataList = getArguments().getParcelableArrayList("reviewList");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_species_photo,
                container, false);
        setHasOptionsMenu(true);
        databaseHelper = DatabaseHelper.getDatabaseInstance(context);
        SharedPreferences prefs = context.getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
        String userId = prefs.getString("USERID", "0");
        selectedItems = new SparseBooleanArray();
        TextView textMsg = (TextView) view.findViewById(R.id.textMsg);
        topToolBar = (Toolbar)view.findViewById(R.id.toolbarSpecies);
        ((AppCompatActivity)getActivity()).setSupportActionBar(topToolBar);
        dataListWithAllSpecies = new ArrayList<>();
        dataListSpeciesNames = new ArrayList<>();
        dataListSameSpecies = new ArrayList<>();
        dataList = new ArrayList<>();
        recyclerViewSpecies = (RecyclerView) view.findViewById(R.id.recycler_view_upload_species);
//        if (getArguments() != null && Constants.isNetworkAvailable(context)) {
//            dataList = getArguments().getParcelableArrayList("reviewList");
//        }else{
//            dataList = databaseHelper.getImageUploadedInfo(userId);
//        }
        if(getArguments() != null && databaseHelper.getTotalUploadedData(userId) == 0){
            dataList = getArguments().getParcelableArrayList("reviewList");
        }else{
            dataList = databaseHelper.getImageUploadedInfo(userId);
        }
        int flagSpeciesName = 0;
        dataListSpeciesNames.add(0,"Select Species");
        if(dataList != null){
            for (int i=0 ; i<dataList.size();i++){
                if(dataList.get(i).getSpecies() != null && dataList.get(i).getSpecies().trim().length()>0){
                    if(dataListWithAllSpecies.size() == 0){
                        dataListWithAllSpecies.add(dataList.get(i));
                        if(dataList.get(i).getSpecies().trim().length()>0) {
                            dataListSpeciesNames.add(dataList.get(i).getSpecies().trim());
                        }
                        flagSpeciesName = 1;
                    }
                    int same = 0;
                    for(int j=0;j<dataListWithAllSpecies.size();j++){
                        if(dataList.get(i).getSpecies().equals(dataListWithAllSpecies.get(j).getSpecies())){
                            same= 1;
                        }
                    }
                    if(same ==0){
                        dataListWithAllSpecies.add(dataList.get(i));
                        if(dataList.get(i).getSpecies().trim().length()>0){
                            dataListSpeciesNames.add(dataList.get(i).getSpecies().trim());
                        }
                    }
                }
            }
        }
        if(flagSpeciesName == 1){
            textMsg.setVisibility(View.GONE);
            recyclerViewSpecies.setVisibility(View.VISIBLE);
            mAdapter = new UploadedSpeciesAdapter(context, dataListWithAllSpecies,this);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context,3);
            recyclerViewSpecies.setLayoutManager(layoutManager);
            recyclerViewSpecies.setItemAnimator(new DefaultItemAnimator());
            recyclerViewSpecies.addItemDecoration(new ItemOffsetDecoration(context, R.dimen.item_offset));
            recyclerViewSpecies.setAdapter(mAdapter);
        }else{
            textMsg.setVisibility(View.VISIBLE);
            recyclerViewSpecies.setVisibility(View.GONE);
        }

        recyclerViewSpecies.addOnItemTouchListener(new CustomRVItemTouchListener(context, recyclerViewSpecies, new RecyclerViewItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                dataListSameSpecies = new ArrayList<>();
                for(int i=0;i<dataList.size();i++){
                    if(dataListWithAllSpecies.get(position).getSpecies().equals(dataList.get(i).getSpecies())){
                        dataListSameSpecies.add(dataList.get(i));
                    }
                }
                Intent i = new Intent(context, ReviewMyUpload.class);
                i.putExtra("dataListSameSpecies",(ArrayList<? extends Parcelable>)dataListSameSpecies);
                startActivity(i);
            }
            /*@Override
            public void onLongClick(View view, int position) {

            }*/
        }));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_folder, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_folder) {
            Toast.makeText(context, " Selected : "+getSelectedItems(), Toast.LENGTH_SHORT).show();
//            if(getSelectedItems() != null && getSelectedItems().size() > 0) {
//                for (int i = 0; i < getSelectedItems().size(); i++) {
//                    callDeleteImageService(dataList.get(getSelectedItems().get(i)).getUserId(), dataList.get(getSelectedItems().get(i)).getImages(),i,getSelectedItems().size());
//                }
//            }
        }
        if (id == R.id.action_move_folder) {
            Toast.makeText(context, " Selected : "+getSelectedItems(), Toast.LENGTH_SHORT).show();
//            if(getSelectedItems() != null && getSelectedItems().size() > 0) {
//                moveToFolderPopUp();
//            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        deleteMenuFolder = menu.findItem(R.id.action_delete_folder);
        moveMenuFolder = menu.findItem(R.id.action_move_folder);
        if(mAdapter != null){
            if(mAdapter.getSelectedItemCount() == 0){
                deleteMenuFolder.setVisible(false);
                moveMenuFolder.setVisible(false);
            }
        }
    }

    @Override
    public boolean onItemLongClicked (int position) {
//        toggleSelection(position);
//        if(mAdapter.getSelectedItemCount() == 0){
//            deleteMenuFolder.setVisible(false);
//            moveMenuFolder.setVisible(false);
//        }else if(mAdapter.getSelectedItemCount() > 0){
//            deleteMenuFolder.setVisible(true);
//            moveMenuFolder.setVisible(true);
//        }
        return true;
    }
    private void toggleSelection(int position) {
        if(mAdapter != null){
            mAdapter.toggleSelection (position);
        }
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        }
        else {
            selectedItems.put(position, true);
        }
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }
}
