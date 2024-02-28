package com.josephlumpkin.downloadservice.download;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.josephlumpkin.downloadservice.R;
import com.josephlumpkin.downloadservice.resources.ApkResource;
import com.josephlumpkin.downloadservice.resources.MovieResource;
import com.josephlumpkin.downloadservice.resources.Resource;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A download service to listen for and handle resource download requests.
 */
public class DownloadService extends Service {
    /** Debug Logging Tag */
    private final String TAG = "DownloadService";
    /** Download service handler */
    private final Handler mDownloadHandler = new Handler();
    /** Amount of time to wait before checking the download queue for entries again in ms */
    private final int dnDOWNLOAD_DEQUEUE_DELAY = 10000;
    /** Download Resource Queue */
    private Queue<DownloadResourceTask> mDownloadQueue = new LinkedList<>();
    /** Concurrent executor service for multiple background download tasks */
    private ExecutorService mResourceDownloadExecutor;
    /** Number of allocated download threads */
    private final int dnALLOCATED_CONCURRENT_DOWNLOAD_THREADS = 2;
    /** Android Download Service Notification Channel ID */
    private static final String dsCHANNEL_ID = "com.josephlumpkin.downloadservice.download";

    //*******************************
    // Download Service Actions
    //*******************************
    /** Action to queue an apk download */
    public static final String ACTION_DOWNLOAD_APK = "com.josephlumpkin.downloadservice.DOWNLOAD_APK";
    /** Action to remove an apk */
    public static final String ACTION_REMOVE_APK = "com.josephlumpkin.downloadservice.REMOVE_APK";
    /** Action to queue a movie download */
    public static final String ACTION_DOWNLOAD_MOVIE = "com.josephlumpkin.downloadservice.DOWNLOAD_MOVIE";
    /** Action to queue a movie download */
    public static final String ACTION_REMOVE_MOVIE = "com.josephlumpkin.downloadservice.REMOVE_MOVIE";

    /** Broadcast receiver to listen for resource handle requests and pass them along. */
    private final BroadcastReceiver mInstallerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If there is no internet connection
            if (!isNetworkConnected()) {
                Log.d(TAG, "Network is not connected. Resource not downloaded.");
                return;
            }

            // Receive broadcast data
            String action = "";
            String url = "";
            try {
                action = intent.getAction();
                url = intent.getStringExtra("url");
                Log.d(TAG, String.format(Locale.US, "Received intent: %s", action));
            } catch (Exception e) {
                Log.d(TAG, "Unable to extract data. Resource not downloaded.");
                return;
            }

