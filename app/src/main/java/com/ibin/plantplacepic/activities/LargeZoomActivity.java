package com.ibin.plantplacepic.activities;

import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.adapter.CustomPagerAdapterImage;
import com.ibin.plantplacepic.bean.Information;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.ibin.plantplacepic.utility.Constants;

import java.util.ArrayList;

public class LargeZoomActivity extends AppCompatActivity {
    ViewPager viewPager;
    int selectedPosition;
    ImageView swipeImage ;
    CustomPagerAdapterImage customPagerAdapterImage;
    ArrayList<Information> dataList = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_large_zoom);
        dataList = new ArrayList<>();
        viewPager = (ViewPager)findViewById(R.id.viewPagerLargeImage);
        swipeImage = (ImageView)findViewById(R.id.swipeImage);
        if(getIntent() != null){
            dataList = getIntent().getExtras().getParcelableArrayList("imageDataList");
            selectedPosition = getIntent().getExtras().getInt("selectedPosition");
        }
        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_SWIPE, MODE_PRIVATE);
        boolean isShowHint  = prefs.getBoolean(Constants.KEY_HINT_SWAP,true);
        if(isShowHint){
            if(dataList.size()>1) {
                swipeImage.setVisibility(View.VISIBLE);
                Target listObjetivo = new ViewTarget(R.id.swipeImage, this);
                new ShowcaseView.Builder(this, false)
                        .setTarget(listObjetivo)
                        .setContentTitle("Swipe left or right to view more images")
                        .setContentText("You can view next image on swipe")
                        .setStyle(2)
                        .singleShot(1)
                        .hideOnTouchOutside()
                        .setShowcaseEventListener(new OnShowcaseEventListener() {
                            @Override
                            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                swipeImage.setVisibility(View.GONE);
                                SharedPreferences.Editor editor1 = getSharedPreferences(Constants.MY_PREFS_SWIPE, MODE_PRIVATE).edit();
                                editor1.putBoolean(Constants.KEY_HINT_SWAP, false);
                                editor1.commit();
                            }

                            @Override
                            public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                            }

                            @Override
                            public void onShowcaseViewShow(ShowcaseView showcaseView) {

                            }
                        })
                        .build();
            }

        }else{
            swipeImage.setVisibility(View.GONE);
        }
        customPagerAdapterImage = new CustomPagerAdapterImage(LargeZoomActivity.this, dataList);
        viewPager.setAdapter(customPagerAdapterImage);
        viewPager.setCurrentItem(selectedPosition);

//        final TouchImageView touchImageView = (TouchImageView) findViewById(R.id.largeimage);
        final Button cancelDialog = (Button) findViewById(R.id.button_cancel_largeimage);
//
//        if(getIntent() != null){
//            imageName = getIntent().getStringExtra("imageName");
//            tag = getIntent().getStringExtra("tag");
//        }
//
//        String imageFolderPath ="";
       /* if(tag.equals(Constants.TAG_TREE)){
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
//        imageFolderPath = Constants.IMAGE_DOWNLOAD_PATH;
//        Glide.with(LargeZoomActivity.this)
//                .load(imageFolderPath+imageName)
//                .placeholder(R.drawable.pleasewait)   // optional
//                .error(R.drawable.pleasewait)
//                //.resize(200,200)             // optional
//                .into(touchImageView);
//
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
