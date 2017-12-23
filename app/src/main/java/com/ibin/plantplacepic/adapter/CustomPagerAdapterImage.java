package com.ibin.plantplacepic.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.text.InputType;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.activities.ReviewMyUploadTabActivity;
import com.ibin.plantplacepic.activities.SpeciesSearchActivity;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.TouchImageView;
import com.ibin.plantplacepic.utility.progressview.CircleProgressView;
import com.ibin.plantplacepic.utility.progressview.TextMode;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by NN on 27/07/2017.
 */

public class CustomPagerAdapterImage extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;
    ArrayList<Information> dataList = null;
    ImageView buttonLike,buttonDisLike;
    TextView buttonCommment;
    private Dialog commentDialog;
    CircleProgressView mCircleView;
    LinearLayout likePanel;
    TouchImageView imageView;
    TextView textLikeCount , textDisLikeCount;
    String whiteBg = "";
    Activity activity ;
    EditText editTextSpeciesName;
    ImageView buttonEditSpeciesName;
    //ImageView buttonSaveSpeciesName;
    DatabaseHelper databaseHelper;
    boolean isHideEditSpecies;
    String toSpecies;
    String fromSpecies;
    String imageName;

    public CustomPagerAdapterImage(Context context, ArrayList<Information> dataList , String whiteBg ,Activity activity,boolean isHideEditSpecies) {
        this.context = context;
        this.dataList = dataList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.whiteBg = whiteBg;
        this.activity = activity ;
        databaseHelper = DatabaseHelper.getDatabaseInstance(context);
        this.isHideEditSpecies = isHideEditSpecies;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = layoutInflater.inflate(R.layout.image_viewpager, container, false);
        imageView = (TouchImageView) itemView.findViewById(R.id.largeimage);
        if(whiteBg.equals("whiteBg")){
            imageView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        editTextSpeciesName=(EditText) itemView.findViewById(R.id.editTextSpeciesName);
        buttonEditSpeciesName=(ImageView) itemView.findViewById(R.id.buttonEditSpeciesName);
        //buttonSaveSpeciesName=(ImageView)itemView.findViewById(R.id.buttonSaveSpeciesName);
        if(isHideEditSpecies){
            editTextSpeciesName.setVisibility(View.GONE);
            buttonEditSpeciesName.setVisibility(View.GONE);
        }else{
            editTextSpeciesName.setVisibility(View.VISIBLE);
            buttonEditSpeciesName.setVisibility(View.VISIBLE);
        }
        buttonCommment = (TextView) itemView.findViewById(R.id.buttonImageComment);
        textLikeCount = (TextView) itemView.findViewById(R.id.like_count);
        textDisLikeCount = (TextView) itemView.findViewById(R.id.dislike_count);
        buttonLike = (ImageView) itemView.findViewById(R.id.buttonLike);
        buttonDisLike = (ImageView) itemView.findViewById(R.id.buttonDislike);
        likePanel = (LinearLayout) itemView.findViewById(R.id.likePanel);

        mCircleView = (CircleProgressView) itemView.findViewById(R.id.circleView);
        mCircleView.setShowTextWhileSpinning(true); // Show/hide text in spinning mode
        mCircleView.setText("Loading...");
        mCircleView.setOnProgressChangedListener(new CircleProgressView.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(float value) {
                //Log.d("Download in % : ", "Progress Changed: " + value);
            }
        });
       /* Glide.with(context)
                .load(Constants.IMAGE_DOWNLOAD_PATH+dataList.get(position).getImages())
                .placeholder(R.drawable.pleasewait)   // optional
                .error(R.drawable.pleasewait)
                //.resize(200,200)             // optional
                .into(imageView);*/
        File file = new File(Constants.FOLDER_PATH + File.separator + dataList.get(position).getImages());
        if(file != null && file.exists()){
           // imageView.setImageURI(Uri.fromFile(file));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);
            imageView.setImageBitmap(bitmap);
        }else{
            /*for(float i=1;i <= 100;i++){
                mCircleView.setValueAnimated(i, 1500);
            }*/
            /*Picasso.with(context)
                    .load(Constants.IMAGE_DOWNLOAD_PATH + dataList.get(position).getImages())
                    .into(imageView);*/

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new DownloadFileFromURL().execute(Constants.IMAGE_DOWNLOAD_PATH + dataList.get(position).getImages(),dataList.get(position).getImages());
                }
            });

            Picasso.with(context)
                    .load(Constants.IMAGE_DOWNLOAD_PATH + dataList.get(position).getImages())
                    .into(getTarget(dataList.get(position).getImages()));
        }
        editTextSpeciesName.setText(dataList.get(position).getSpecies());
        container.addView(itemView);

        buttonEditSpeciesName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(context, "edit", Toast.LENGTH_SHORT).show();
                if(editTextSpeciesName.isEnabled()){
                    editTextSpeciesName.setEnabled(false);
                    toSpecies = editTextSpeciesName.getText().toString();
                    fromSpecies = dataList.get(position).getSpecies();
                    imageName = dataList.get(position).getImages();
                    if(toSpecies.length() >0){
                        renameSpecies(imageName,fromSpecies,toSpecies);
                    }else{
                        editTextSpeciesName.requestFocus();
                        editTextSpeciesName.setError("Please Enter Species Name");
                    }
                }else{
                    editTextSpeciesName.setEnabled(true);
                    final int sdk = android.os.Build.VERSION.SDK_INT;
                    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        buttonEditSpeciesName.setBackgroundDrawable( context.getResources().getDrawable(R.drawable.checkmark) );
                        editTextSpeciesName.setBackgroundDrawable( context.getResources().getDrawable(R.drawable.edittextbg));
                    } else {
                        buttonEditSpeciesName.setBackground( context.getResources().getDrawable(R.drawable.checkmark));
                        editTextSpeciesName.setBackground( context.getResources().getDrawable(R.drawable.edittextbg));
                    }
                }
            }
        });
      //  buttonSaveSpeciesName.setOnClickListener(new View.OnClickListener() {
      //      @Override
       //     public void onClick(View v) {
                /*if(toSpecies.length() >0){
                    renameSpecies(imageName,fromSpecies,toSpecies);
                }else{
                    editTextSpeciesName.re  questFocus();
                    editTextSpeciesName.setError("Please Enter Species Name");
                }*/
         //   }
       // });//

        //listening to image click
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "you clicked image " + (position + 1), Toast.LENGTH_LONG).show();
            }
        });
