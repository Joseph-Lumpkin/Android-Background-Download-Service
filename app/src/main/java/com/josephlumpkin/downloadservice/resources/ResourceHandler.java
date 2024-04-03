package com.josephlumpkin.downloadservice.resources;

public interface ResourceHandler {

    /**
     * Add this resource item and makes it available to the system.
     */
    void add();

    /**
     * Remove this resource from the system.
     */
    void remove();

    /**
     * Get the resource name.
     *
     * @return the name of the resource as a String.
     */
    String getResourceName();

    /**
     * Get the path of the directory this item should be saved in.
     *
     * @return the local save root directory.
     */
    String getSaveDirectory();

    /**
     * Check if this resource exists
     *
     * @return (exists == true)
     */
    boolean exists();

    /**
     * Check if this resource's file type is appropriate for this resource object type.
     *
     * @return (valid file and resource type == true)
     */
    boolean isValidFileType();

    //TODO implement if use cases arise
    ///**
    // * Determine whether an update is available for this resource.
    // *
    // * @return true if there is an update available for this resource.
    // */
    //boolean updateAvailable();

    ///**
    // * Determine whether an update is required for usage of this resource.
    // *
    // * @return true if there is an update required for this resource.
    // */
    //boolean updateRequired();

    ///**
    // * Get the current version of this resource on the system.
    // *
    // * @return this resource's current version.
    // */
    //long getCurrentVersion();
}
