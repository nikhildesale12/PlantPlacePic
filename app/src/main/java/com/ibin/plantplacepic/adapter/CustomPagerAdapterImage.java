package com.ibin.plantplacepic.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.TouchImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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
    LinearLayout likePanel;
    TextView textLikeCount , textDisLikeCount;
    public CustomPagerAdapterImage(Context context, ArrayList<Information> dataList) {
        this.context = context;
        this.dataList = dataList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.image_viewpager, container, false);
        TouchImageView imageView = (TouchImageView) itemView.findViewById(R.id.largeimage);
        buttonCommment = (TextView) itemView.findViewById(R.id.buttonImageComment);
        textLikeCount = (TextView) itemView.findViewById(R.id.like_count);
        textDisLikeCount = (TextView) itemView.findViewById(R.id.dislike_count);
        buttonLike = (ImageView) itemView.findViewById(R.id.buttonLike);
        buttonDisLike = (ImageView) itemView.findViewById(R.id.buttonDislike);
        likePanel = (LinearLayout) itemView.findViewById(R.id.likePanel);
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
            Picasso.with(context)
                    .load(Constants.IMAGE_DOWNLOAD_PATH + dataList.get(position).getImages())
                    .placeholder(R.drawable.pleasewait)   // optional
                    .error(R.drawable.pleasewait)
                    //.resize(200,200)             // optional
                    .into(imageView);
            Picasso.with(context)
                    .load(Constants.IMAGE_DOWNLOAD_PATH + dataList.get(position).getImages())
                    .placeholder(R.drawable.pleasewait)   // optional
                    .error(R.drawable.pleasewait)
                    //.resize(200,200)             // optional
                    .into(getTarget(dataList.get(position).getImages()));
        }
        container.addView(itemView);

        //listening to image click
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "you clicked image " + (position + 1), Toast.LENGTH_LONG).show();
            }
        });

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
        });
        return itemView;
    }

  /*  public Bitmap getScaleBitmapFromFile(String pathName, int inSampleSize) {
        if (inSampleSize > 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPurgeable = true;
            options.inSampleSize = inSampleSize;
            return BitmapFactory.decodeFile(pathName, options);
        }
        return BitmapFactory.decodeFile(pathName);
    }
*/
   /* private Bitmap decodeSampledBitmapFromResource(Resources resources, int imageViewUploadedPhotoes, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, imageViewUploadedPhotoes, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, imageViewUploadedPhotoes, options);
    }*/

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
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
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
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
