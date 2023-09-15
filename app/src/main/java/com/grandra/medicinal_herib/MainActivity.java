package com.grandra.medicinal_herib;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.review.model.ReviewErrorCode;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.RuntimeExecutionException;
import com.google.android.play.core.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> imageUrl = new ArrayList<>();
    private ArrayList<String> herb_name = new ArrayList<>();
    private ArrayList<String> herb_num = new ArrayList<>();

    private ImageButton menu;
    private FrameLayout TOS;

    private Animation slidingLeft;
    private Animation slidingRight;
    private boolean isOpen = false;
    private TextView TosText;
    private TextView person_info;   //개인정보처리방침
    private Button favorite_btn;

    private RecyclerView recyclerView;
    private EditText editText;

    private AdView mAdview; //애드뷰 변수 선언
    private AppUpdateManager appUpdateManager;

    private final int REQUEST_CODE = 366;
    private final int MY_REQUEST_CODE = 700;

    private NativeAd mNativeAd;
    private boolean isAdLoaded = false;
    private Context mContext;
    private static final String PREF_LAST_UPDATE_VERSION_CODE = "last_update_version_code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());

        // mContext 초기화
        mContext = this;  // 이 부분을 추가하세요


        loadNativeAd();

        this.Init();
        this.Textsearch();
        this.Excelread("medicinal_herbs.xls");
        this.menu();
        this.pop_up();
        this.favorite();

        // 앱의 현재 버전 코드와 마지막 업데이트 확인 버전 코드 비교
        int currentVersionCode = BuildConfig.VERSION_CODE;
        int lastUpdateVersionCode = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getInt(PREF_LAST_UPDATE_VERSION_CODE, 0);

        if (currentVersionCode > lastUpdateVersionCode) {
            // 업데이트 확인 시작
            check_update();

            // 마지막 업데이트 확인 버전 코드 업데이트
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putInt(PREF_LAST_UPDATE_VERSION_CODE, currentVersionCode)
                    .apply();
        }


        MobileAds.initialize(this, new OnInitializationCompleteListener() { //광고 초기화
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdview = findViewById(R.id.adView); //배너광고 레이아웃 가져오기
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdview.loadAd(adRequest);
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER); //광고 사이즈는 배너 사이즈로 설정
        adView.setAdUnitId("\n" + "ca-app-pub-4268507364131475/1822861988");
    }

    private void Init(){
        editText = findViewById(R.id.search);
        imageUrl = new ArrayList<>();
        herb_name = new ArrayList<>();
        herb_num = new ArrayList<>();
        menu = findViewById(R.id.leftButton);
        TOS = findViewById(R.id.TOS);
        TOS.setVisibility(View.GONE);
        slidingLeft = AnimationUtils.loadAnimation(this,R.anim.sliding_left);
        slidingRight = AnimationUtils.loadAnimation(this,R.anim.sliding_right);
        SlidingAnimationListener listener = new SlidingAnimationListener();
        slidingLeft.setAnimationListener(listener);
        slidingRight.setAnimationListener(listener);

        TosText = findViewById(R.id.Tos_popup);
        person_info = findViewById(R.id.person_info);
        favorite_btn = findViewById(R.id.favorite_btn);
    }

    private void favorite(){
        favorite_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,Favorite.class);
                startActivity(intent);
            }
        });
    }

    private void menu(){
        menu.setOnClickListener(new View.OnClickListener() { // 열기 버튼을 누르면
            @Override
            public void onClick(View v) {
                if (isOpen){ // 슬라이딩 레이아웃이 열려져 있으면
                    TOS.startAnimation(slidingRight); // 슬라이딩 레이아웃 닫기
                } else { // 슬라이딩 레이아웃이 닫혀져 있으면
                    TOS.setVisibility(View.VISIBLE); // 슬라이딩 레이아웃을 보이게하기
                    TOS.startAnimation(slidingLeft); // 슬라이딩 레이아웃 열기
                }
            }
        });
    }

    private void Textsearch(){
        // EditText에 텍스트 변경 이벤트 리스너 등록
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 텍스트 변경될 때마다 호출되는 부분
                String searchText = charSequence.toString();
                // 여기에 검색 동작을 수행하는 로직을 작성합니다.
                // searchText를 가지고 원하는 동작을 수행합니다.

                ArrayList<String> filteredImageUrl = new ArrayList<>();
                ArrayList<String> filteredherbName = new ArrayList<>();
                ArrayList<String> filteredherbNum = new ArrayList<>();

                for (int j = 0; j < herb_name.size(); j++) {
                    if (herb_name.get(j).toLowerCase().contains(searchText)) {
                        filteredImageUrl.add(imageUrl.get(j));
                        filteredherbName.add(herb_name.get(j));
                        filteredherbNum.add(herb_num.get(j));
                    }
                }

                // 검색 결과를 리사이클뷰에 업데이트
                CustomAdapter customAdapter = new CustomAdapter(MainActivity.this, filteredImageUrl, filteredherbName, filteredherbNum);

                customAdapter.setOnItemClickListener(new CustomAdapter.OnItemClickListener() {


                    @Override
                    public void onItemClicked(int position, String data,String image_data) {
                        Intent intent = new Intent(getApplicationContext(),Description.class);
                        intent.putExtra("herb_num", data);
                        intent.putExtra("herb_image",image_data);
                        startActivity(intent);
                    }

                    @Override
                    public void onItemClicked(int position, String data) {

                    }
                });

                recyclerView.setAdapter(customAdapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
                Cell cell = sheet.getCell(5, i);
                String cellValue = cell.getContents();
                herb_name.add(cellValue);
            }
            for (int i = 1; i < numRows; i++) {
                Cell cell = sheet.getCell(3, i);
                String cellValue = cell.getContents();
                herb_num.add(cellValue);
            }
            for (int i = 1; i < numRows; i++) {
                Cell cell = sheet.getCell(12, i);
                String cellValue = cell.getContents();
                imageUrl.add(cellValue);
            }
            workbook.close();
        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
        recycleview_set();
    }

    private void recycleview_set(){
        //--------------------------------------------------------

        recyclerView = findViewById(R.id.recyclerView);

        //--- LayoutManager는 아래 3가지중 하나를 선택하여 사용 ----
        // 1) LinearLayoutManager()
        // 2) GridLayoutManager()
        // 3) StaggeredGridLayoutManager()
        //---------------------------------------------------------
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((Context) this);
        recyclerView.setLayoutManager(linearLayoutManager);  // LayoutManager 설정

        CustomAdapter customAdapter = new CustomAdapter(this,imageUrl,herb_name,herb_num);
        //===== [Click 이벤트 구현을 위해 추가된 코드] ==============
        customAdapter.setOnItemClickListener(new CustomAdapter.OnItemClickListener() {


            @Override
            public void onItemClicked(int position, String data,String image_data) {
                Intent intent = new Intent(getApplicationContext(),Description.class);
                intent.putExtra("herb_num", data);
                intent.putExtra("herb_image",image_data);
                startActivity(intent);
            }

            @Override
            public void onItemClicked(int position, String data) {

            }
        });

        recyclerView.setAdapter(customAdapter); // 어댑터 설정
    }

    private void pop_up(){
        TosText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder menu = new AlertDialog.Builder(MainActivity.this);
                menu.setIcon(R.mipmap.ic_launcher);
                menu.setTitle("이용약관"); // 제목
                menu.setMessage(R.string.tos); // 문구


                // 확인 버튼
                menu.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // dialog 제거
                        dialog.dismiss();
                    }
                });

                menu.show();
            }
        });

        person_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder menu = new AlertDialog.Builder(MainActivity.this);
                menu.setIcon(R.mipmap.ic_launcher);
                menu.setTitle("개인정보처리방침"); // 제목
                menu.setMessage(R.string.person_info); // 문구


                // 확인 버튼
                menu.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // dialog 제거
                        dialog.dismiss();
                    }
                });
                menu.show();
            }
        });
    }

    public void check_update(){
        // 앱 업데이트 매니저 초기화
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());

        // 업데이트를 체크하는데 사용되는 인텐트를 리턴한다.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> { // appUpdateManager이 추가되는데 성공하면 발생하는 이벤트
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE // UpdateAvailability.UPDATE_AVAILABLE == 2 이면 앱 true
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) { // 허용된 타입의 앱 업데이트이면 실행 (AppUpdateType.IMMEDIATE || AppUpdateType.FLEXIBLE)
                // 업데이트가 가능하고, 상위 버전 코드의 앱이 존재하면 업데이트를 실행한다.
                requestUpdate (appUpdateInfo);
            }
        });
    }


    // 업데이트 요청
    private void requestUpdate (AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    // 'getAppUpdateInfo()' 에 의해 리턴된 인텐트
                    appUpdateInfo,
                    // 'AppUpdateType.FLEXIBLE': 사용자에게 업데이트 여부를 물은 후 업데이트 실행 가능
                    // 'AppUpdateType.IMMEDIATE': 사용자가 수락해야만 하는 업데이트 창을 보여줌
                    AppUpdateType.IMMEDIATE,
                    // 현재 업데이트 요청을 만든 액티비티, 여기선 MainActivity.
                    this,
                    // onActivityResult 에서 사용될 REQUEST_CODE.
                    REQUEST_CODE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            Toast myToast = Toast.makeText(this.getApplicationContext(), "MY_REQUEST_CODE", Toast.LENGTH_SHORT);
            myToast.show();

            // 업데이트가 성공적으로 끝나지 않은 경우
            if (resultCode != RESULT_OK) {
                Log.v("태그", "Update flow failed! Result code: " + resultCode);
                // 업데이트가 취소되거나 실패하면 업데이트를 다시 요청할 수 있다.,
                // 업데이트 타입을 선택한다 (IMMEDIATE || FLEXIBLE).
                Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

                appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            // flexible한 업데이트를 위해서는 AppUpdateType.FLEXIBLE을 사용한다.
                            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        // 업데이트를 다시 요청한다.
                        requestUpdate(appUpdateInfo);
                    }
                });
            }
        }
    }

    // 앱이 포그라운드로 돌아오면 업데이트가 UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS 상태로 중단되지 않았는지 확인해야 합니다.
    // 업데이트가 이 상태로 중단된 경우 아래와 같이 업데이트를 계속하세요.
    @Override
    protected void onResume() {
        super.onResume();

        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            AppUpdateType.IMMEDIATE,
                                            this,
                                            MY_REQUEST_CODE);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        });
    }

    class  SlidingAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) { // 애니메이션이 끝날 때 자동 호출됨
            if(isOpen) { // 슬라이딩 레이아웃의 열린 상태가 끝나면
                TOS.setVisibility(View.INVISIBLE); // 슬라이딩 레이아웃 안보이게 하고
                isOpen = false; // 닫기 상태가 됨
            } else { // 슬라이딩 레이아웃의 닫힌 상태가 끝나면
                isOpen = true; // 열기 상태가됨
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private void loadNativeAd(){

        AdLoader.Builder adBuilder = new AdLoader.Builder(mContext,getResources().getString(R.string.admob_native_ad_id));

        adBuilder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                if(isDestroyed() || isFinishing() || isChangingConfigurations()){
                    nativeAd.destroy();
                    return;
                }

                if(mNativeAd != null){
                    mNativeAd.destroy();
                }

                mNativeAd = nativeAd;
                isAdLoaded = true;
            }
        });

        VideoOptions videoOptions =
                new VideoOptions.Builder().setStartMuted(true).build();

        NativeAdOptions nativeAdOptions =
                new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();

        AdLoader adLoader = adBuilder.withAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onBackPressed() {
        if (isAdLoaded) {
            // 다이얼로그를 열고 광고 표시
            showNativeAdDialog();
        } else {
            // 광고가 로드되지 않은 경우 기본 뒤로가기 동작 수행
            super.onBackPressed();
        }
    }

    private void showNativeAdDialog() {
        // 다이얼로그 레이아웃을 inflate하여 표시합니다.
        Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.ad_native);
        dialog.setCancelable(true);
        dialog.show();

        Button review = dialog.findViewById(R.id.btnReview);
        Button exit = dialog.findViewById(R.id.btnExit);

        // 네이티브 광고를 다이얼로그 내의 NativeAdView에 표시합니다.
        NativeAdView adView = dialog.findViewById(R.id.nativeAdView);
        populateNativeAdView(mNativeAd, adView);

        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInAppReviewPopup();
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        adView.setMediaView(adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);
    }

    private void showInAppReviewPopup() {
        ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                manager.launchReviewFlow(MainActivity.this, reviewInfo).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
            } else {
                // There was some problem, log or handle the error code.
                @ReviewErrorCode int reviewErrorCode = ((RuntimeExecutionException) Objects.requireNonNull(task.getException())).getErrorCode();
            }
        });
    }
}