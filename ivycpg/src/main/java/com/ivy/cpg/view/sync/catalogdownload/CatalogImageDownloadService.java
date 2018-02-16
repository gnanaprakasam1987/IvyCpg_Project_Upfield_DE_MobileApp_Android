package com.ivy.cpg.view.sync.catalogdownload;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by dharmapriya.k on 15/12/17.
 */

public class CatalogImageDownloadService extends IntentService {
    public static boolean isServiceRunning;
    public CatalogImageDownloadProvider catalogImageDownloadProvider;

    public CatalogImageDownloadService() {
        super(CatalogImageDownloadService.class.getName());
        catalogImageDownloadProvider = CatalogImageDownloadProvider.getInstance(this);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {


        if (getApplicationContext() != null) {
            BusinessModel businessModel = (BusinessModel) getApplicationContext();


            if (catalogImageDownloadProvider.getCatalogDownloadStatus().equals(CatalogDownloadConstants.UNZIP)) {
                boolean flag = false;
                try {
                    File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + CatalogDownloadConstants.FILE_NAME);
                    int mb = (int) file.length() / 1048576;

                    if (Util.isExternalStorageAvailable(mb * 2)) {

                        if (file.exists()) {
                            File target = new File(Environment.getExternalStorageDirectory().getPath() +"/"+ CatalogDownloadConstants.FOLDER_PATH);
                            target.deleteOnExit();
                            target.mkdir();
                            //target.createNewFile();
                            flag = unzip(file, target);
                        }
                    } else {
                        catalogImageDownloadProvider.storeCatalogDownloadStatusError(CatalogDownloadConstants.NO_SPACE);
                    }
                } catch (IOException e) {
                    Commons.printException(e);
                }
                if (flag) {
                    catalogImageDownloadProvider.storeCatalogDownloadStatus(0,CatalogDownloadConstants.DONE);
                }

            }


        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Commons.print("Catalog service starting");
        return super.onStartCommand(intent, flags, startId);
    }

    public static boolean unzip(File zipFile, File targetDirectory) throws IOException {
        Log.e("StartTimeZip", String.valueOf(new Date()));
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {

            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                dir.mkdir();
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                }
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            zis.close();
        }
        Log.e("EndTimeZip", String.valueOf(new Date()));
        return true;

    }
}
