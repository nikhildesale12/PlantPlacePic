package com.ibin.plantplacepic.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.CommentResponseBean;
import com.ibin.plantplacepic.database.DatabaseHelper;

import java.util.List;

/**
 * Created by NN on 09/02/2018.
 */

public class MyCommentRecyclerViewAdapter extends RecyclerView.Adapter<MyCommentRecyclerViewAdapter.DataObjectHolder> {
    private static String LOG_TAG = "MyCommentRecyclerViewAdapter";
    private static MyClickListener myClickListener;
    List<CommentResponseBean> informationList ;
    Activity activity ;
    DatabaseHelper databaseHelper;

    public static class DataObjectHolder extends RecyclerView.ViewHolder {//implements View.OnClickListener
        TextView commentBy;
        TextView commentDate;
        TextView textCommentBy;

        public DataObjectHolder(View itemView) {
            super(itemView);
            commentBy = (TextView) itemView.findViewById(R.id.commentBy);
            commentDate= (TextView) itemView.findViewById(R.id.commentDate);
            textCommentBy= (TextView) itemView.findViewById(R.id.textCommentBy);
            Log.i(LOG_TAG, "Adding Listener");
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public MyCommentRecyclerViewAdapter(List<CommentResponseBean> informationList,Activity activity) {
        this.informationList = informationList;
        this.activity = activity;
        databaseHelper = DatabaseHelper.getDatabaseInstance(activity);
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_comment, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        holder.commentBy.setText(informationList.get(position).getUserName());
        if(informationList.get(position).getDate() != null && informationList.get(position).getDate().length()>0){
            holder.commentDate.setText(informationList.get(position).getDate());
        }else{
            holder.commentDate.setVisibility(View.GONE);
        }
        holder.textCommentBy.setText(informationList.get(position).getComment());
    }

    @Override
    public int getItemCount() {
        return informationList.size();
    }
    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }
}
