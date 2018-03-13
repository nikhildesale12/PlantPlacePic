/*
 * Copyright (c) 2017. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package com.ibin.plantplacepic.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.activities.LargeZoomActivity;
import com.ibin.plantplacepic.activities.MountingBoardActivity;
import com.ibin.plantplacepic.bean.CommentResponse;
import com.ibin.plantplacepic.bean.CommentResponseBean;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.DataObjectHolder> {
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    private static MyClickListener myClickListener;
    List<Information> informationList ;
    Activity activity ;
    DatabaseHelper databaseHelper;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    private List<Information> dataListSameSpecies;
    public static class DataObjectHolder extends RecyclerView.ViewHolder {//implements View.OnClickListener
        EditText textScientificName;
        TextView uploadedDate;
        ImageView buttonLike;
        ImageView buttonDislike;
        ImageView buttonImageComment;
        TextView likeCount;
        TextView dislikeCount;
        ImageView editSpeciesMounting;
        TextView textUserName;
        TextView textCommonName;
        LinearLayout layoutCommonName;
        ImageView imageSpecies;
        TextView textLikeCount , textDisLikeCount;
        ImageView imagePicType ;

        public DataObjectHolder(View itemView) {
            super(itemView);
            textScientificName = (EditText) itemView.findViewById(R.id.textScientificName);
            uploadedDate = (TextView) itemView.findViewById(R.id.uploadedDate);
            buttonLike = (ImageView) itemView.findViewById(R.id.buttonLike) ;
            buttonDislike = (ImageView) itemView.findViewById(R.id.buttonDislike) ;
            buttonImageComment= (ImageView) itemView.findViewById(R.id.buttonImageComment);
            likeCount= (TextView) itemView.findViewById(R.id.like_count);
            dislikeCount= (TextView) itemView.findViewById(R.id.dislike_count);
            editSpeciesMounting= (ImageView) itemView.findViewById(R.id.editSpeciesMounting);
            textUserName = (TextView) itemView.findViewById(R.id.textUserName);
            textCommonName = (TextView) itemView.findViewById(R.id.textCommonName);
            layoutCommonName = (LinearLayout) itemView.findViewById(R.id.layoutCommonName);
            imageSpecies = (ImageView) itemView.findViewById(R.id.imageSpeciesMb);
            textLikeCount = (TextView) itemView.findViewById(R.id.like_count);
            textDisLikeCount = (TextView) itemView.findViewById(R.id.dislike_count);
            imagePicType = (ImageView)  itemView.findViewById(R.id.imagePicType);
            Log.i(LOG_TAG, "Adding Listener");
            //itemView.setOnClickListener(this);
        }

        /*@Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }*/
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public MyRecyclerViewAdapter(List<Information> informationList,Activity activity) {
        this.informationList = informationList;
        this.activity = activity;
        databaseHelper = DatabaseHelper.getDatabaseInstance(activity);
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_row, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        holder.textScientificName.setPaintFlags(holder.textScientificName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if(informationList.size() > 0){
            holder.textScientificName.setText(informationList.get(position).getSpecies());
            String[] date = informationList.get(position).getTime().split("_");
            holder.uploadedDate.setText(date[0]);
            holder.textUserName.setText(informationList.get(position).getUserName());
            if(informationList.get(position).getTitle() != null && informationList.get(position).getTitle().length()>0){
                holder.textCommonName.setText(informationList.get(position).getTitle());
            }else{
                holder.textCommonName.setVisibility(View.GONE);
                holder.layoutCommonName.setVisibility(View.GONE);
            }
            holder.likeCount.setText(informationList.get(position).getLikeCount());
            holder.dislikeCount.setText(informationList.get(position).getDisLikeCount());
            if(informationList.get(position).getLike() != null && informationList.get(position).getLike().equals("1")){
                holder.buttonLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_selected));
            }else if(informationList.get(position).getDisLike() != null && informationList.get(position).getDisLike().equals("1")){
                holder.buttonDislike.setImageDrawable(activity.getResources().getDrawable(R.drawable.dislike_selected));
            }
            File file = new File(Constants.FOLDER_PATH + File.separator + informationList.get(position).getImages());
            if(file != null && file.exists()){
                // imageView.setImageURI(Uri.fromFile(file));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);
                holder.imageSpecies.setImageBitmap(bitmap);
            }else{

                Picasso.with(activity)
                        .load(Constants.IMAGE_DOWNLOAD_PATH + informationList.get(position).getImages())
                        .into(holder.imageSpecies);
                Picasso.with(activity)
                        .load(Constants.IMAGE_DOWNLOAD_PATH + informationList.get(position).getImages())
                        .into(getTarget(informationList.get(position).getImages()));
            }

            holder.buttonLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.buttonDislike.getDrawable().getConstantState() == activity.getResources().getDrawable(R.drawable.dislike).getConstantState()) {
                        if (holder.buttonLike.getDrawable().getConstantState() == activity.getResources().getDrawable(R.drawable.like_normal).getConstantState()) {
                            int count = Integer.parseInt(holder.textLikeCount.getText().toString()) + 1;
                            holder.buttonLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_selected));
                            holder.textLikeCount.setText("" + count);
                            //Like operation = 1
                            CallUpdateLike(informationList.get(position).getImages(),"1");
                        } else {
                            int count = Integer.parseInt(holder.textLikeCount.getText().toString()) - 1;
                            holder.buttonLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_normal));
                            holder.textLikeCount.setText("" + count);
                            //Like operation = 0
                            CallUpdateLike(informationList.get(position).getImages(),"0");
                        }
                    }
                }
            });
            holder.buttonDislike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.buttonLike.getDrawable().getConstantState() == activity.getResources().getDrawable(R.drawable.like_normal).getConstantState()) {
                        if (holder.buttonDislike.getDrawable().getConstantState() == activity.getResources().getDrawable(R.drawable.dislike).getConstantState()) {
                            holder.buttonDislike.setImageDrawable(activity.getResources().getDrawable(R.drawable.dislike_selected));
                            int count = Integer.parseInt(holder.textDisLikeCount.getText().toString()) + 1;
                            holder.textDisLikeCount.setText("" + count);
                            CallUpdateDisLike(informationList.get(position).getImages(),"1");
                        } else {
                            int count = Integer.parseInt(holder.textDisLikeCount.getText().toString()) - 1;
                            holder.buttonDislike.setImageDrawable(activity.getResources().getDrawable(R.drawable.dislike));
                            holder.textDisLikeCount.setText("" + count);
                            CallUpdateDisLike(informationList.get(position).getImages(),"0");
                        }
                    }
                }
            });
            holder.buttonImageComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog commentDialog = new Dialog(activity);
                    commentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    commentDialog.setContentView(R.layout.popup_comment);
                    Button submitComment  = (Button) commentDialog.findViewById(R.id.submitComment);
                    Button cancelComment  = (Button) commentDialog.findViewById(R.id.cancelComment);
                    final LinearLayout layoutNoComment = (LinearLayout) commentDialog.findViewById(R.id.layoutNoComment);
                    mRecyclerView = (RecyclerView) commentDialog.findViewById(R.id.comment_recycler_view);
                    mRecyclerView.setHasFixedSize(true);
                    mLayoutManager = new LinearLayoutManager(activity);

                    getCommentData(position,layoutNoComment);

                    final EditText editComment  = (EditText) commentDialog.findViewById(R.id.textComment);
                    submitComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Toast.makeText(activity, editComment.getText().toString(), Toast.LENGTH_SHORT).show();
                            if(editComment.getText().toString().length()>0){
                                View view = activity.getCurrentFocus();
                                if (view != null) {
                                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }
                                executeCommentService(informationList.get(position).getImages(),editComment.getText().toString(),commentDialog);
                            }else{
                                Toast.makeText(activity,"Please enter comment",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    cancelComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            commentDialog.dismiss();
                        }
                    });
                    commentDialog.show();
                }
            });

            holder.editSpeciesMounting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Toast.makeText(context, "edit", Toast.LENGTH_SHORT).show();
                    if(holder.textScientificName.isEnabled()){
                        holder.textScientificName.setEnabled(false);
                        String toSpecies = holder.textScientificName.getText().toString();
                        String fromSpecies = informationList.get(position).getSpecies();
                        String imageName = informationList.get(position).getImages();
                        if(toSpecies.length() >0){
                            renameSpecies(imageName,fromSpecies,toSpecies);
                        }else{
                            holder.textScientificName.requestFocus();
                            holder.textScientificName.setError("Please Enter Species Name");
                        }
                    }else{
                        holder.textScientificName.setEnabled(true);
                        final int sdk = android.os.Build.VERSION.SDK_INT;
                        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            holder.editSpeciesMounting.setBackgroundDrawable( activity.getResources().getDrawable(R.drawable.checkmark) );
                            holder.textScientificName.setBackgroundDrawable( activity.getResources().getDrawable(R.drawable.edittextbg));
                        } else {
                            holder.editSpeciesMounting.setBackground( activity.getResources().getDrawable(R.drawable.checkmark));
                            holder.textScientificName.setBackground( activity.getResources().getDrawable(R.drawable.edittextbg));
                        }
                        holder.editSpeciesMounting.setPadding(10,8,10,8);
                    }
                }
            });

            holder.imageSpecies.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dataListSameSpecies = new ArrayList<>();
                    Information information = new Information();
                    information.setImages(informationList.get(position).getImages());
                    dataListSameSpecies.add(information);
                    Intent intent = new Intent(activity, LargeZoomActivity.class);
                    Bundle data = new Bundle();
                    data.putParcelableArrayList("imageDataList", (ArrayList<? extends Parcelable>) dataListSameSpecies);
                    intent.putExtras(data);
                    activity.startActivity(intent);
                }
            });

