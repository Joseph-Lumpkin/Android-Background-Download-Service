package com.josephlumpkin.downloadservice.resources;

import android.content.Context;

public abstract class Resource implements ResourceHandler {
    // Required Parameters
    protected String mUrl;
    protected String mTitle;
    protected String mResourcePackage;
    protected int mVersion;
    protected int mRequiredVersion;
    protected long mSize;
    protected Context mContext;

    // Optional Parameters
    protected String additionalPayloadUrl;
    protected long additionalPayloadSize;

    public Resource (Context context) {
        mContext = context;
    }

    /**
     * @inheritDoc
     *
     * Return the final segment of the file name for this resource.<br>
     * IE: Movie.mp4
     */
    @Override
    public String getResourceName() {
        if (mUrl == null)
            return null;
        return mUrl.substring(mUrl.lastIndexOf("/") + 1);
    }

    //*************************
    // Getters and Setters
    //*************************

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(int version) {
        mVersion = version;
    }

    public int getRequiredVersion() {
        return mRequiredVersion;
    }

    public void setRequiredVersion(int requiredVersion) {
        mRequiredVersion = requiredVersion;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public String getAdditionalPayloadUrl() {
        return additionalPayloadUrl;
    }

    public void setAdditionalPayloadUrl(String additionalPayloadUrl) {
        additionalPayloadUrl = additionalPayloadUrl;
    }

    public long getAdditionalPayloadSize() {
        return additionalPayloadSize;
    }

    public void setAdditionalPayloadSize(long additionalPayloadSize) {
        additionalPayloadSize = additionalPayloadSize;
    }

    public String getPackage() {
        return mResourcePackage;
    }

    public void setPackage(String resourcePackage) {
        this.mResourcePackage = resourcePackage;
    }
}
