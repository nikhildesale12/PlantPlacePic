package com.ibin.plantplacepic.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.activities.LargeZoomActivity;
import com.ibin.plantplacepic.activities.ReviewMyUploadTabActivity;
import com.ibin.plantplacepic.activities.UpdateInfoActivity;
import com.ibin.plantplacepic.adapter.UploadedPhotoAdapter;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.ItemOffsetDecoration;

import android.util.SparseBooleanArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

public class PhotosFragment extends Fragment implements UploadedPhotoAdapter.MyViewHolder.ClickListener {
    Context context;
    ArrayList<Information> dataList = new ArrayList<>();
    UploadedPhotoAdapter mAdapter;
    Toolbar topToolBar;
    RecyclerView recyclerView;
    MenuItem deleteMenu;
    MenuItem editMenu;
    MenuItem moveMenu;
    MenuItem viewMenu;
    private SparseBooleanArray selectedItems ;
    DatabaseHelper databaseHelper;

    public PhotosFragment() {
    }
    public PhotosFragment(Context context) {
        this.context = context;
    }

    @Override
    public void
    onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dataList = getArguments().getParcelableArrayList("reviewList");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_photos,
                container, false);
        databaseHelper = DatabaseHelper.getDatabaseInstance(context);
        SharedPreferences prefs = context.getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
        String userId = prefs.getString("USERID", "0");
        selectedItems = new SparseBooleanArray();
        TextView textMsg = (TextView) view.findViewById(R.id.textMsgPhoto);
        topToolBar = (Toolbar) view.findViewById(R.id.toolbarPhotos);
        ((AppCompatActivity)getActivity()).setSupportActionBar(topToolBar);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_upload_photo);
