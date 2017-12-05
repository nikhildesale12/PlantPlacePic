package com.ibin.plantplacepic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.barteksc.pdfviewer.PDFView;

public class HelpActivity extends AppCompatActivity {

    PDFView pdfView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        pdfView=(PDFView)findViewById(R.id.pdfview);
        pdfView.fromAsset("PlantPlacePicture.pdf").load();
    }
}
