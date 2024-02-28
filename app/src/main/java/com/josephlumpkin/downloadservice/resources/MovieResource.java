package com.josephlumpkin.downloadservice.resources;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class MovieResource extends Resource {
    /** Debugging Tag */
    private final String TAG = "MovieResource";

    public MovieResource(Context context) {
        super(context);
    }

    /**
     * @inheritDoc
     *
     * Move this movie file to the correct location and
     * add resource entry to shared preferences.
     */
    @Override
    public void add() {
        Log.d(TAG, "Adding movie resource to the system.");
    }

    @Override
    public boolean exists() {
        File file = new File(getSaveDirectory() + mTitle);
        return file.exists();
    }

    @Override
    public boolean isValidFileType() {
        return getResourceName().substring(getResourceName().lastIndexOf('.')).equals(".mp4");
    }

    /**
     * @inheritDoc
     *
     * Remove this movie resource from the /Movies/ directory.
     */
    @Override
    public void remove() {
        File file = new File(getSaveDirectory() + mTitle);
        if (file.exists()) {
            file.delete();
        }
    }

    ///**
    // * @inheritDoc
    // *
    // * Determined by comparing the movie resource version to the current version.
    // */
    //@Override
    //public boolean updateAvailable() {
    //    return false;
    //}

    ///**
    // * @inheritDoc
    // *
    // * Determined by comparing the required movie resource version to the current version.
    // */
    //@Override
    //public boolean updateRequired() {
    //    return false;
    //}

    //@Override
    //public long getCurrentVersion() {
    //    //TODO get the current version of this movie resource from shared preferences
    //    return 0;
    //}

    /**
     * @inheritDoc
     *
     * Get the root long term storage directory of this movie resource.
     *
     * @return The absolute path of this resource object.
     */
    @Override
    public String getSaveDirectory() {
        File moviesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        return moviesDir.getPath() + "/";
    }
}