            // Handle the action received
            if (action.equals(ACTION_DOWNLOAD_APK)) {           // Download APK
                ApkResource resource = new ApkResource(getApplicationContext());
                resource.setUrl(url);
                downloadResourceFromUrl(resource);
            } else if (action.equals(ACTION_REMOVE_APK)) {      // Remove APK
                ApkResource resource = new ApkResource(getApplicationContext());
                resource.setPackage(intent.getStringExtra("packageName"));
                resource.remove();
            } else if (action.equals(ACTION_DOWNLOAD_MOVIE)) {  // Download Movie
                MovieResource resource = new MovieResource(getApplicationContext());
                resource.setUrl(url);
                downloadResourceFromUrl(resource);
            } else if (action.equals(ACTION_REMOVE_MOVIE)) {    // Remove Movie
                MovieResource resource = new MovieResource(getApplicationContext());
                resource.setTitle(intent.getStringExtra("title"));
                resource.remove();
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // Create a broadcast filter
        IntentFilter filter = new IntentFilter();
        // Add broadcast definitions to listen for
        filter.addAction(ACTION_DOWNLOAD_APK);
        filter.addAction(ACTION_REMOVE_APK);
        filter.addAction(ACTION_DOWNLOAD_MOVIE);
        filter.addAction(ACTION_REMOVE_MOVIE);
        // Register the receiver
        registerReceiver(mInstallerReceiver, filter);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mDownloadHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(mInstallerReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /* Create a notification channel to bind our service to.
        The DownloadService is a background service,
        but requires execution as if it were a foreground service to avoid unnecessary throttling,
        or putting the service to sleep by the Android system. */
        createNotificationChannel();
        Notification notification = buildNotification();
        startForeground(1, notification);   // Start the service as a foreground service, hidden
        // Create a new concurrent executor pool at the start of the service
        mResourceDownloadExecutor =
                Executors.newFixedThreadPool(dnALLOCATED_CONCURRENT_DOWNLOAD_THREADS);
        // Begin a looping cycle to clear the download queue
        mDownloadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Report the download service status for debug
                Log.d(TAG,
                        String.format(
                                Locale.US,
                                "Download service is running...",
                                dnDOWNLOAD_DEQUEUE_DELAY / 1000
                        )
                );
                // If the resource downloader is not already queued for a shutdown
                if (mResourceDownloadExecutor != null && !mResourceDownloadExecutor.isShutdown()) {
                    // Move all download tasks to the executor
                    while (mDownloadQueue.size() > 0) {
                        mResourceDownloadExecutor.execute(mDownloadQueue.remove());
                    }
                    // Queue a shutdown to be executed after
                    mResourceDownloadExecutor.shutdown();
                } else if (mResourceDownloadExecutor != null && mResourceDownloadExecutor.isShutdown() && mResourceDownloadExecutor.isTerminated()) {
                    // If the previous executor is finished with it's job, create a new executor
                    mResourceDownloadExecutor = Executors.newFixedThreadPool(dnALLOCATED_CONCURRENT_DOWNLOAD_THREADS);
                }
                // Wait to check the download queue again
                mDownloadHandler.postDelayed(this, dnDOWNLOAD_DEQUEUE_DELAY);
            }
        }, dnDOWNLOAD_DEQUEUE_DELAY);
        return START_STICKY;
    }

    /**
     * Check if the console is connected to the internet.
     *
     * @return true if this console is connected to the internet.
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    /**
     * Build a notification to communicate
     * with foreground services.
     *
     * @return a notification from the DownloadService.
     */
    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, dsCHANNEL_ID);
        builder.setOngoing(true)
                .setContentTitle("Download Service")
                .setContentText("This is a notification from the download service.")
                .setSmallIcon(R.drawable.ic_downloadservice_foreground);
        return builder.build();
    }

    /**
     * Create a notification channel to communicate with foreground services.
     * This is mostly used to register this as a foreground service
     * and avoid being put to sleep.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "josephlumpkin Resource Management";
            String description = "Channel for requesting managed resource downloads.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(dsCHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Download a supplied resource object.
     *
     * @param seed Resource object to populate from the download server.
     *                 This resource object should already have it's URL set,
     *                 other parameters will be overwritten.
     *
     * @return the supplied resource object, populated.
     */
    private <T extends Resource> void downloadResourceFromUrl(T seed) {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<String, Void, JSONObject> downloadManifestTask = new AsyncTask<String, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... args) {
                //*******************************
                // Download manifest from website
                //*******************************
                try {
                    URL url = new URL(
                            seed.getUrl().substring(0, seed.getUrl().lastIndexOf("/"))
                            + "/manifest.json"
                    );
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder str = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            str.append(line).append("\n");
                        }
                        br.close();
                        return new JSONObject(str.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject rootJsonObject) {
                //*********************************
                // Parse manifest for this resource
                //*********************************
                JSONObject categoryJSONObject = null;
                if (rootJsonObject != null) {
                    // Get the resource category
                    try {
                        // Locate the category
                        if (seed instanceof ApkResource) {
                            categoryJSONObject = rootJsonObject.getJSONObject("apps");
                        } else if (seed instanceof MovieResource) {
                            categoryJSONObject = rootJsonObject.getJSONObject("movies");
                        }
                        // Locate this resource in the manifest
                        Iterator<String> keys = categoryJSONObject.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            try {
                                // Get the child JSON object at this key
                                JSONObject downloadJsonObject = categoryJSONObject.getJSONObject(key);
                                String parsedUrl = downloadJsonObject.getString("url");
                                // When we find the correct object in the manifest
                                if (parsedUrl.equals(seed.getUrl())) {
                                    // Populate this resource
                                    seed.setVersion(downloadJsonObject.getInt("versionCode"));
                                    seed.setRequiredVersion(downloadJsonObject.getInt("requiredVersionCode"));
                                    seed.setTitle(downloadJsonObject.getString("title"));
                                    seed.setSize(Long.parseLong(downloadJsonObject.getString("size")));
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "Error searching for update");
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Unable to extract download data. Resource not downloaded.");
                    }
                    // Ensure the file type is appropriate for this resource
                    if (seed.isValidFileType()) {
                        // Add this resource to the download queue
                        mDownloadQueue.add(new DownloadResourceTask(seed));
                    }
                }
            }
        };
        // Begin resource creation from url process
        try {
            downloadManifestTask.execute(seed.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
