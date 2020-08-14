package com.martiandeveloper.statussaverwhatsapp.recyclerview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.martiandeveloper.statussaverwhatsapp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import de.mateware.snacky.Snacky;

@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.FileHolder> {

    private static final String DIRECTORY_TO_SAVE_MEDIA_NOW = "/Status Saver/";
    private final ArrayList<File> filesList;
    private final Activity activity;
    private InterstitialAd interstitialAd;

    public MainAdapter(ArrayList<File> filesList, Activity activity) {
        this.filesList = filesList;
        this.activity = activity;
        setAds();
    }

    @NonNull
    @Override
    public MainAdapter.FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new FileHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(final MainAdapter.FileHolder holder, int position) {
        final File currentFile = filesList.get(position);

        holder.recycler_item_videoDownloadBTN.setOnClickListener(this.downloadMediaItem(currentFile));
        holder.recyclerview_item_imageDownloadBTN.setOnClickListener(this.downloadMediaItem(currentFile));

        if (currentFile.getAbsolutePath().endsWith(".mp4")) {
            holder.recycler_item_imageCV.setVisibility(View.GONE);
            holder.recyclerview_item_videoCV.setVisibility(View.VISIBLE);
            Uri video = Uri.parse(currentFile.getAbsolutePath());
            holder.recycler_item_mainVV.setVideoURI(video);
            holder.recycler_item_mainVV.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                holder.recycler_item_mainVV.start();
            });
        } else {
            Bitmap myBitmap = BitmapFactory.decodeFile(currentFile.getAbsolutePath());
            holder.recyclerview_item_mainIV.setImageBitmap(myBitmap);
        }

        holder.recycler_item_mainVV.setOnClickListener(v -> {
            if (holder.recycler_item_mainVV.isPlaying()) {
                holder.recycler_item_mainVV.pause();
            } else {
                holder.recycler_item_mainVV.start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    private View.OnClickListener downloadMediaItem(final File sourceFile) {

        return v -> ((Runnable) () -> {
            try {
                copyFile(sourceFile, new File(Environment.getExternalStorageDirectory().toString() + DIRECTORY_TO_SAVE_MEDIA_NOW + sourceFile.getName()));

                Snacky.builder().setActivity(activity).
                        setText(R.string.save_successful_message).
                        success().
                        show();

                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (interstitialAd != null && interstitialAd.isLoaded()) {
                        interstitialAd.show();
                    }
                }, 1000);

            } catch (Exception e) {
                e.printStackTrace();

                Snacky.builder().setActivity(activity).
                        setText(R.string.save_error_message).
                        error().
                        show();
            }
        }).run();
    }

    @SuppressWarnings("ConstantConditions")
    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        try (FileChannel source = new FileInputStream(sourceFile).getChannel(); FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }
    }

    @SuppressWarnings("unused")
    static class FileHolder extends RecyclerView.ViewHolder {

        final ImageView recyclerview_item_mainIV;
        final VideoView recycler_item_mainVV;
        final CardView recyclerview_item_videoCV;
        final CardView recycler_item_imageCV;
        final Button recycler_item_videoDownloadBTN;
        final Button recyclerview_item_imageDownloadBTN;

        FileHolder(View itemView) {
            super(itemView);
            recyclerview_item_mainIV = itemView.findViewById(R.id.recyclerview_item_mainIV);
            recycler_item_mainVV = itemView.findViewById(R.id.recyclerview_item_mainVV);
            recyclerview_item_videoCV = itemView.findViewById(R.id.recyclerview_item_videoCV);
            recycler_item_imageCV = itemView.findViewById(R.id.recyclerview_item_imageCV);
            recycler_item_videoDownloadBTN = itemView.findViewById(R.id.recyclerview_item_imageDownloadBTN);
            recyclerview_item_imageDownloadBTN = itemView.findViewById(R.id.recyclerview_item_videoDownloadBTN);
        }
    }

    private void setAds() {
        // Interstitial
        interstitialAd = new InterstitialAd(activity);
        interstitialAd.setAdUnitId(activity.getResources().getString(R.string.interstitial));
        AdRequest interstitialAdRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(interstitialAdRequest);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interstitialAd.loadAd(interstitialAdRequest);
            }
        });
    }
}