//            holder.imagePicType.setVisibility(View.VISIBLE);
//            holder.imagePicType.setImageDrawable(activity.getResources().getDrawable(R.drawable.leafdisplayicon));
            if(informationList.get(position).getTag() != null){
                if(informationList.get(position).getTag().equals("LEAF")){
                    holder.imagePicType.setVisibility(View.VISIBLE);
                    holder.imagePicType.setImageDrawable(activity.getResources().getDrawable(R.drawable.leafdisplayicon));
                }else if(informationList.get(position).getTag().equals("FLOWER")){
                    holder.imagePicType.setVisibility(View.VISIBLE);
                    holder.imagePicType.setImageDrawable(activity.getResources().getDrawable(R.drawable.flowerdisplayicon));
                } else if(informationList.get(position).getTag().equals("TREE")){
                    holder.imagePicType.setVisibility(View.VISIBLE);
                    holder.imagePicType.setImageDrawable(activity.getResources().getDrawable(R.drawable.tree));
                } else if(informationList.get(position).getTag().equals("FRUIT")){
                    holder.imagePicType.setVisibility(View.VISIBLE);
                    holder.imagePicType.setImageDrawable(activity.getResources().getDrawable(R.drawable.apple));
                } else{
                    holder.imagePicType.setVisibility(View.GONE);
                }
            }
        }
    }

    private void getCommentData(int position, final LinearLayout layoutNoComment) {
        if(Constants.isNetworkAvailable(activity)){
            final ProgressDialog dialog = new ProgressDialog(activity);
            dialog.setMessage("Please Wait...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            ApiService service = retrofit.create(ApiService.class);
            Call<CommentResponse> call = service.getAllComments(informationList.get(position).getImages());
            call.enqueue(new Callback<CommentResponse>() {
                @Override
                public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    if (response != null && response.body() != null) {
                        if (response.body().getSuccess().toString().trim().equals("1")) {
                            if(response.body().getInformation() != null){
                                layoutNoComment.setVisibility(View.GONE);
                                mRecyclerView.setLayoutManager(mLayoutManager);
                                mAdapter = new MyCommentRecyclerViewAdapter(response.body().getInformation(),activity);
                                mRecyclerView.setAdapter(mAdapter);
                            }else{
                                Log.d("NO records", "NO records --->");
                                layoutNoComment.setVisibility(View.VISIBLE);
                            }
                        } else if (response.body().getSuccess().toString().trim().equals("0")) {
                            Log.d("NO records", "NO records --->");
                            layoutNoComment.setVisibility(View.VISIBLE);
                        } else {
                            Log.d("Technical Error !!!", "Technical Error !!! --->");
                            layoutNoComment.setVisibility(View.VISIBLE);
                        }
                    }else {
                        Log.d("Technical Error !!!", "Technical Error !!! --->");
                        layoutNoComment.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onFailure(Call<CommentResponse> call, Throwable t) {
                    Log.d("error", "error :--->"+t.toString());
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    layoutNoComment.setVisibility(View.VISIBLE);
                }
            });
        }else{
            layoutNoComment.setVisibility(View.VISIBLE);
        }
    }

    private void executeCommentService(String IMAGENAME, String COMMENT, final Dialog commentDialog) {
        if(Constants.isNetworkAvailable(activity)){
            SharedPreferences prefs = activity.getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
            String USERID = prefs.getString("USERID", "0");
            Gson gson = new GsonBuilder().setLenient().create();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build();
            ApiService service = retrofit.create(ApiService.class);
            Call<LoginResponse> call = service.commentService(USERID,IMAGENAME,COMMENT);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if(response != null && response.body() != null ){
                        commentDialog.dismiss();
                        if(response.body().getSuccess().toString().trim().equals("1")) {
//                            Toast toast = Toast.makeText(activity, "Comment", Toast.LENGTH_LONG);
//                            toast.setGravity(Gravity.CENTER, 0, 0);
//                            toast.show();
                        }else  if(response.body().getSuccess().toString().trim().equals("0")) {
                            Toast.makeText(activity, "Filed to post comment , please try again", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(activity, "Technical Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    commentDialog.dismiss();
                    Toast.makeText(activity, "Technical Error", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            commentDialog.dismiss();
            Toast.makeText(activity,"Internet Unavailable,Unable to submit, please check internet connection...",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return informationList.size();
    }

    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }


    private void CallUpdateLike(String IMAGENAME,String LIKE_OP) {
        if(Constants.isNetworkAvailable(activity)){
            SharedPreferences prefs = activity.getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
            String USERID = prefs.getString("USERID", "0");
            Gson gson = new GsonBuilder().setLenient().create();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build();
            ApiService service = retrofit.create(ApiService.class);
            Call<LoginResponse> call = service.updateLikeService(USERID,IMAGENAME,LIKE_OP);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if(response != null && response.body() != null ){
                        if(response.body().getSuccess().toString().trim().equals("1")) {
//                            Toast toast = Toast.makeText(activity, "Like", Toast.LENGTH_LONG);
//                            toast.setGravity(Gravity.CENTER, 0, 0);
//                            toast.show();
                        }else  if(response.body().getSuccess().toString().trim().equals("0")) {
                            Toast.makeText(activity, "Filed to like , please try again", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(activity, "Technical Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(activity, "Technical Error", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(activity,"Internet Unavailable,Unable to submit, please check internet connection...",Toast.LENGTH_LONG).show();
        }
    }

    private void CallUpdateDisLike(String IMAGENAME,String DISLIKE_OP) {
        SharedPreferences prefs = activity.getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
        String USERID = prefs.getString("USERID", "0");
        if(Constants.isNetworkAvailable(activity)){
            Gson gson = new GsonBuilder().setLenient().create();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build();
            ApiService service = retrofit.create(ApiService.class);
            Call<LoginResponse> call = service.updateDisLikeService(USERID,IMAGENAME,DISLIKE_OP);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if(response != null && response.body() != null ){
                        if(response.body().getSuccess().toString().trim().equals("1")) {
//                            Toast toast = Toast.makeText(activity, "DisLike", Toast.LENGTH_LONG);
//                            toast.setGravity(Gravity.CENTER, 0, 0);
//                            toast.show();
                        }else  if(response.body().getSuccess().toString().trim().equals("0")) {
                            Toast.makeText(activity, "Filed to like , please try again", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(activity, "Technical Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(activity, "Technical Error", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(activity,"Internet Unavailable,Unable to submit, please check internet connection...",Toast.LENGTH_LONG).show();
        }
    }

    private Target getTarget(final String imageName){
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
                            if(file != null && !file.exists()){
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


    private void renameSpecies(final String imageName, final String fromSpecies, final String toSpecies){
        if(Constants.isNetworkAvailable(activity)){
            final ProgressDialog dialog = new ProgressDialog(activity);
            dialog.setMessage("Updating...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
            Gson gson = new GsonBuilder().setLenient().create();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build();
            ApiService service = retrofit.create(ApiService.class);
            Call<LoginResponse> call = service.renameSpeciesService(imageName,fromSpecies,toSpecies);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if(response != null && response.body() != null ){
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        if(response.body().getSuccess().toString().trim().equals("1")) {
                            //if(i == size-1) {
                            Toast toast = Toast.makeText(activity, "Updated Successfully...", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            //update it in local
                            databaseHelper.renameSpeciesLocal(imageName,fromSpecies,toSpecies);
                            SharedPreferences prefs = activity.getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
                            String userId = prefs.getString(Constants.KEY_USERID, "0");
                            int result = databaseHelper.moveFolderInLocal(userId,imageName,fromSpecies,toSpecies);
                            Intent i = new Intent(activity, MountingBoardActivity.class);
                            activity.startActivity(i);
                            ((Activity) activity).finish();
                            //}
                        }else  if(response.body().getSuccess().toString().trim().equals("0")) {
                            Toast.makeText(activity, "Filed to rename , please try again", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(activity, "Technical Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Toast.makeText(activity, "Technical Error", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(activity,"Internet Unavailable,Unable to update data please checck internet connection...",Toast.LENGTH_LONG).show();
        }
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            mCircleView.setVisibility(View.VISIBLE);
//            mCircleView.setTextMode(TextMode.TEXT); // show text while spinning
//            mCircleView.setUnitVisible(false);
//            mCircleView.spin();
        }
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            ImageView imageView;
            String imageName = "";
            try {
                URL url = new URL(f_url[0]);
                imageName = f_url[1];
                URLConnection conection = url.openConnection();
                conection.connect();
                int lenghtOfFile = conection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(Constants.FOLDER_PATH + File.separator + imageName);
                byte data[] = new byte[1024];
                long total = 0;
//                mCircleView.stopSpinning();
//                mCircleView.setTextMode(TextMode.PERCENT); // show percent if not spinning
                //mCircleView.setUnitVisible(true);
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return imageName;
        }
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
//            mCircleView.setValueAnimated(Float.parseFloat(progress[0]), 1500);
        }
        @Override
        protected void onPostExecute(String imageName) {
//            mCircleView.setVisibility(View.GONE);
            //imageView.setImageDrawable(Drawable.createFromPath(Constants.FOLDER_PATH + File.separator+imageName));
        }
    }

}