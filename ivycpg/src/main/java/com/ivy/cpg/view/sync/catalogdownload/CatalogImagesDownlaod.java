package com.ivy.cpg.view.sync.catalogdownload;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.StrictMode;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.SDKGlobalConfiguration;
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
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

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

    private ImageDownloadReceiver receiver;

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

        //   lastDownloadTime = catalogImageDownloadProvider.getLastDownloadedDateTime();
        //   int totalDownloadedCount = getFilesCount();

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
                last_download_time.setText("");
                tvDownloadStatus.setText(getResources().getString(R.string.loading));

                if (catalogImageDownloadProvider.deleteLogFile()) {
                    // Detete the folder.
                    catalogImageDownloadProvider.clearCatalogImages();
                    // Initiate full download.
                    CatalogImageDownloadProvider.getInstance(bmodel).callCatalogImageDownload(new DownloadListener(bmodel.getApplicationContext()));
                }

            }
        });


        if (!Util.isExternalStorageAvailable(10)) {
            tvDownloadStatus.setText(getResources().getString(R.string.external_storage_not_available));
        }

        last_download_time = (TextView) findViewById(R.id.last_download_time);
        last_download_time.setText(getResources().getString(R.string.last_image_download_time) + " " +
                lastDownloadTime);


        if (bmodel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD) {

            /* Register reciver to receive downlaod status. */
            IntentFilter filter = new IntentFilter(ImageDownloadReceiver.PROCESS_RESPONSE);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            receiver = new ImageDownloadReceiver();
            registerReceiver(receiver, filter);
            isServiceRunning();
            // tvDownloadStatus.setText("Downloaded " + totalDownloadedCount);

            if (!isExternalStorageAvailable()) {
                tvDownloadStatus.setText(getResources().getString(R.string.external_storage_not_available));
            }

            last_download_time = (TextView) findViewById(R.id.last_download_time);
            last_download_time.setText(getResources().getString(R.string.last_image_download_time) + " " +
                    lastDownloadTime);
        }

        resumeDownload();


    }


    private void isServiceRunning() {
        if (CatalogImageDownloadService.isServiceRunning) {
            catalogRefreshButton.setVisibility(View.INVISIBLE);
            catalogFullDownloadButton.setVisibility(View.INVISIBLE);
        } else {
            catalogRefreshButton.setVisibility(View.VISIBLE);
            catalogFullDownloadButton.setVisibility(View.VISIBLE);
        }
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


    private void resumeDownload() {
        if (catalogImageDownloadProvider.getCatalogDownloadStatus().equals(CatalogDownloadConstants.DOWNLOADING)) {
            tvDownloadStatus.setText(getResources().getString(R.string.loading));
            catalogFullDownloadButton.setVisibility(View.INVISIBLE);
            catalogRefreshButton.setVisibility(View.INVISIBLE);
            last_download_time.setText("");

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
            new UpdateStatus().execute();
        } else {
            new UpdateStatus().execute();
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

                catalogFullDownloadButton.setVisibility(View.INVISIBLE);
                catalogRefreshButton.setVisibility(View.INVISIBLE);
                Bundle b = intent.getExtras();
                if (b.getString("Error") != null) {
                    Toast.makeText(getApplicationContext(), b.getString("Error"), Toast.LENGTH_LONG).show();
                } else if (b.getInt("errorCode") != 0) {
                    if (b.getString("errorMessage") != null)
                        tvDownloadStatus.setText(b.getString("errorMessage"));
                } else if (b.getInt("Status") != 0) {
                    if (b.getInt("Status") == DataMembers.MESSAGE_UNZIPPED) {
                        new UpdateStatus().execute();
                    }
                }
            }

        }

    }


    public class CatalogImagesRefresh extends AsyncTask<String, Void, String> {

        private List<S3ObjectSummary> summaries = new ArrayList<>();
        private ProgressDialog progressDialogue;
        int filesCount = 0;

        protected void onPreExecute() {
            catalogFullDownloadButton.setVisibility(View.INVISIBLE);
            catalogRefreshButton.setVisibility(View.INVISIBLE);
            last_download_time.setText("");
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
                    ObjectListing listing = s3.listObjects(DataMembers.S3_BUCKET, DataMembers.IMG_DOWN_URL + "Product/ProductCatalog");

                    if (listing.getObjectSummaries().size() > 0) {
                        for (S3ObjectSummary fileList : listing.getObjectSummaries()) {
                            if (fileList.getLastModified()
                                    .after(DateTimeUtils.convertStringToDateObject(lastDownloadTime, "MM/dd/yyyy HH:mm:ss"))) {
                                summaries.add(fileList);
                            }
                        }
                    }

                    while (listing.isTruncated()) {
                        listing = s3.listNextBatchOfObjects(listing);
                        if (listing.getObjectSummaries().size() > 0) {
                            for (S3ObjectSummary fileList : listing.getObjectSummaries()) {
                                if (fileList.getLastModified()
                                        .after(DateTimeUtils.convertStringToDateObject(lastDownloadTime, "MM/dd/yyyy HH:mm:ss"))) {
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
                    filesCount = getFilesCount();
                    catalogImageDownloadProvider.setCatalogImageDownloadFinishTime(filesCount + "", DateTimeUtils.now(DateTimeUtils.DATE_TIME));
                    lastDownloadTime = catalogImageDownloadProvider.getLastDownloadedDateTime();

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
            tvDownloadStatus.setText("Downloaded " + filesCount + "/" + filesCount);
            last_download_time.setText(getResources().getString(R.string.last_image_download_time) + lastDownloadTime);
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
        System.setProperty
                (SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");
        BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                ConfigurationMasterHelper.SECRET_KEY);
        AmazonS3Client s3 = new AmazonS3Client(myCredentials);
        s3.setEndpoint(DataMembers.S3_BUCKET_REGION);
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

                // update log file with time.
                catalogImageDownloadProvider.storeCatalogDownloadStatus(id, CatalogDownloadConstants.UNZIP);

                // Update status in UI
                if (tvDownloadStatus != null)
                    tvDownloadStatus.setText(bmodel.getResources().getString(R.string.un_zip));

                // Update time in UI
                if (last_download_time != null)
                    last_download_time.setText(getResources().getString(R.string.last_image_download_time) + " " +
                            DateTimeUtils.now(DateTimeUtils.DATE_TIME));

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

    public class UpdateStatus extends AsyncTask<String, Void, String> {

        int filesCount = 0;

        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            try {

                filesCount = getFilesCount();
                lastDownloadTime = catalogImageDownloadProvider.getLastDownloadedDateTime();

            } catch (Exception e) {
                Commons.printException(e);
                return "Error";
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            tvDownloadStatus.setText("Downloaded " + filesCount + "/" + filesCount);
            last_download_time.setText(getResources().getString(R.string.last_image_download_time) + lastDownloadTime);
            catalogRefreshButton.setVisibility(View.VISIBLE);
            catalogFullDownloadButton.setVisibility(View.VISIBLE);
        }

    }

}