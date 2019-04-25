package com.ivy.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.io.FileInputStream;

public class FileUtils {

    public static String photoFolderPath;

    private FileUtils(){

    }


    /**
     * @return <code>true<code/> if external storage available else <code>false<code/>
     */

    public static boolean isExternalStorageAvailable() {

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks()
                * (double) stat.getBlockSize();
        // One binary gigabyte equals 1,073,741,824 bytes.
        double mbAvailable = sdAvailSize / 1048576;

        String state = Environment.getExternalStorageState();
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        if (mExternalStorageAvailable == true
                && mExternalStorageWriteable == true && mbAvailable > 10) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * To check file availability
     *
     * @param path File path
     * @return Availability
     */
    public static boolean isFileExisting(String path) {
        File f = new File(path);
        return f.exists();
    }

    /**
     * Getting file URI
     *
     * @param path File path
     * @return URI
     */
    public static Uri getUriFromFile(Context mContext, String path) {
        File f = new File(path);
        if (Build.VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", f);

        } else {
            return Uri.fromFile(f);
        }

    }

    /**
     * DecodeFile is convert the large size image to fixed size which mentioned
     * above
     */
    public static Bitmap decodeFile(File f) {
        int IMAGE_MAX_SIZE = 500;
        Bitmap b = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.ceil(Math.log(IMAGE_MAX_SIZE
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return b;
    }

    public static void checkFileExist(String imageName, String retailerID, boolean isLatLongImage) {
        try {
            String fName = (!isLatLongImage) ? "PRO_" : "LATLONG_" + retailerID;
            File sourceDir = new File(photoFolderPath);
            File[] files = sourceDir.listFiles();
            for (File file : files) {
                if (file.getName().startsWith(fName) &&
                        !file.getName().equals(imageName))
                    file.delete();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public static boolean createFilePathAndFolder(Context context) {
        boolean bool=true;
        try {
            photoFolderPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                    + DataMembers.photoFolderName;

            File photoFolder = new File(photoFolderPath);
            if (!photoFolder.exists()) {
                bool = photoFolder.mkdir();
            }
        }catch(Exception e){
            Commons.printException(e);
        }
        return bool;
    }

    public static void deleteFiles(String folderPath, String fnamesStarts) {
        File folder = new File(folderPath);

        File files[] = folder.listFiles();
        if ((files != null) && (files.length >= 1)) {

            for (File tempFile : files) {
                if (tempFile != null) {
                    if (tempFile.getName().startsWith(fnamesStarts))
                        tempFile.delete();
                }
            }
        }
    }

    /*
     * It returns true if the folder contains the n or more than n files
     * which starts name fnameStarts otherwiese returns false;
     */
    public static boolean checkForNFilesInFolder(String folderPath, int n,
                                                 String fNameStarts) {
        if(fNameStarts==null)
            return false;

        if (n < 1)
            return true;

        File folder = new File(folderPath);
        if (!folder.exists()) {
            return false;
        } else {
            String fnames[] = folder.list();
            if ((fnames == null) || (fnames.length < n)) {
                return false;
            } else {
                int count = 0;
                for (String str : fnames) {
                    if ((str != null) && !fNameStarts.equals("") && (str.length() > 0)) {
                        if (str.startsWith(fNameStarts)) {
                            count++;
                        }
                    }

                    if (count == n) {
                        return true;
                    }
                }
            }

        }
        return false;
    }
}
