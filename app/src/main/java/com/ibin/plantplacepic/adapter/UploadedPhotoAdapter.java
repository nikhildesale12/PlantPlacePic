package com.ibin.plantplacepic.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.SelectableAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class UploadedPhotoAdapter extends SelectableAdapter<UploadedPhotoAdapter.MyViewHolder> {

    private List<Information> reviewList;
    private Context mContext;
    private MyViewHolder.ClickListener clickListener;

    public UploadedPhotoAdapter(Context context, ArrayList<Information> reviewList,UploadedPhotoAdapter.MyViewHolder.ClickListener clickListener) {
        mContext = context;
        this.reviewList = reviewList;
        this.clickListener = clickListener;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_uploaded_photo_item, parent, false);

        return new MyViewHolder(itemView,clickListener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String imageFolderPath ="";
        imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH;
        /*Glide.with(mContext)
                .load(imageFolderPath+reviewList.get(position).getImages())
                .placeholder(R.drawable.pleasewait)
                .error(R.drawable.pleasewait)
                .into(holder.imageView);*/
        File rootDirectory = new File(Constants.FOLDER_PATH);
        if (!rootDirectory.exists()) {
            rootDirectory.mkdir();
        }

        Glide.with(mContext)
                    .load(imageFolderPath+reviewList.get(position).getImages())
                    .placeholder(R.drawable.pleasewait)
                    .error(R.drawable.pleasewait)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);

        Picasso.with(mContext)
                .load(imageFolderPath+reviewList.get(position).getImages())
                .placeholder(R.drawable.pleasewait)   // optional
                .error(R.drawable.pleasewait)
                .into(getTarget(reviewList.get(position).getImages()));

//        File file = new File(Constants.FOLDER_PATH + File.separator + reviewList.get(position).getImages());
//        if(file != null && file.exists()){
//            //holder.imageView.setImageURI(Uri.fromFile(file));
//             new LoadImage(holder.imageView, file).execute();
//        }else{
//            Picasso.with(mContext)
//                    .load(imageFolderPath+reviewList.get(position).getImages())
//                    .placeholder(R.drawable.pleasewait)   // optional
//                    .error(R.drawable.pleasewait)
//                    //.resize(200,200)             // optional
//                    .into(getTarget(reviewList.get(position).getImages()));
//            Glide.with(mContext)
//                    .load(imageFolderPath+reviewList.get(position).getImages())
//                    .placeholder(R.drawable.pleasewait)
//                    .error(R.drawable.pleasewait)
//                    .into(holder.imageView);
//        }
        holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);

      /*String tag = reviewList.get(position).getTag();
        if(tag.equals(Constants.TAG_TREE)){
            imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH_TAG_TREE;
        }else if(tag.equals(Constants.TAG_LEAF)){
            imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH_TAG_LEAF;
        }else if(tag.equals(Constants.TAG_FLOWER)){
            imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH_TAG_FLOWER;
        }else  if(tag.equals(Constants.TAG_FRUIT)){
            imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH_TAG_FRUIT;
        }else {
            imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH;
        }*/
    }

    class LoadImage extends AsyncTask<Object, Void, File> {

        private ImageView imv;
        private File file = null;

        public LoadImage(ImageView imv,File file) {
            this.imv = imv;
            this.file = file;
        }

        @Override
        protected File doInBackground(Object... params) {
            if(file != null && imv != null && file.exists()){
                imv.post(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = getBitmap(file);
                        imv.setImageBitmap(bitmap);
                    }
                });

                //return file;
            }
            return null;
        }
        @Override
        protected void onPostExecute(File file) {
            if(file != null && imv != null){
                //imv.setImageURI(Uri.fromFile(file));
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 5;
//                options.inScaled = true;
//                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);


            }
        }
    }

    private Bitmap getBitmap(File path) {
        String TAG = "";
        Uri uri = Uri.fromFile(path);
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = mContext.getContentResolver().openInputStream(uri);

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
            in = mContext.getContentResolver().openInputStream(uri);
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
                        File file = new File(Constants.FOLDER_PATH + File.separator + imageName);
                        try {
                            if(file != null && !file.exists()) {
                                file.createNewFile();
                                FileOutputStream ostream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, ostream);
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
        return reviewList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{

        public ImageView imageView;
        private ClickListener listener;
        private final View selectedOverlay;

        public MyViewHolder(View view,ClickListener listener) {
            super(view);
            this.listener = listener;
            imageView = (ImageView) view.findViewById(R.id.imageViewUploadedPhotoes);
            selectedOverlay = (View) itemView.findViewById(R.id.selected_overlay);
            view.setOnClickListener(this);
            view.setOnLongClickListener (this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(getAdapterPosition ());
            }
        }
        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                return listener.onItemLongClicked(getAdapterPosition ());
            }
            return false;
        }

        public interface ClickListener {
            public void onItemClicked(int position);
            public boolean onItemLongClicked(int position);
        }
    }

   /* public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        }
        else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }*/

}
