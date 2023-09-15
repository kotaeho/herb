package com.grandra.medicinal_herib;


import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


public class Description extends AppCompatActivity {
    private ImageView imageView;
    private String herb_num;
    private String des_imageUrl;
    private Context context;
    private AdView mAdview; //애드뷰 변수 선언
    private TextView title;

    private TextView mclltNo;
    private TextView plantClsscNm;
    private TextView mclltSpecsNm;
    private TextView plantFamlNm;
    private TextView plantSpecsScnm;
    private TextView plantEclgDscrt;
    private TextView mclltDistrDscrt;
    private TextView mclltSfrmdNm;
    private TextView usMthodDscrt;
    private TextView mclltEfectDscrt;
    private TextView mclltDoseMthodDscrt;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.description);

        this.Init();
        this.image();

        MobileAds.initialize(this, new OnInitializationCompleteListener() { //광고 초기화
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdview = findViewById(R.id.des_adView); //배너광고 레이아웃 가져오기
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdview.loadAd(adRequest);
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER); //광고 사이즈는 배너 사이즈로 설정
        adView.setAdUnitId("\n" + "ca-app-pub-4268507364131475/1822861988");

        this.Excelread("medicinal_herbs.xls");
    }

    private void Init() {
        title = findViewById(R.id.herb_title);
        imageView = findViewById(R.id.des_imageView);
        herb_num = getIntent().getStringExtra("herb_num");
        des_imageUrl = getIntent().getStringExtra("herb_image");

        mclltNo = findViewById(R.id.mclltNo);
        plantClsscNm = findViewById(R.id.plantClsscNm);
        mclltSpecsNm = findViewById(R.id.mclltSpecsNm);
        plantFamlNm = findViewById(R.id.plantFamlNm);
        plantSpecsScnm = findViewById(R.id.plantSpecsScnm);
        plantEclgDscrt = findViewById(R.id.plantEclgDscrt);
        mclltDistrDscrt = findViewById(R.id.mclltDistrDscrt);
        mclltSfrmdNm = findViewById(R.id.mclltSfrmdNm);
        usMthodDscrt = findViewById(R.id.usMthodDscrt);
        mclltEfectDscrt = findViewById(R.id.mclltEfectDscrt);
        mclltDoseMthodDscrt = findViewById(R.id.mclltDoseMthodDscrt);
    }

    private void image(){
        RequestOptions requestOptions = new RequestOptions()
                .transform(new RoundedCorners(20)) // 둥글게 처리를 위한 RoundedCorners 변환 적용
                .diskCacheStrategy(DiskCacheStrategy.ALL); // 디스크 캐싱 전략 설정

        Glide.with(this)
                .load(des_imageUrl)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade()) // 이미지 로딩 시 CrossFade 효과 적용
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 디스크 캐싱 전략 설정
                .into(imageView);
    }

    private void Excelread(String excelname){
        // Excel 파일을 읽어올 InputStream 얻기
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open(excelname);

            // 엑셀 파일 읽기
            Workbook workbook = Workbook.getWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(0);

            int numRows = sheet.getRows();
            int numCols = sheet.getColumns();

            for (int i = 1; i < numRows; i++) {
                Cell cell = sheet.getCell(3, i);
                String cellValue = cell.getContents();
                if(Objects.equals(herb_num, cellValue)){
                    title.setText(sheet.getCell(5,i).getContents());
                    mclltNo.setText(cellValue);
                    plantClsscNm.setText(sheet.getCell(6,i).getContents());
                    mclltSpecsNm.setText(sheet.getCell(5,i).getContents());
                    plantFamlNm.setText(sheet.getCell(8,i).getContents());
                    plantSpecsScnm.setText(sheet.getCell(9,i).getContents());
                    plantEclgDscrt.setText(sheet.getCell(7,i).getContents());
                    mclltDistrDscrt.setText(sheet.getCell(0,i).getContents());
                    mclltSfrmdNm.setText(sheet.getCell(4,i).getContents());
                    usMthodDscrt.setText(sheet.getCell(10,i).getContents());
                    mclltEfectDscrt.setText(sheet.getCell(2,i).getContents());
                    mclltDoseMthodDscrt.setText(sheet.getCell(1,i).getContents());


                }
            }

            workbook.close();
        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
    }
}