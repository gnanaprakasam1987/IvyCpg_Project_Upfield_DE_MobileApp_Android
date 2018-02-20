package com.ivy.cpg.view.sync.catalogdownload;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CatalogImagesDownlaod extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private Button catalogRefreshButton;
    private Button catalogFullDownloadButton;
    private TextView tvDownloadStatus;
    private TextView last_download_time;
    private TransferUtility transferUtility;
    private String lastDownloadTime;
    private CatalogImageDownloadProvider catalogImageDownloadProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_images_downlaod);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        catalogImageDownloadProvider = CatalogImageDownloadProvider.getInstance(bmodel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.catalog_images_download));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(false);
        }

        lastDownloadTime = catalogImageDownloadProvider.getLastDownloadedDateTime();

        int totalDownloadedCount = getFilesCount();

        tvDownloadStatus = (TextView) findViewById(R.id.tv_downloadStaus);
        catalogRefreshButton = (Button) findViewById(R.id.refresh_catalog);
        catalogFullDownloadButton = (Button) findViewById(R.id.full_download_catalog);

        tvDownloadStatus.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        catalogRefreshButton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        catalogFullDownloadButton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));


        catalogRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CatalogImagesRefresh().execute();
            }
        });

        catalogFullDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catalogFullDownloadButton.setVisibility(View.INVISIBLE);
                catalogRefreshButton.setVisibility(View.INVISIBLE);

                // Detete the folder and log file.
                catalogImageDownloadProvider.clearCatalogImages();
                // Initiate full download.
                CatalogImageDownloadProvider.getInstance(bmodel).callCatalogImageDownload(new DownloadListener(bmodel.getApplicationContext()));

            }
        });


        if (!Util.isExternalStorageAvailable(10)) {
            tvDownloadStatus.setText(getResources().getString(R.string.external_storage_not_available));
        }

        last_download_time = (TextView) findViewById(R.id.last_download_time);
        last_download_time.setText(getResources().getString(R.string.last_image_download_time) + " " +
                lastDownloadTime);

        resumeDownload();


    }

    private void resumeDownload() {
        if (catalogImageDownloadProvider.getCatalogDownloadStatus().equals(CatalogDownloadConstants.DOWNLOADING)) {

            catalogFullDownloadButton.setVisibility(View.INVISIBLE);
            catalogRefreshButton.setVisibility(View.INVISIBLE);

            bmodel.configurationMasterHelper.setAmazonS3Credentials();
            TransferUtility transferUtility = Util.getTransferUtility(getApplicationContext());
            List<TransferObserver> observers = transferUtility.getTransfersWithType(TransferType.DOWNLOAD);
            for (TransferObserver observer : observers) {
                if (catalogImageDownloadProvider.getCatalogDownloadStatusId() == observer.getId()) {
                    // Sets listeners to in progress transfers
                    if (TransferState.WAITING.equals(observer.getState())
                            || TransferState.WAITING_FOR_NETWORK.equals(observer.getState())
                            || TransferState.IN_PROGRESS.equals(observer.getState())
                            || TransferState.FAILED.equals(observer.getState())) {
                        observer.cleanTransferListener();
                        observer.setTransferListener(new DownloadListener(getApplicationContext()));
                        TransferObserver resumed = transferUtility.resume(observer.getId());
                    }
                }
            }
        } else if (catalogImageDownloadProvider.getCatalogDownloadStatus().equals(CatalogDownloadConstants.UNZIP)) {

            catalogFullDownloadButton.setVisibility(View.INVISIBLE);
            catalogRefreshButton.setVisibility(View.INVISIBLE);

            tvDownloadStatus.setText("UnZipping");

            if (!CatalogImageDownloadService.isServiceRunning) {
                Intent intent = new Intent(CatalogImagesDownlaod.this, CatalogImageDownloadService.class);
                startService(intent);
            }

        } else if (catalogImageDownloadProvider.getCatalogDownloadStatus().equals(CatalogDownloadConstants.DONE)) {

            tvDownloadStatus.setText("Total Images Downloaded :" + getFilesCount());

            catalogFullDownloadButton.setVisibility(View.VISIBLE);
            catalogRefreshButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    public class CatalogImagesRefresh extends AsyncTask<String, Void, String> {

        private List<S3ObjectSummary> summaries = new ArrayList<>();
        private ProgressDialog progressDialogue;


        protected void onPreExecute() {
            catalogFullDownloadButton.setVisibility(View.INVISIBLE);
            catalogRefreshButton.setVisibility(View.INVISIBLE);
            progressDialogue = ProgressDialog.show(CatalogImagesDownlaod.this,
                    DataMembers.SD, getResources().getString(R.string.refreshing),
                    true, false);
        }

        @Override
        protected String doInBackground(String... params) {
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
                    ObjectListing listing = s3.listObjects(DataMembers.S3_BUCKET, DataMembers.img_Down_URL + "Product/ProductCatalog");

                    if (listing.getObjectSummaries().size() > 0) {
                        for (S3ObjectSummary fileList : listing.getObjectSummaries()) {
                            if (fileList.getLastModified()
                                    .after(DateUtil.convertStringToDateObject(lastDownloadTime, "MM/dd/yyyy HH:mm:ss"))) {
                                summaries.add(fileList);
                            }
                        }
                    }

                    while (listing.isTruncated()) {
                        listing = s3.listNextBatchOfObjects(listing);
                        if (listing.getObjectSummaries().size() > 0) {
                            for (S3ObjectSummary fileList : listing.getObjectSummaries()) {
                                if (fileList.getLastModified()
                                        .after(DateUtil.convertStringToDateObject(lastDownloadTime, "MM/dd/yyyy HH:mm:ss"))) {
                                    summaries.add(fileList);
                                }
                            }
                        }

                    }


                    if (summaries != null && summaries.size() > 0) {


                        deleteImages(summaries);


                        for (S3ObjectSummary s3ObjectSummary : summaries) {
                            String imagurl = s3ObjectSummary.getKey();
                            String folderName = DataMembers.CATALOG;
                            String mFileName = "file.bin";
                            File mTranDevicePath = catalogImageDownloadProvider.getStorageDir(getResources().getString(R.string.app_name));

                            int index = imagurl.lastIndexOf('/');

                            if (index >= 0) {
                                mFileName = imagurl.substring(index + 1);
                            }
                            if (mFileName.equals("")) {
                                mFileName = "file.bin";
                            }

                            File mFolderPath = new File(mTranDevicePath, folderName);

                            if (!mFolderPath.exists())
                                mFolderPath.mkdirs();

                            File mfile = new File(mTranDevicePath + "/" + folderName + "/" + mFileName);

                            bmodel.configurationMasterHelper.setAmazonS3Credentials();

                            transferUtility = Util.getTransferUtility(getApplicationContext());

                            // Initiate the download
                            TransferObserver observer = transferUtility.download(DataMembers.S3_BUCKET, imagurl, mfile);
                            observer.setTransferListener(new RefreshListener());
                        }
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
            progressDialogue.dismiss();
            if (summaries.size() == 0) {
                Toast.makeText(getApplicationContext(), "All images are upTo date", Toast.LENGTH_LONG).show();
            }
            catalogImageDownloadProvider.setCatalogImageDownloadFinishTime(getFilesCount() + "", SDUtil.now(SDUtil.DATE_TIME));
            tvDownloadStatus.setText("Downloaded " + getFilesCount() + "/" + getFilesCount());
            catalogRefreshButton.setVisibility(View.VISIBLE);
            catalogFullDownloadButton.setVisibility(View.VISIBLE);

        }

    }

    /**
     * Return number of files available under IvyCPG/CAT folder.
     * Total number of imaged downloaded will be returned.
     *
     * @return count
     */
    private int getFilesCount() {
        int count = 0;
        if (Util.isExternalStorageAvailable(10)) {
            try {
                File folderImage = new File(
                        catalogImageDownloadProvider.getStorageDir(getResources().getString(R.string.app_name))
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


    private void deleteImages(List<S3ObjectSummary> deleteList) {
        if (Util.isExternalStorageAvailable(10)) {
            try {
                File folderImage = new File(catalogImageDownloadProvider.getStorageDir(getResources().getString(R.string.app_name))
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


    private void initializeTransferUtility() {
        BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                ConfigurationMasterHelper.SECRET_KEY);
        AmazonS3Client s3 = new AmazonS3Client(myCredentials);
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

    public class DownloadListener implements TransferListener {
        Context context;

        public DownloadListener(Context context) {
            this.context = context;
        }

        // Simply updates the list when notified.
        @Override
        public void onError(int id, Exception e) {
            Commons.print("IvyCPG" + "onError: " + id + "," + e);

        }


        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            Commons.print("IvyCPG" + String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
            if (tvDownloadStatus != null)
                tvDownloadStatus.setText("Downlaoding " + Util.getBytesString(bytesCurrent) + "/" + Util.getBytesString(bytesTotal));

        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Commons.print("IvyCPG" + "onStateChanged: " + id + ", " + state);
            if (state.equals(TransferState.COMPLETED)) {

                // store time in SDCard.
                catalogImageDownloadProvider.setCatalogImageDownloadFinishTime("1", SDUtil.now(SDUtil.DATE_TIME));
                // update log file with time.
                catalogImageDownloadProvider.storeCatalogDownloadStatus(id, CatalogDownloadConstants.UNZIP);

                // Update status in UI
                if (tvDownloadStatus != null)
                    tvDownloadStatus.setText(bmodel.getResources().getString(R.string.un_zip));

                // Update time in UI
                if (last_download_time != null)
                    last_download_time.setText(SDUtil.now(SDUtil.DATE_TIME));

                int mb = 10;
                try {
                    File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + CatalogDownloadConstants.FILE_NAME);
                    mb = (int) file.length() / 1048576;
                } catch (Exception e) {
                    Commons.printException(e);
                }

                if (Util.isExternalStorageAvailable(mb * 2)) {
                    // Call UnZip service
                    Intent intent = new Intent(CatalogImagesDownlaod.this, CatalogImageDownloadService.class);
                    startService(intent);
                } else {
                    catalogImageDownloadProvider.storeCatalogDownloadStatusError(CatalogDownloadConstants.NO_SPACE);
                    // Update status in UI
                    if (tvDownloadStatus != null)
                        tvDownloadStatus.setText(bmodel.getResources().getString(R.string.no_space));
                }


            } else if (state.equals(TransferState.CANCELED) || state.equals(TransferState.FAILED)) {
                // Finish this activity so that, when user open it will automatically resume.
                // Or we have to build a snackbar with retry option.
                Toast.makeText(context, getResources().getString(R.string.connection_error_please_try_again), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


    public class RefreshListener implements TransferListener {


        // Simply updates the list when notified.
        @Override
        public void onError(int id, Exception e) {
            Commons.print("IvyCPG ref " + "onError: " + id + "," + e);

        }


        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            Commons.print("IvyCPG ref " + String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));

        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Commons.print("IvyCPG ref " + "onStateChanged: " + id + ", " + state);

        }
    }

}