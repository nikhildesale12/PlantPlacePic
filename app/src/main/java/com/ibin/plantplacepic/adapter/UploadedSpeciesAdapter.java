package com.ibin.plantplacepic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.utility.SelectableAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class UploadedSpeciesAdapter extends SelectableAdapter<UploadedSpeciesAdapter.MyViewHolder> {

    private List<Information> reviewList;
    private Context mContext;
    private UploadedSpeciesAdapter.MyViewHolder.ClickListener clickListener;


    public UploadedSpeciesAdapter(Context context, ArrayList<Information> reviewList,UploadedSpeciesAdapter.MyViewHolder.ClickListener clickListener) {
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
                .inflate(R.layout.row_uploaded_species_item, parent, false);

        return new MyViewHolder(itemView,clickListener);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if(reviewList != null && reviewList.size()>0){
            if(reviewList.get(position).getSpecies() != null && reviewList.get(position).getSpecies().length()>0){
                holder.textViewSpeciesName.setText(reviewList.get(position).getSpecies());
                Picasso.with(mContext)
                        .load(R.mipmap.albamicon)
                        .placeholder(R.mipmap.albamicon)   // optional
                        .error(R.mipmap.albamicon)
                        //.resize(200,200)             // optional
                        .into(holder.imageViewUploadedSpecies);
                holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
            }
        }
        /*if(reviewList != null){
            if(reviewList.get(position).getSpecies() != null && reviewList.get(position).getSpecies().length()>0){
                holder.textViewSpeciesName.setText(reviewList.get(position).getSpecies());
               *//* String imageFolderPath ="";
                String tag = reviewList.get(position).getTag();
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
                }*//*
                Picasso.with(mContext)
                        .load(R.mipmap.albamicon)
                        .placeholder(R.mipmap.albamicon)   // optional
                        .error(R.mipmap.albamicon)
                        //.resize(200,200)             // optional
                        .into(holder.imageViewUploadedSpecies);
            }
        }*/
    }


    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        public ImageView imageViewUploadedSpecies;
        public TextView textViewSpeciesName;
        private UploadedSpeciesAdapter.MyViewHolder.ClickListener listener;
        private final View selectedOverlay;

        public MyViewHolder(View view,UploadedSpeciesAdapter.MyViewHolder.ClickListener listener) {
            super(view);
            imageViewUploadedSpecies = (ImageView) view.findViewById(R.id.imageViewUploadedSpecies);
            textViewSpeciesName = (TextView) view.findViewById(R.id.textViewSpeciesName);
            this.listener = listener;
            selectedOverlay = (View) itemView.findViewById(R.id.selected_overlay_folder);
            //view.setOnClickListener(this);
            view.setOnLongClickListener (this);
        }

        /*@Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(getAdapterPosition ());
            }
        }*/

        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                return listener.onItemLongClicked(getAdapterPosition ());
            }
            return false;
        }

        public interface ClickListener {
            //public void onItemClicked(int position);
            public boolean onItemLongClicked(int position);
        }
    }


}
