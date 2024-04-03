package com.josephlumpkin.downloadservice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
        Toast.makeText(
                this,
                getResources().getString(R.string.toast_download),
                Toast.LENGTH_LONG
        ).show();
        // Here is an example call to the service below via a broadcast
        //Intent intent = new Intent();
        //intent.setAction(DownloadService.ACTION_DOWNLOAD_APK);
        //intent.putExtra("url", "https://www.samepl-website.com/apkfile.apk");
        //this.sendBroadcast(intent);
    }
}