package com.martiandeveloper.statussaverwhatsapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.martiandeveloper.statussaverwhatsapp.R;
import com.martiandeveloper.statussaverwhatsapp.databinding.ActivityMainBinding;
import com.martiandeveloper.statussaverwhatsapp.recyclerview.MainAdapter;

import java.io.File;
import java.util.ArrayList;

import de.mateware.snacky.Snacky;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {

    private static final String WHATSAPP_STATUSES_LOCATION = "/WhatsApp/Media/.Statuses";
    private static final int REQUEST_CODE = 101;
    private ActivityMainBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(binding.activityMainMainTB);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setIcon(R.mipmap.ic_launcher);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        check();
        setAds();
    }

    private void check() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);

            } else {
                setRecyclerView();
            }
        } else {
            setRecyclerView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setRecyclerView();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snacky.builder().setActivity(this).
                            setText(R.string.permission_denied).
                            success().
                            show();
                } else {
                    Snacky.builder().setActivity(this).
                            setText(R.string.permission_denied2).
                            success().
                            show();
                }
            }
        }
    }

    private void setRecyclerView() {
        RecyclerView activity_main_mainRV = binding.activityMainMainRV;
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        activity_main_mainRV.setLayoutManager(mLinearLayoutManager);
        MainAdapter mainAdapter = new MainAdapter(this.getListFiles(new File(Environment.getExternalStorageDirectory().toString() + WHATSAPP_STATUSES_LOCATION)), MainActivity.this);
        activity_main_mainRV.setAdapter(mainAdapter);
    }

    private ArrayList<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<>();
        File[] files;
        files = parentDir.listFiles();
        if (files != null) {
            for (File file : files) {

                if (file.getName().endsWith(".jpg") ||
                        file.getName().endsWith(".gif") ||
                        file.getName().endsWith(".mp4")) {
                    if (!inFiles.contains(file))
                        inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    private void setAds() {
        MobileAds.initialize(this);

        // Banner
        AdView adView = new AdView(this);
        adView.setAdUnitId(getResources().getString(R.string.banner));
        binding.activityMainMainFL.addView(adView);
        AdRequest bannerAdRequest = new AdRequest.Builder().build();
        adView.setAdSize(getAdSize());
        adView.loadAd(bannerAdRequest);
    }

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                share();
                return true;
            case R.id.rate_us:
                rateUs();
                return true;
            case R.id.privacy_policy:
                privacyPolicy();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.check_out) + "\n" + getResources().getString(R.string.app_link));
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private void rateUs() {
        Uri uri = Uri.parse(getResources().getString(R.string.app_link));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(webIntent);
    }

    private void privacyPolicy() {
        Uri uri = Uri.parse(getResources().getString(R.string.privacy_policy_link));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(webIntent);
    }
}
