package com.ivy.sd.png.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.StrictMode;
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
import com.ivy.sd.png.model.CatalogImageDownloadService;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CatalogImagesDownlaod extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;
    private Toolbar toolbar;
    private Button catalogRefresh;
    private Button full_download_catalog;
    private TextView tvDownloadStatus;
    private ImageDownloadReceiver receiver;
    TransferUtility transferUtility;
    AmazonS3Client s3;
    private int totalDownloadImageCount;
    private int totalDownloadedCount;
    private String lastDownloadTime;
    TextView last_download_time;

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

        lastDownloadTime = bmodel.synchronizationHelper.getLastDownloadedDateTime();
        totalDownloadImageCount = bmodel.synchronizationHelper.getCatalogImagesCount();
        totalDownloadedCount = getTotalDowloadedImages();
        tvDownloadStatus = (TextView) findViewById(R.id.tv_downloadStaus);
        catalogRefresh = (Button) findViewById(R.id.refresh_catalog);
        full_download_catalog = (Button) findViewById(R.id.full_download_catalog);
        tvDownloadStatus.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        catalogRefresh.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        full_download_catalog.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        if (bmodel.synchronizationHelper.getImageDetails().size() == 0) {
            catalogRefresh.setVisibility(View.INVISIBLE);
        }
        catalogRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CatalogImageDownloadService.isServiceRunning) {
                    new CatalogImagesRefresh(false).execute();
                }
            }
        });

        full_download_catalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CatalogImageDownloadService.isServiceRunning) {
                    stopService(new Intent(CatalogImagesDownlaod.this, CatalogImageDownloadService.class));
                }
                new CatalogImagesRefresh(true).execute();
            }
        });

        if (bmodel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD) {
            ((CardView) findViewById(R.id.catalog_card)).setVisibility(View.VISIBLE);

            //catalogRefresh.setVisibility(View.VISIBLE);
            /* Register reciver to receive downlaod status. */
            IntentFilter filter = new IntentFilter(ImageDownloadReceiver.PROCESS_RESPONSE);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            receiver = new ImageDownloadReceiver();
            registerReceiver(receiver, filter);
            isServiceRunning();
            tvDownloadStatus.setText("Downloaded " + totalDownloadedCount + "/" + totalDownloadImageCount);
            /*if (totalDownloadedCount == totalDownloadImageCount) {
                catalogRefresh.setVisibility(View.INVISIBLE);
                full_download_catalog.setVisibility(View.VISIBLE);
            }*/
            if (!isExternalStorageAvailable()) {
                tvDownloadStatus.setText(getResources().getString(R.string.external_storage_not_available));
            }

            last_download_time = (TextView) findViewById(R.id.last_download_time);
            last_download_time.setText(getResources().getString(R.string.last_image_download_time) + " " +
                    lastDownloadTime);
        } else {
            ((CardView) findViewById(R.id.catalog_card)).setVisibility(View.GONE);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    public class ImageDownloadReceiver extends BroadcastReceiver {
        public static final String PROCESS_RESPONSE = "com.ivy.intent.action.CatalogImageDownload";

        @Override
        public void onReceive(Context context, Intent intent) {
            updateDownloadStatus(intent);
        }
    }

    public void updateDownloadStatus(Intent intent) {
        if (intent != null) {
            if (intent.getExtras() != null) {
                if (totalDownloadImageCount == 0) {
                    last_download_time.setText(bmodel.synchronizationHelper.getLastDownloadedDateTime());
                    totalDownloadImageCount = bmodel.synchronizationHelper.getCatalogImagesCount();
                }
                full_download_catalog.setVisibility(View.INVISIBLE);
                catalogRefresh.setVisibility(View.INVISIBLE);
                Bundle b = intent.getExtras();
                if (b.getString("Error") != null) {
                    Toast.makeText(getApplicationContext(), b.getString("Error"), Toast.LENGTH_LONG).show();
                } else if (b.getInt("Status") != 0) {
                    if (b.getInt("Status") == DataMembers.MESSAGE_DOWNLOAD_COMPLETE_CATALOG) {
                        tvDownloadStatus.setText("Downloaded " + b.getInt("responseCount") + "/" + totalDownloadImageCount);
                    }
                } else if (b.getInt("responseCount") != 0) {
                    tvDownloadStatus.setText("Downloading " + b.getInt("responseCount") + "/" + totalDownloadImageCount);
                }

            }

        }

    }

    public class CatalogImagesRefresh extends AsyncTask<String, Void, String> {

        List<S3ObjectSummary> summaries = new ArrayList<>();
        private boolean isFullDownload;

        CatalogImagesRefresh(boolean isFullDownload) {
            this.isFullDownload = isFullDownload;
        }

        protected void onPreExecute() {
            full_download_catalog.setVisibility(View.INVISIBLE);
            catalogRefresh.setVisibility(View.INVISIBLE);
            tvDownloadStatus.setText(getResources().getString(R.string.connecting));
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                if (isFullDownload) {
                    bmodel.synchronizationHelper.clearCatalogImages();
                }
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

                    if (!isFullDownload) {
                        if (listing.getObjectSummaries().size() > 0) {
                            for (S3ObjectSummary fileList : listing.getObjectSummaries()) {
                                if (fileList.getLastModified()
                                        .after(DateUtil.convertStringToDateObject(lastDownloadTime, "MM/dd/yyyy HH:mm:ss"))) {
                                    summaries.add(fileList);
                                }
                            }
                        }
                    } else {
                        summaries.addAll(listing.getObjectSummaries());
                    }
                    int totalImageCount = listing.getObjectSummaries().size();

                    while (listing.isTruncated()) {
                        listing = s3.listNextBatchOfObjects(listing);
                        if (!isFullDownload) {
                            if (listing.getObjectSummaries().size() > 0) {
                                for (S3ObjectSummary fileList : listing.getObjectSummaries()) {
                                    if (fileList.getLastModified()
                                            .after(DateUtil.convertStringToDateObject(lastDownloadTime, "MM/dd/yyyy HH:mm:ss"))) {
                                        summaries.add(fileList);
                                    }
                                }
                            }
                        } else {
                            summaries.addAll(listing.getObjectSummaries());
                        }
                        totalImageCount += listing.getObjectSummaries().size();
                        Commons.print("File size" + listing.getObjectSummaries().size() + ", " + summaries.size());


                    }
                    bmodel.synchronizationHelper.setCatalogImageDownloadFinishTime(totalImageCount + "");
                    if (summaries != null && summaries.size() > 0) {


                        if (!isFullDownload) {
                            deleteImages(summaries);
                        }

                        bmodel.synchronizationHelper.updateImageDetails(summaries);
                        return "Success";
                    }


                }
            } catch (Exception e) {
                Commons.printException(e);
                return "Error";
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            totalDownloadImageCount = bmodel.synchronizationHelper.getCatalogImagesCount();
            if (summaries.size() == 0) {
                Toast.makeText(getApplicationContext(), "All images are upTo date", Toast.LENGTH_LONG).show();
                tvDownloadStatus.setText("Downloaded " + getTotalDowloadedImages() + "/" + totalDownloadImageCount);
                catalogRefresh.setVisibility(View.VISIBLE);
                full_download_catalog.setVisibility(View.VISIBLE);
            } else if (s.equalsIgnoreCase("Success")) {
                tvDownloadStatus.setText(getResources().getString(R.string.downloading));
                if (CatalogImageDownloadService.isServiceRunning) {
                    stopService(new Intent(CatalogImagesDownlaod.this, CatalogImageDownloadService.class));
                }
                Intent intent = new Intent(CatalogImagesDownlaod.this, CatalogImageDownloadService.class);
                startService(intent);

            } else {
                Toast.makeText(getApplicationContext(), "All images are upTo date", Toast.LENGTH_LONG).show();
                tvDownloadStatus.setText("Downloaded " + getTotalDowloadedImages() + "/" + totalDownloadImageCount);
                catalogRefresh.setVisibility(View.VISIBLE);
                full_download_catalog.setVisibility(View.VISIBLE);
            }

        }

    }

    private void isServiceRunning() {
        if (CatalogImageDownloadService.isServiceRunning) {
            catalogRefresh.setVisibility(View.INVISIBLE);
            full_download_catalog.setVisibility(View.INVISIBLE);
        } else {
            catalogRefresh.setVisibility(View.VISIBLE);
            full_download_catalog.setVisibility(View.VISIBLE);
        }
    }

    private int getTotalDowloadedImages() {
        int count = 0;
        if (isExternalStorageAvailable()) {
            try {
                File folderImage = new File(
                        bmodel.synchronizationHelper.getStorageDir(getResources().getString(R.string.app_name))
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
                File folderImage = new File(bmodel.synchronizationHelper.getStorageDir(getResources().getString(R.string.app_name))
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
                File folderImage = new File(
                        bmodel.synchronizationHelper.getStorageDir(getResources().getString(R.string.app_name))
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

}