/*
        buttonLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonDisLike.getDrawable().getConstantState() == context.getResources().getDrawable(R.drawable.dislike).getConstantState()) {
                    if (buttonLike.getDrawable().getConstantState() == context.getResources().getDrawable(R.drawable.like_normal).getConstantState()) {
                        int count = Integer.parseInt(textLikeCount.getText().toString()) + 1;
                        buttonLike.setImageDrawable(context.getResources().getDrawable(R.drawable.like_selected));
                        textLikeCount.setText("" + count);
                    } else {
                        int count = Integer.parseInt(textLikeCount.getText().toString()) - 1;
                        buttonLike.setImageDrawable(context.getResources().getDrawable(R.drawable.like_normal));
                        textLikeCount.setText("" + count);
                    }
                }
            }
        });
        buttonDisLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonLike.getDrawable().getConstantState() == context.getResources().getDrawable(R.drawable.like_normal).getConstantState()) {
                    if (buttonDisLike.getDrawable().getConstantState() == context.getResources().getDrawable(R.drawable.dislike).getConstantState()) {
                        buttonDisLike.setImageDrawable(context.getResources().getDrawable(R.drawable.dislike_selected));
                        int count = Integer.parseInt(textDisLikeCount.getText().toString()) + 1;
                        textDisLikeCount.setText("" + count);
                    } else {
                        int count = Integer.parseInt(textDisLikeCount.getText().toString()) - 1;
                        buttonDisLike.setImageDrawable(context.getResources().getDrawable(R.drawable.dislike));
                        textDisLikeCount.setText("" + count);
                    }
                }
            }
        });
        buttonCommment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentDialog=new Dialog(context);
                commentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                commentDialog.setContentView(R.layout.popup_comment);
                Button submitComment  = (Button) commentDialog.findViewById(R.id.submitComment);
                final EditText editComment  = (EditText) commentDialog.findViewById(R.id.textComment);
                submitComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, editComment.getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                commentDialog.show();
            }
        });*/
        return itemView;
    }

    private void renameSpecies(final String imageName, final String fromSpecies, final String toSpecies){
        if(Constants.isNetworkAvailable(context)){
            final ProgressDialog dialog = new ProgressDialog(context);
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
                                Toast toast = Toast.makeText(context, "Updated Successfully...", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                //move in local db
                                SharedPreferences prefs = context.getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
                                String userId = prefs.getString("USERID", "0");
                                int result = databaseHelper.moveFolderInLocal(userId,imageName,fromSpecies,toSpecies);
                                Intent i = new Intent(context, SpeciesSearchActivity.class);
                                context.startActivity(i);
                                ((Activity) context).finish();
                            //}
                        }else  if(response.body().getSuccess().toString().trim().equals("0")) {
                            Toast.makeText(context, "Filed to rename , please try again", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context, "Technical Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
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
    //target to save
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


    class DownloadFileFromURL extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCircleView.setVisibility(View.VISIBLE);
            mCircleView.setTextMode(TextMode.TEXT); // show text while spinning
            mCircleView.setUnitVisible(false);
            mCircleView.spin();
        }
        @Override
        protected String doInBackground(String... f_url) {
            int count;
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
                mCircleView.stopSpinning();
                mCircleView.setTextMode(TextMode.PERCENT); // show percent if not spinning
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
            mCircleView.setValueAnimated(Float.parseFloat(progress[0]), 1500);
        }
        @Override
        protected void onPostExecute(String imageName) {
            mCircleView.setVisibility(View.GONE);
            imageView.setImageDrawable(Drawable.createFromPath(Constants.FOLDER_PATH + File.separator+imageName));
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