//        if (getArguments() != null && Constants.isNetworkAvailable(context)) {
//            dataList = getArguments().getParcelableArrayList("reviewList");
//        }else {
//            dataList = databaseHelper.getImageUploadedInfo(userId);
//        }
        if(getArguments() != null  && databaseHelper.getTotalUploadedData(userId) == 0){
            dataList = getArguments().getParcelableArrayList("reviewList");
        }else{
            dataList = databaseHelper.getImageUploadedInfo(userId);
        }
        if(dataList != null && dataList.size()>0){
            textMsg.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            mAdapter = new UploadedPhotoAdapter(context, dataList,this);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context,3);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new ItemOffsetDecoration(context, R.dimen.item_offset));
            recyclerView.setAdapter(mAdapter);
            //new LoadImage(context, dataList,this).execute();
        }else{
            textMsg.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

//        recyclerView.addOnItemTouchListener(new CustomRVItemTouchListener(context, recyclerView, new RecyclerViewItemClickListener() {
//            @Override
//            public void onClick(View view, int position) {
//                Intent intent = new Intent(context,LargeZoomActivity.class);
//                intent.putExtra("imageName",dataList.get(position).getImages());
//                intent.putExtra("tag",dataList.get(position).getTag());
//                context.startActivity(intent);
//            }
//            @Override
//            public void onLongClick(View view, int position) {
//
//            }
//        }));

        return view;
    }

    /*class LoadImage extends AsyncTask<Object, Object, String> {
        Context context;
        ArrayList<Information> dataList;
        UploadedPhotoAdapter.MyViewHolder.ClickListener clickListener ;
        public LoadImage(Context cxt,ArrayList<Information> dataList, UploadedPhotoAdapter.MyViewHolder.ClickListener clickListener) {
            this.context = cxt;
            this.dataList = dataList;
            this.clickListener = clickListener;
        }

        @Override
        protected String doInBackground(Object... params) {
            recyclerView.setVisibility(View.VISIBLE);
            mAdapter = new UploadedPhotoAdapter(context, dataList,clickListener);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context,3);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new ItemOffsetDecoration(context, R.dimen.item_offset));
            recyclerView.setAdapter(mAdapter);
            return "";
        }
        @Override
        protected void onPostExecute(String result) {

        }

    }*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_view_img) {
            if(getSelectedItems() != null && getSelectedItems().size() > 0) {
                Intent intent = new Intent(context, LargeZoomActivity.class);
               // intent.putExtra("imageName", dataList.get(getSelectedItems().get(0)).getImages());
               // intent.putExtra("tag", dataList.get(getSelectedItems().get(0)).getTag());
                Bundle data = new Bundle();
                data.putParcelableArrayList("imageDataList", (ArrayList<? extends Parcelable>) dataList);
                data.putInt("selectedPosition",getSelectedItems().get(0));
                intent.putExtras(data);
                context.startActivity(intent);
                //((Activity)context).finish();
            }
        }
        if (id == R.id.action_delete) {
            if(getSelectedItems() != null && getSelectedItems().size() > 0) {
                for (int i = 0; i < getSelectedItems().size(); i++) {
                    callDeleteImageService(dataList.get(getSelectedItems().get(i)).getUserId(), dataList.get(getSelectedItems().get(i)).getImages(),i,getSelectedItems().size());
                }
            }
        }
        if (id == R.id.action_edit) {
            if(getSelectedItems() != null && getSelectedItems().size() > 0) {
                Intent i = new Intent(context, UpdateInfoActivity.class);
                i.putExtra("dataListUpdate", dataList.get(getSelectedItems().get(0)));
                context.startActivity(i);
                //((Activity)context).finish();
            }
        }
        if (id == R.id.action_move) {
            if(getSelectedItems() != null && getSelectedItems().size() > 0) {
                    moveToFolderPopUp();
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        viewMenu = menu.findItem(R.id.action_view_img);
        deleteMenu = menu.findItem(R.id.action_delete);
        editMenu = menu.findItem(R.id.action_edit);
        moveMenu = menu.findItem(R.id.action_move);
        if(mAdapter != null){
            if(mAdapter.getSelectedItemCount() == 0){
                deleteMenu.setVisible(false);
                moveMenu.setVisible(false);
                viewMenu.setVisible(false);
                editMenu.setVisible(false);
            }
        }
    }

/*    @Override
    public void onItemClicked (int position) {
        toggleSelection(position);
    }*/

    @Override
    public void onItemClicked(int position) {
//        toggleSelection(position);
//        if(mAdapter != null) {
//            if (mAdapter.getSelectedItemCount() == 0) {
//                deleteMenu.setVisible(false);
//                moveMenu.setVisible(false);
//                viewMenu.setVisible(false);
//                editMenu.setVisible(false);
//            } else if (mAdapter.getSelectedItemCount() > 1) {
//                deleteMenu.setVisible(true);
//                moveMenu.setVisible(true);
//                viewMenu.setVisible(false);
//                editMenu.setVisible(false);
//            } else if (mAdapter.getSelectedItemCount() == 1) {
//                viewMenu.setVisible(true);
//                editMenu.setVisible(true);
//                deleteMenu.setVisible(true);
//                moveMenu.setVisible(true);
//            }
//        }

        if(getSelectedItems() != null && getSelectedItems().size() > 0) {
            Intent intent = new Intent(context, LargeZoomActivity.class);
            // intent.putExtra("imageName", dataList.get(getSelectedItems().get(0)).getImages());
            // intent.putExtra("tag", dataList.get(getSelectedItems().get(0)).getTag());
            Bundle data = new Bundle();
            data.putParcelableArrayList("imageDataList", (ArrayList<? extends Parcelable>) dataList);
            data.putInt("selectedPosition",getSelectedItems().get(0));
            intent.putExtras(data);
            context.startActivity(intent);
            //((Activity)context).finish();
        }
    }

    @Override
    public boolean onItemLongClicked (int position) {
        toggleSelection(position);
        if(mAdapter != null) {
            if (mAdapter.getSelectedItemCount() == 0) {
                deleteMenu.setVisible(false);
                moveMenu.setVisible(false);
                viewMenu.setVisible(false);
                editMenu.setVisible(false);
            } else if (mAdapter.getSelectedItemCount() > 1) {
                deleteMenu.setVisible(true);
                moveMenu.setVisible(true);
                viewMenu.setVisible(false);
                editMenu.setVisible(false);
            } else if (mAdapter.getSelectedItemCount() == 1) {
                viewMenu.setVisible(true);
                editMenu.setVisible(true);
                deleteMenu.setVisible(true);
                moveMenu.setVisible(true);
            }
        }
        return true;
    }

    private void toggleSelection(int position) {
        if(mAdapter != null) {
            mAdapter.toggleSelection(position);
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



    private void callDeleteImageService(String userId, String imageName, final int i , final int size) {
        if(Constants.isNetworkAvailable(context)){
            Log.d("In callDeleteImageService","In callDeleteImageService");
            final ProgressDialog dialog = new ProgressDialog(context);
            dialog.setMessage("Please Wait...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();

            Gson gson = new GsonBuilder().setLenient().create();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build();
            ApiService service = retrofit.create(ApiService.class);
            Call<LoginResponse> call = service.deleteDataService(userId,imageName);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if(response != null && response.body() != null ){
                        if(response.body().getSuccess().toString().trim().equals("1")) {
                            //Toast.makeText(context,"Deleted Successfully...",Toast.LENGTH_LONG).show();
                            if(i == size-1){
                                Toast toast = Toast.makeText(context,"Deleted Successfully...", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                Intent i = new Intent(context,ReviewMyUploadTabActivity.class);
                                context.startActivity(i);
                                ((Activity)context).finish();
                            }
                        }else  if(response.body().getSuccess().toString().trim().equals("0")) {
                            Toast.makeText(context,"Failed to Deleted..",Toast.LENGTH_LONG).show();
                        }else {
                            Log.d("Error dataUploadService ","Error dataUploadService : Technical Error !!!");
                        }
                    }
                }
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Log.d("Exception dataUploadService: ","Exception dataUploadService: "+t.toString());
                }
            });
        }else{
            Toast.makeText(context,"Internet Unavailable,Unable to update data please checck internet connection...",Toast.LENGTH_LONG).show();
        }
    }

    private void moveToFolderPopUp() {
        final Dialog folderPopup = new Dialog(context);
        folderPopup.requestWindowFeature(Window.FEATURE_NO_TITLE);
        folderPopup.setContentView(R.layout.popup_moveto);
        Button btnCancel = (Button) folderPopup.findViewById(R.id.buttonPopupCancel);
        final ListView listview = (ListView) folderPopup.findViewById(R.id.list);
        List<String> speciesfolder = new ArrayList<>();
        speciesfolder = SpeciesPhotoFragment.dataListSpeciesNames;
        if(speciesfolder.get(0).equals("Select Species")){
            speciesfolder.remove(0);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,android.R.layout.simple_list_item_1,speciesfolder);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int positionFolderList, long id) {
                SharedPreferences prefs = context.getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
                String userId = prefs.getString("USERID", "0");
                String toSpecies = (String) listview.getItemAtPosition(positionFolderList);
                for (int i = 0; i < getSelectedItems().size(); i++) {
                    String imageName = dataList.get(getSelectedItems().get(i)).getImages();
                    String fromSpecies = dataList.get(getSelectedItems().get(i)).getSpecies();
                    if(toSpecies.equals(fromSpecies)){
                        Toast.makeText(context,"One of the Image is Already in " + toSpecies,Toast.LENGTH_SHORT).show();
                    }else{
                        //Toast.makeText(context,"You selected : " + item,Toast.LENGTH_SHORT).show();
                        folderPopup.dismiss();
                        moveImageServiceCall(userId,imageName,fromSpecies,toSpecies,getSelectedItems().get(i),i,getSelectedItems().size());
                    }
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                folderPopup.dismiss();
            }
        });
        folderPopup.show();
    }


    private void moveImageServiceCall(String userId, String imageName, String fromSpecies, String toSpecies, final int positionSpec, final int i, final int size) {
        if(Constants.isNetworkAvailable(context)){
            Log.d("In moveImageServiceCall","In moveImageServiceCall");
            final ProgressDialog dialog = new ProgressDialog(context);
            dialog.setMessage("Moving...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
            Gson gson = new GsonBuilder().setLenient().create();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build();
            ApiService service = retrofit.create(ApiService.class);
            Call<LoginResponse> call = service.moveUpdateService(userId,imageName,fromSpecies,toSpecies);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if(response != null && response.body() != null ){
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        if(response.body().getSuccess().toString().trim().equals("1")) {
                            if(i == size-1) {
                                Toast toast = Toast.makeText(context, "Move Successfully...", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                Intent i = new Intent(context, ReviewMyUploadTabActivity.class);
                                context.startActivity(i);
                                ((Activity) context).finish();
                            }

                        }else  if(response.body().getSuccess().toString().trim().equals("0")) {
                            Toast.makeText(context, "Filed to move , please try again", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context, "Technical Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Log.d("Exception dataUploadService: ","Exception dataUploadService: "+t.toString());
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Toast.makeText(context, "Technical Error", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(context,"Internet Unavailable,Unable to update data please checck internet connection...",Toast.LENGTH_LONG).show();
        }
    }

}
