package com.josephlumpkin.downloadservice.resources;

import android.content.Context;
import android.util.Log;

import com.josephlumpkin.downloadservice.utility.ApkUtils;
import com.josephlumpkin.downloadservice.utility.StorageUtils;

import java.io.File;
import java.util.Locale;

public class ApkResource extends Resource {

    /** Debugging Tag */
    private final String TAG = "ApkResource";

    public ApkResource(Context context) {
        super(context);
    }

    /**
     * @inheritDoc
     *
     * Launch the install intent for this APK resource.
     */
    @Override
    public void add() {
        Log.d(TAG, "Adding apk resource to the system.");
        // Install this apk resource
        File fileToInstall = new File(getSaveDirectory(), getResourceName());
        if (fileToInstall.exists()) {
            Log.d(TAG,
                String.format(
                    Locale.US,
                    "Starting install for %s.",
                    getResourceName()
                )
            );
            ApkUtils.installAPk(mContext, fileToInstall);
        } else {
            Log.d(TAG,
                    String.format(Locale.US, "Error, file '%s' not found.", fileToInstall.getAbsolutePath()));
        }
    }

    /**
     * @inheritDoc
     *
     * Check if this apk resource is installed on the system.
     */
    @Override
    public boolean exists() {
        return ApkUtils.isApkInstalled(mContext, mResourcePackage);
    }

    @Override
    public boolean isValidFileType() {
        return getResourceName().substring(getResourceName().lastIndexOf('.')).equals(".apk");
    }

    /**
     * @inheritDoc
     *
     * Uninstall this APK resource from the device.
     */
    @Override
    public void remove() {
        if (exists())
            ApkUtils.uninstallApk(mContext, mResourcePackage);
    }

    /**
     * @inheritDoc
     *
     * @return a temporary save path's root directory.
     *  This is used as an intermediate location when downloading and installing android packages.
     */
    @Override
    public String getSaveDirectory() {
        return StorageUtils.getCacheDirectory(mContext).getPath();
    }

    ///**
    // * @inheritDoc
    // *
    // * Compare this APK resource version to the current version installed.
    // */
    //@Override
    //public boolean updateAvailable() {
    //    return false;
    //}

    ///**
    // * @inheritDoc
    // *
    // * Compare this APK resource requiredVersion to the current version installed.
    // */
    //@Override
    //public boolean updateRequired() {
    //    return false;
    //}

    ///**
    // * @inheritDoc
    // *
    // * Parse the Android system for this resource's version number.
    // */
    //@Override
    //public long getCurrentVersion() {
    //    return ApkUtils.getApkVersion(mContext, mResourcePackage);
    //}
}
