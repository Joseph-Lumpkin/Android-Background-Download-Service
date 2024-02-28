package com.josephlumpkin.downloadservice.utility;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * APK Utilities
 * Utility class to provide APK Install Intent and APK File Names
 */
public class ApkUtils {

    /** Logging Tag */
    private static final String TAG = "ApkUtils";

    /**
     * Install APK
     * Install Application from APK
     *
     * @param context       Context
     * @param apkFile       Filename of APK to install
     */
    public static void installAPk(Context context, File apkFile) {
        Log.d(TAG, "InstallAPK: APK Install Class created.");
        Process process = null;
        OutputStream os = null;
        try {
            // Request root access
            process = Runtime.getRuntime().exec("su");
            os = process.getOutputStream();

            // Execute the uninstall command
            os.write(("pm install " + apkFile.getAbsolutePath() + "\n").getBytes());
            os.flush();

            // Close the terminal
            os.write("exit\n".getBytes());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This uninstalls a package from the local device
     * given the package's name.
     * ex: uninstallApp(com.example.app);
     *
     * @param packageName   Name of package to uninstall
     */
    public static void uninstallApk(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse(packageName));
        context.startActivity(intent);
    }

    /**
     * Tell if a given apk package is installed.
     *
     * @param packageName Name of the package to check the installation status.
     *
     * @return (installed == true)
     */
    public static boolean isApkInstalled(Context context, String packageName) {
        try {
            // Get package info
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, 0);
            // Get version code
            if (packageInfo != null && packageInfo.getLongVersionCode() != 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the version of a given apk package
     *
     * @param packageName Name of the package to check the version of.
     *
     * @return (installed == true)
     */
    public static long getApkVersion(Context context, String packageName) {
        try {
            // Get package info
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, 0);
            // Get version code
            return packageInfo.getLongVersionCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get APK Install Intent <p>
     * Get the Intent required to launch an install activity.
     *
     * @param context       Context
     * @param apkFile       Filename of APK to install
     */
    private static Intent getApkInStallIntent(Context context, File apkFile) {
        Log.d(TAG, "InstallAPK: Creating Install Intent.");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // For Android 7 and above use file provider
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            Log.d(TAG, "InstallAPK: Using File provider = " + apkFile);
            // Use secure file provider to create Content uri for file
            Uri uri = FileProvider.getUriForFile(
                    context, "com.josephlumpkin.downloadservice.utility.provider", apkFile);
            // Create the intent needed to launch install activity
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }
        return intent;
    }

    /**
     * Get URI of APK <p>
     * Get the URI of the APK files.
     *
     * @param apkFile       Filename of APK to install
     */
    private static Uri getApkUri(File apkFile) {
        // Log the APK file requested
        Log.d(TAG, "getApkUri getting simple filename " + apkFile.toString());

        // If SDCard write permission is not set, or if there is no SDCard,
        // the apk file is saved in memory, you need to grant permission to install
        try {
            String[] command = {"chmod", "777", apkFile.toString()};
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
        } catch (IOException ignored) {}

        // Generate the URI
        Uri uri = Uri.fromFile(apkFile);

        // Log the URI created
        Log.d(TAG, uri.toString());

        // Return the URI requested
        return uri;
    }
}
