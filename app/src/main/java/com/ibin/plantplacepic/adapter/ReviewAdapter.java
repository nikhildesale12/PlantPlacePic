package com.ibin.plantplacepic.adapter;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.IDNA;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.activities.LargeZoomActivity;
import com.ibin.plantplacepic.activities.ReviewMyUpload;
import com.ibin.plantplacepic.activities.ReviewMyUploadTabActivity;
import com.ibin.plantplacepic.activities.SpeciesInfoActivity;
import com.ibin.plantplacepic.activities.UpdateInfoActivity;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.InformationResponseBean;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.fragment.ImagesFragment;
import com.ibin.plantplacepic.fragment.SpeciesPhotoFragment;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.R.id.list;
import static android.content.Context.MODE_PRIVATE;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {
    Context context ;
    private List<Information> dataListSameSpecies;
    DatabaseHelper databaseHelper;
    String SearchByName = "";
    public static boolean moveFlag = false;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textSpecies;
        public TextView textTitle;
        public TextView textRemark;
        public TextView textTag;
        public ImageView imageView ;
        public ImageView menuButton ;
        public RelativeLayout recyclerItem;

        public MyViewHolder(View view) {
            super(view);
            textSpecies = (TextView) view.findViewById(R.id.textSpeciesReview);
            textTitle = (TextView) view.findViewById(R.id.textTitleReview);
            textRemark = (TextView) view.findViewById(R.id.textRemarkReview);
            textTag = (TextView) view.findViewById(R.id.textTagReview);
            imageView = (ImageView) view.findViewById(R.id.imageViewReview);
            menuButton = (ImageView) view.findViewById(R.id.menuButton);
            recyclerItem = (RelativeLayout) view.findViewById(R.id.recyclerItem);
        }
    }


    public ReviewAdapter(List<Information> dataListSameSpecies, Context context , String SearchByName) {
        this.dataListSameSpecies = dataListSameSpecies;
        this.context = context ;
        this.SearchByName = SearchByName;
        moveFlag = false;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if(SearchByName.equals("SearchByName")){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_species_by_name_details, parent, false);
        }else{
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_details, parent, false);
        }
        databaseHelper = DatabaseHelper.getDatabaseInstance(context);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Information review = dataListSameSpecies.get(position);
        if(review.getSpecies().length()>0){
            holder.textSpecies.setText("SPECIES : "+review.getSpecies());
        }else{
            holder.textSpecies.setText("SPECIES : NA");
        }
        if(review.getRemark().length()>0){
            holder.textRemark.setText("REMARK : "+review.getRemark());
        }else{
            holder.textRemark.setText("REMARK : NA");
        }
        if(review.getTitle().length()>0){
            holder.textTitle.setText("TITLE : "+review.getTitle());
        }else{
            holder.textTitle.setText("TITLE : NA");
        }
        if(review.getTag().length()>0){
            holder.textTag.setText("TAG : "+review.getTag());
        }else{
            holder.textTag.setText("TAG : NA");
        }

        String imageFolderPath ="";
        /*if(review.getTag().equals(Constants.TAG_TREE)){
            imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH_TAG_TREE;
        }else if(review.getTag().equals(Constants.TAG_LEAF)){
            imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH_TAG_LEAF;
        }else if(review.getTag().equals(Constants.TAG_FLOWER)){
            imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH_TAG_FLOWER;
        }else  if(review.getTag().equals(Constants.TAG_FRUIT)){
            imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH_TAG_FRUIT;
        }else {
            imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH;
        }*/
        imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH;
        File file = new File(Constants.FOLDER_PATH + File.separator + review.getImages());
//        if(file != null && file.exists()){
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 8;
//            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);

//            Bitmap bitmap = getBitmap(file);
//            holder.imageView.setImageBitmap(bitmap);

