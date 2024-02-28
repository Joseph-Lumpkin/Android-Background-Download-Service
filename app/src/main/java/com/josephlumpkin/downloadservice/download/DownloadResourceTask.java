package com.josephlumpkin.downloadservice.download;

import android.util.Log;

import com.josephlumpkin.downloadservice.resources.Resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

/**
 * A runnable task to download resources.
 */
public class DownloadResourceTask implements Runnable {
    /** Debug Logging Tag */
    private final String TAG = "DownloadResourceTask";
    /** Buffer Size for download */
    private static final int dn_BUFFER_SIZE = 200 * 1024;
    /** Resource object to download */
    private Resource mResource;

    /**
     * Download a resource from the provided,
     * populated resource object's data.
     *
     * @param resource Resource to download
     */
    public DownloadResourceTask(Resource resource) {
        Log.d(
            TAG,
            String.format(Locale.US,
                "DownloadResourceTask: Download task created for %s.",
                resource.getResourceName())
        );
        mResource = resource;
    }

    /**
     * Attempt to download this task's resource.
     */
    @Override
    public void run() {
        try {
            //******************************
            // Setup Download Streams
            //******************************
            BufferedInputStream in = new BufferedInputStream(new URL(mResource.getUrl()).openStream());
            File outputFile = new File(mResource.getSaveDirectory(), mResource.getResourceName());
            if (!outputFile.exists())
                outputFile.createNewFile();
            FileOutputStream out = new FileOutputStream(outputFile);
            Log.d(TAG, String.format(Locale.US, "Download beginning, output file: '%s'", outputFile.getAbsolutePath()));

            //******************************
            // Setup Download Buffer
            //******************************
            byte[] dataBuffer = new byte[dn_BUFFER_SIZE];
            int bytesRead;
            long byteSum = 0;
            int oldProgress = 0;

            // Wait while the File downloads
            while ((bytesRead = in.read(dataBuffer)) != -1) {
                // Append the data to the file as it arrives
                byteSum += bytesRead;
                out.write(dataBuffer, 0, bytesRead);
                // Calculate the progress
                int progress = (int) (byteSum * 100L / (mResource.getSize() + mResource.getAdditionalPayloadSize()));
                // Only report progress changes
                if (progress != oldProgress) {
                    Log.d(
                        TAG,
                        String.format(
                            Locale.US,
                            "DownloadResourceTask progress for %s: %d",
                            mResource.getResourceName(),
                            progress
                        )
                    );
                }
                oldProgress = progress;
            }

            // Close the download streams when finished
            in.close();
            out.close();
            // Make the resource available for use
            mResource.add();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
