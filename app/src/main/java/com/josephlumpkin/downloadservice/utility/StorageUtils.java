package com.josephlumpkin.downloadservice.utility;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * Storage Utilities
 * This provides utilities to Application Storage.
 */
public final class StorageUtils {
    /**
     * Get Application cache directory
     *
     * @param context   Context
     * @return          File to use for temporary Cache
     */
    public static File getCacheDirectory(Context context) {
        File appCacheDir = context.getCacheDir();
        if (appCacheDir == null) {
            Log.w("StorageUtils",
                    "Can't define system cache directory" +
                            "The app should be re-installed.");
        }
        return appCacheDir;
    }

}
