package com.ivy.sd.png.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.DownloaderThreadCatalog;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CatalogImagesDownlaod extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;
    private Toolbar toolbar;
    private static Button catalogRefresh;
    private static TextView tvDownloadStatus;
    TransferUtility transferUtility;
    AmazonS3Client s3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_images_downlaod);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        initializeItems();
    }

    private void initializeItems() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // Set title to actionbar
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.catalog_images_download));
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Used to hide the app logo icon from actionbar
            getSupportActionBar().setDisplayUseLogoEnabled(false);

        }

        tvDownloadStatus = (TextView) findViewById(R.id.tv_downloadStaus);
        catalogRefresh = (Button) findViewById(R.id.refresh_catalog);
        tvDownloadStatus.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        catalogRefresh.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        catalogRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CatalogImagesRefresh().execute();
            }
        });

        if (bmodel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD) {
            ((CardView) findViewById(R.id.catalog_card)).setVisibility(View.VISIBLE);

            catalogRefresh.setVisibility(View.VISIBLE);
            tvDownloadStatus.setText("Downloaded " + getTotalDowloadedImages() + "/" + bmodel.synchronizationHelper.getCatalogImagesCount());
            if (!isExternalStorageAvailable()) {
                tvDownloadStatus.setText(getResources().getString(R.string.external_storage_not_available));
            }

        } else {
            ((CardView) findViewById(R.id.catalog_card)).setVisibility(View.GONE);
        }

    }

    public class CatalogImagesRefresh extends AsyncTask<String, Void, String> {

        ArrayList<S3ObjectSummary> filesList = new ArrayList<>();

        protected void onPreExecute() {
            catalogRefresh.setVisibility(View.INVISIBLE);
            tvDownloadStatus.setText(getResources().getString(R.string.connecting));
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                if (android.os.Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    bmodel.getimageDownloadURL();

                    bmodel.configurationMasterHelper.setAmazonS3Credentials();
                    initializeTransferUtility();

                    BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                            ConfigurationMasterHelper.SECRET_KEY);
                    AmazonS3Client s3 = new AmazonS3Client(myCredentials);

                    ObjectListing listing = s3.listObjects(DataMembers.S3_BUCKET, DataMembers.img_Down_URL + "Product/ProductCatalog/");
                    List<S3ObjectSummary> summaries = listing.getObjectSummaries();

                    while (listing.isTruncated()) {
                        listing = s3.listNextBatchOfObjects(listing);
                        summaries.addAll(listing.getObjectSummaries());
                    }

                    List<S3ObjectSummary> oldsummaries = bmodel.synchronizationHelper.getImageDetails();

                    if (oldsummaries != null && oldsummaries.size() > 0) {

                        List<S3ObjectSummary> tempSummaries = new ArrayList<>();//mofiled images file list
                        HashMap<String, String> lastModifiedMap = new HashMap<>();
                        HashMap<String, String> modifiedMap = new HashMap<>();

                        for (int i = 0; i < oldsummaries.size(); i++) {
                            lastModifiedMap.put(oldsummaries.get(i).getKey(),
                                    oldsummaries.get(i).getETag());
                        }

                        for (int i = 0; i < summaries.size(); i++) {
                            modifiedMap.put(summaries.get(i).getKey(),
                                    summaries.get(i).getLastModified() + "");
                        }

                        //adding mofied and new added files
                        for (int j = 0; j < summaries.size(); j++) {
                            if (lastModifiedMap.get(summaries.get(j).getKey()) != null) {
                                if (!lastModifiedMap.get(summaries.get(j).getKey()).equals(modifiedMap.get(summaries.get(j).getKey())))
                                    tempSummaries.add(summaries.get(j));
                            } else {
                                tempSummaries.add(summaries.get(j));
                            }
                        }

                        //deleting deleted images in Server
                        List<S3ObjectSummary> deletedFiles = new ArrayList<>();
                        if (lastModifiedMap.size() != modifiedMap.size()) {
                            for (int j = 0; j < oldsummaries.size(); j++) {
                                if (modifiedMap.get(oldsummaries.get(j).getKey()) == null) {
                                    deletedFiles.add(oldsummaries.get(j));
                                }
                            }
                        }

                        if (deletedFiles.size() > 0) {
                            bmodel.synchronizationHelper.deleteImageDetails(deletedFiles);
                            deleteImages(deletedFiles);
                        }

                        if (tempSummaries.size() > 0 || oldsummaries.size() != getFilesCount()) {
                            bmodel.synchronizationHelper.updateImageDetails(tempSummaries);
                            List<S3ObjectSummary> updatedList = bmodel.synchronizationHelper.getImageDetails();

                            filesList = new ArrayList<>();
                            for (int i = 0; i < updatedList.size(); i++) {
                                S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
                                s3ObjectSummary.setBucketName(DataMembers.CATALOG);
                                s3ObjectSummary.setKey(updatedList.get(i).getKey());
                                s3ObjectSummary.setETag("R");

                                //eTag flag made to for modified images to re download
                                for (S3ObjectSummary s3Object : tempSummaries) {
                                    if (s3Object.getKey().equals(updatedList.get(i).getKey()))
                                        s3ObjectSummary.setETag("S");
                                }

                                filesList.add(s3ObjectSummary);
                            }

                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Commons.printException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (filesList.size() > 0) {
                tvDownloadStatus.setText(getResources().getString(R.string.downloading));
                Thread downloaderThread = new DownloaderThreadCatalog(CatalogImagesDownlaod.this,
                        activityHandlerCatalog, filesList,
                        bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid(), transferUtility);
                downloaderThread.start();
            } else {
                Toast.makeText(CatalogImagesDownlaod.this, "All images are upto date", Toast.LENGTH_SHORT).show();
                tvDownloadStatus.setText("Downloaded " + getTotalDowloadedImages() + "/" + bmodel.synchronizationHelper.getCatalogImagesCount());
                catalogRefresh.setVisibility(View.VISIBLE);
            }
        }
    }

    private int getTotalDowloadedImages() {
        int count = 0;
        if (isExternalStorageAvailable()) {
            try {
                File folderImage = new File(CatalogImagesDownlaod.this
                        .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        + "/"
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getUserid()
                        + DataMembers.DIGITAL_CONTENT
                        + "/"
                        + DataMembers.CATALOG);

                if (folderImage.exists())
                    count = folderImage.listFiles().length;
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        return count;
    }

    private boolean isExternalStorageAvailable() {

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

    private void deleteImages(List<S3ObjectSummary> deleteList) {
        if (isExternalStorageAvailable()) {
            try {
                File folderImage = new File(CatalogImagesDownlaod.this
                        .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        + "/"
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getUserid()
                        + DataMembers.DIGITAL_CONTENT
                        + "/"
                        + DataMembers.CATALOG);

                if (folderImage.exists()) {
                    for (S3ObjectSummary s3ObjectSummary : deleteList) {

                        String fileNames[] = s3ObjectSummary.getKey().split("/");
                        String fileName = fileNames[fileNames.length - 1];

                        File outFile = new File(folderImage + "/"
                                + fileName);
                        if (outFile.exists())
                            outFile.delete();
                    }
                }
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
    }

    private int getFilesCount() {
        int count = 0;
        if (isExternalStorageAvailable()) {
            try {
                File folderImage = new File(CatalogImagesDownlaod.this
                        .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        + "/"
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getUserid()
                        + DataMembers.DIGITAL_CONTENT
                        + "/"
                        + DataMembers.CATALOG);

                if (folderImage.exists()) {
                    count = folderImage.listFiles().length;
                }
            } catch (Exception e) {
                Commons.printException(e);
                count = 0;
            }
        }
        return count;
    }

    private void initializeTransferUtility() {
        BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                ConfigurationMasterHelper.SECRET_KEY);
        s3 = new AmazonS3Client(myCredentials);
        transferUtility = new TransferUtility(s3, CatalogImagesDownlaod.this);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    public static Handler activityHandlerCatalog = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case DataMembers.MESSAGE_UPDATE_PROGRESS_CATALOG:
                    int currentProgress = msg.arg1;
                    int totalCount = msg.arg2;
                    if (tvDownloadStatus != null)
                        tvDownloadStatus.setText("Downloading " + currentProgress + "/" + totalCount);

                    if (catalogRefresh != null)
                        catalogRefresh.setVisibility(View.INVISIBLE);


                    break;


                case DataMembers.SDCARD_NOT_AVAILABLE:
                    String errorMessage = "";
                    if (msg.obj != null && msg.obj instanceof String) {
                        errorMessage = (String) msg.obj;
                        Commons.print("SD Card not Available" + errorMessage);
                    }


                    if (catalogRefresh != null && tvDownloadStatus != null) {
                        catalogRefresh.setVisibility(View.VISIBLE);
                        tvDownloadStatus.setText(errorMessage);
                    }

                    break;

                case DataMembers.MESSAGE_DOWNLOAD_COMPLETE_CATALOG:
                    int downloaded = msg.arg1;
                    int total = msg.arg2;

                    Commons.print("Catalog Image Downloading Completed");

                    if (catalogRefresh != null && tvDownloadStatus != null) {
                        catalogRefresh.setVisibility(View.VISIBLE);
                        tvDownloadStatus.setText("Downloaded " + downloaded + "/" + total);
                    }

                    break;

                case DataMembers.MESSAGE_ENCOUNTERED_ERROR_CATALOG:
                    String errorMessage1 = "";
                    int arg1 = msg.arg1;
                    int arg2 = msg.arg2;

                    if (msg.obj != null && msg.obj instanceof String) {
                        errorMessage1 = (String) msg.obj;
                        Commons.print("Catalog Image Downloading Error:" + errorMessage1);
                    }

                    if (catalogRefresh != null && tvDownloadStatus != null) {
                        catalogRefresh.setVisibility(View.VISIBLE);
                        tvDownloadStatus.setText(errorMessage1 + "  Downloaded " + arg1 + "/" + arg2);
                    }

                    break;

                default:
                    // nothing to do here
                    break;
            }
        }
    };
}
