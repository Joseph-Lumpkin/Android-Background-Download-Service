package com.josephlumpkin.downloadservice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.josephlumpkin.downloadservice.download.DownloadService;

public class MainActivity extends AppCompatActivity {
    private Intent mDownloadService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Start the download service
         mDownloadService = new Intent(this, DownloadService.class);
        startForegroundService(mDownloadService);
    }

    public void onClickDebug(View view) {
        Intent intent = new Intent();
        intent.setAction(DownloadService.ACTION_DOWNLOAD_APK);
        intent.putExtra("url", "https://www.website.com/hulu.apk");
        //intent.setAction(DownloadService.ACTION_DOWNLOAD_MOVIE);
        //intent.putExtra("url", "https://www.website.com/KodiakIsland720p.mp4");
        this.sendBroadcast(intent);
    }
}