//        }else{
//            Picasso.with(context)
//                    .load(imageFolderPath+review.getImages())
//                    .placeholder(R.drawable.pleasewait)
//                    .error(R.drawable.pleasewait)
//                    .into(getTarget(review.getImages()));
//            Glide.with(context)
//                    .load(imageFolderPath+review.getImages())
//                    .placeholder(R.drawable.pleasewait)
//                    .error(R.drawable.pleasewait)
//                    .thumbnail(0.5f)
//                    .crossFade()
//                    .into(holder.imageView);
//        }

        Glide.with(context)
                .load(imageFolderPath+review.getImages())
                .placeholder(R.drawable.pleasewait)
                .error(R.drawable.pleasewait)
                .thumbnail(0.5f)
                .crossFade()
                .into(holder.imageView);

        Picasso.with(context)
                .load(imageFolderPath+review.getImages())
                .placeholder(R.drawable.pleasewait)
                .error(R.drawable.pleasewait)
                .into(getTarget(review.getImages()));

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(context,LargeZoomActivity.class);
//                intent.putExtra("imageName",dataListSameSpecies.get(position).getImages());
//                intent.putExtra("tag",dataListSameSpecies.get(position).getTag());
//                context.startActivity(intent);
                if(SearchByName.equals("SearchByName")){
                    //not open large image
                }else {
                    Intent intent = new Intent(context, LargeZoomActivity.class);
                    Bundle data = new Bundle();
                    data.putParcelableArrayList("imageDataList", (ArrayList<? extends Parcelable>) dataListSameSpecies);
                    data.putInt("selectedPosition", position);
                    intent.putExtras(data);
                    context.startActivity(intent);
                }
            }
        });

        if(SearchByName.equals("SearchByName"))
            holder.recyclerItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(context, "posiTION : " + position + "Specis Name : " + dataListSameSpecies.get(position).getSpecies(), Toast.LENGTH_SHORT).show();
                    /*Intent intent = new Intent(context, ImagesFragment.class);
                    intent.putExtra("selectedPosition", position);
                    intent.putExtra("Species Name", dataListSameSpecies.get(position).getImages());
                    context.startActivity(intent);*/
                }
            });
        if(!SearchByName.equals("SearchByName")) {
            holder.menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(context, holder.menuButton);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.popupmenu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                            /*case R.id.action_view_image_popup:
                                Intent intent = new Intent(context,LargeZoomActivity.class);
                                intent.putExtra("imageName",dataListSameSpecies.get(position).getImages());
                                intent.putExtra("tag",dataListSameSpecies.get(position).getTag());
                                context.startActivity(intent);
                                break;*/
                                case R.id.action_delete_popup:
                                    //Toast.makeText(context,"delete",Toast.LENGTH_SHORT).show();
                                    callDeleteService(dataListSameSpecies.get(position).getUserId(), dataListSameSpecies.get(position).getImages(), position);
                                    break;
                                case R.id.action_edit_popup:
                                    //Toast.makeText(context,"edit",Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(context, UpdateInfoActivity.class);
                                    i.putExtra("dataListUpdate", dataListSameSpecies.get(position));
                                    context.startActivity(i);
                                    //((Activity)context).finish();
                                    break;
                                case R.id.action_move_popup:
                                    ///Toast.makeText(context,"move",Toast.LENGTH_SHORT).show();
                                    moveToFolderPopUp(position);
                                    break;
                            }
                            return false;
                        }
                    });
                    //displaying the popup
                    popup.show();

                }
            });
        }
    }


    private Bitmap getBitmap(File path) {
        String TAG = "";
        Uri uri = Uri.fromFile(path);
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = context.getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();

            // int scale = 1;
//            while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) >
//                    IMAGE_MAX_SIZE) {
//                scale++;
//            }
//            Log.d(TAG, "scale = " + scale + ", orig-width: " + options.outWidth + ",orig-height: " + options.outHeight);

            Bitmap resultBitmap = null;
            in = context.getContentResolver().openInputStream(uri);
            // scale to max possible inSampleSize that still yields an image
            // larger than target
            options = new BitmapFactory.Options();
            options.inSampleSize = calculateInSampleSize(options,options.outWidth,options.outHeight);
            resultBitmap = BitmapFactory.decodeStream(in, null, options);

            // resize to desired dimensions
            int height = resultBitmap.getHeight();
            int width = resultBitmap.getWidth();
            Log.d(TAG, "1th scale operation dimenions - width: " + width + ",height: " + height);

            double y = Math.sqrt(IMAGE_MAX_SIZE
                    / (((double) width) / height));
            double x = (y / height) * width;

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(resultBitmap, (int) x,
                    (int) y, true);
            resultBitmap.recycle();
            resultBitmap = scaledBitmap;

            System.gc();
            in.close();

            Log.d(TAG, "bitmap size - width: " +resultBitmap.getWidth() + ", height: " +
                    resultBitmap.getHeight());
            return resultBitmap;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(),e);
            return null;
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    //target to save
    private static Target getTarget(final String imageName){
        Target target = new Target(){
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        //File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + url);
                        File rootDirectory = new File(Constants.FOLDER_PATH);
                        if (!rootDirectory.exists()) {
                            rootDirectory.mkdir();
                        }
                        File file = new File(Constants.FOLDER_PATH + File.separator + imageName);
                        try {
                            if(file != null && !file.exists()) {
                                file.createNewFile();
                                FileOutputStream ostream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                                ostream.flush();
                                ostream.close();
                            }
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();

            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return target;
    }

    @Override
    public int getItemCount() {
        return dataListSameSpecies.size();
    }

    private void callDeleteService(String userId, final String ImageName, final int position) {
        if(Constants.isNetworkAvailable(context)){
            Log.d("In callDeleteService","In callDeleteService");
            final ProgressDialog dialog = new ProgressDialog(context);
            dialog.setMessage("Please Wait...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();

            Gson gson = new GsonBuilder().setLenient().create();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build();
            ApiService service = retrofit.create(ApiService.class);
            Call<LoginResponse> call = service.deleteDataService(userId,ImageName);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if(response != null && response.body() != null ){
                        if(response.body().getSuccess().toString().trim().equals("1")) {
                            Toast toast = Toast.makeText(context,"Deleted Successfully...", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            //delete from local
                            SharedPreferences prefs = context.getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
                            String userId = prefs.getString("USERID", "0");
                            databaseHelper.deleteSaveDataFromLocal(ImageName,userId);
                            Intent i = new Intent(context,ReviewMyUploadTabActivity.class);
                            context.startActivity(i);
                            ((Activity)context).finish();
//                            dataListSameSpecies.remove(position);
//                            ReviewMyUpload.recyclerView.removeViewAt(position);
//                            ReviewMyUpload.mAdapter.notifyItemRemoved(position);
//                            ReviewMyUpload.mAdapter.notifyItemRangeChanged(position, dataListSameSpecies.size());
                            /*remove from list for second time*/
                            /*refresh species folder lis starts*/
//                            if(dataListSameSpecies.size() == 0){
//                                for(int i=0;i<SpeciesPhotoFragment.dataListSpeciesNames.size();i++){
//                                    if(ReviewMyUpload.speciesName.equals(SpeciesPhotoFragment.dataListSpeciesNames.get(i))){
//                                        SpeciesPhotoFragment.dataListSpeciesNames.remove(i);
//                                        SpeciesPhotoFragment.recyclerViewSpecies.removeViewAt(i);
//                                        SpeciesPhotoFragment.mAdapter.notifyItemRemoved(i);
//                                        SpeciesPhotoFragment.mAdapter.notifyItemRangeChanged(i, SpeciesPhotoFragment.dataListSpeciesNames.size());
//                                    }
//                                }
//                            }
//                            SharedPreferences prefs = context.getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE);
//                            String userId = prefs.getString(Constants.KEY_USERID, "0");
//                            callSereviceToRefreshSpeciesFolder(userId);
                            /*refresh species folder lis end*/
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

    private void moveToFolderPopUp(final int positionSameSpec) {
        final Dialog folderPopup = new Dialog(context);
        folderPopup.requestWindowFeature(Window.FEATURE_NO_TITLE);
        folderPopup.setContentView(R.layout.popup_moveto);
        Button btnCancel = (Button) folderPopup.findViewById(R.id.buttonPopupCancel);
        final ListView listview = (ListView) folderPopup.findViewById(R.id.list);
        List<String> speciesfolder = new ArrayList<>();
        speciesfolder = SpeciesPhotoFragment.dataListSpeciesNames;
        //speciesfolder.remove(0);
        if(speciesfolder.get(0).equals("Select Species")){
            speciesfolder.remove(0);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,android.R.layout.simple_list_item_1,speciesfolder);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences prefs = context.getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
                String userId = prefs.getString("USERID", "0");
                String imageName = dataListSameSpecies.get(positionSameSpec).getImages();
                String fromSpecies = dataListSameSpecies.get(positionSameSpec).getSpecies();
                String toSpecies = (String) listview.getItemAtPosition(position);

                if(toSpecies.equals(fromSpecies)){
                    Toast.makeText(context,"Already in " + toSpecies,Toast.LENGTH_SHORT).show();
                }else{
                    //Toast.makeText(context,"You selected : " + item,Toast.LENGTH_SHORT).show();
                    folderPopup.dismiss();
                    moveImageServiceCall(userId,imageName,fromSpecies,toSpecies,positionSameSpec);
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

    private void moveImageServiceCall(String userId, String imageName, String fromSpecies, String toSpecies, final int positionSpec) {
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
//                            dataListSameSpecies.remove(positionSpec);
//                            ReviewMyUpload.recyclerView.removeViewAt(positionSpec);
//                            ReviewMyUpload.mAdapter.notifyItemRemoved(positionSpec);
//                            ReviewMyUpload.mAdapter.notifyItemRangeChanged(positionSpec, dataListSameSpecies.size());
                            Toast toast = Toast.makeText(context,"Move Successfully...", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            //move in local db
                            //databaseHelper.moveSpeciesLocal(ImageName,userId);
                            Intent i = new Intent(context,ReviewMyUploadTabActivity.class);
                            context.startActivity(i);
                            ((Activity)context).finish();

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

    /*private void callSereviceToRefreshSpeciesFolder(String userId) {
        ReviewMyUploadTabActivity.reviewList = new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        ApiService service = retrofit.create(ApiService.class);
        Call<InformationResponseBean> call = service.downloadDataById(userId);
        call.enqueue(new Callback<InformationResponseBean>() {
            @Override
            public void onResponse(Call<InformationResponseBean> call, Response<InformationResponseBean> response) {
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
                                ReviewMyUploadTabActivity.reviewList.add(information);
                                SpeciesPhotoFragment.dataList = new ArrayList<>();
                            }
                            //refresh list
                            //SpeciesPhotoFragment.mAdapter.notifyDataSetChanged();
                        }
                    } else if (response.body().getSuccess().toString().trim().equals("0")) {
                    } else {
                    }
                }else {
                }
            }
            @Override
            public void onFailure(Call<InformationResponseBean> call, Throwable t) {
            }
        });
    }*/
}