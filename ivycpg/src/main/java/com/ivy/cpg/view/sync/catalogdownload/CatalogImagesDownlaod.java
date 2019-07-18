package com.ivy.cpg.view.sync.catalogdownload;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.ivy.cpg.view.sync.AWSConnectionHelper;
import com.ivy.cpg.view.sync.AzureConnectionHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.FontUtils;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import java.io.File;
import java.util.ArrayList;

public class CatalogImagesDownlaod extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private Button catalogRefreshButton;
    private Button catalogFullDownloadButton;
    private TextView tvDownloadStatus;
    private TextView last_download_time;
    private String lastDownloadTime;
    private CatalogImageDownloadProvider catalogImageDownloadProvider;

    private ImageDownloadReceiver receiver;

    private ArrayList<String> stringUrlList = new ArrayList<>();
    int filesCount = 0;
    private int downloadIndex = 0;
    private final String downloadPath = "Product/ProductCatalog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_images_downlaod);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        catalogImageDownloadProvider = CatalogImageDownloadProvider.getInstance(bmodel);

        Toolbar toolbar = findViewById(R.id.toolbar);
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

        tvDownloadStatus = findViewById(R.id.tv_downloadStaus);
        catalogRefreshButton = findViewById(R.id.refresh_catalog);
        catalogFullDownloadButton = findViewById(R.id.full_download_catalog);

        tvDownloadStatus.setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.MEDIUM));
        catalogRefreshButton.setTypeface(FontUtils.getFontBalooHai(this,FontUtils.FontType.REGULAR));
        catalogFullDownloadButton.setTypeface(FontUtils.getFontBalooHai(this,FontUtils.FontType.REGULAR));


        catalogRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CatalogImagesFolderRefresh().execute();
            }
        });

        catalogFullDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonState(View.INVISIBLE);
                last_download_time.setText("");
                tvDownloadStatus.setText(getResources().getString(R.string.loading));

                if (catalogImageDownloadProvider.deleteLogFile()) {
                    // Detete the folder.
                    catalogImageDownloadProvider.clearCatalogImages();
                    // Initiate full download.
//                    CatalogImageDownloadProvider.getInstance(bmodel).callCatalogImageDownload(new DownloadListener(bmodel.getApplicationContext()));
                    CatalogImageDownloadProvider.getInstance(bmodel).callCatalogImageDownload();
                }

            }
        });


        if (!FileUtils.isExternalStorageAvailable(10)) {
            tvDownloadStatus.setText(getResources().getString(R.string.external_storage_not_available));
        }

        last_download_time = findViewById(R.id.last_download_time);
        last_download_time.setText(getResources().getString(R.string.last_image_download_time) + " " +
                lastDownloadTime);


        if (bmodel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD) {

            /* Register reciver to receive downlaod status. */
            IntentFilter filter = new IntentFilter(ImageDownloadReceiver.PROCESS_RESPONSE);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            receiver = new ImageDownloadReceiver();
            registerReceiver(receiver, filter);
            checkServiceRunning();
            // tvDownloadStatus.setText("Downloaded " + totalDownloadedCount);

            if (!FileUtils.isExternalStorageAvailable(10)) {
                tvDownloadStatus.setText(getResources().getString(R.string.external_storage_not_available));
            }

            last_download_time = findViewById(R.id.last_download_time);
            last_download_time.setText(getResources().getString(R.string.last_image_download_time) + " " +
                    lastDownloadTime);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(downloadInfoReceiver,
                new IntentFilter("com.ivy.cpg.view.sync.CatalogDownloadStatus"));

        resumeDownload();
    }

    private void checkServiceRunning() {
        if (CatalogImageDownloadService.isServiceRunning) {
            changeButtonState(View.INVISIBLE);
        } else {
            changeButtonState(View.VISIBLE);
        }
    }

    private void resumeDownload() {
        if (catalogImageDownloadProvider.getCatalogDownloadStatus().equals(CatalogDownloadConstants.DOWNLOADING)) {
            tvDownloadStatus.setText(getResources().getString(R.string.loading));
            changeButtonState(View.INVISIBLE);
            last_download_time.setText(getResources().getString(R.string.last_image_download_time));

            if (!catalogImageDownloadProvider.isDownloadInProgress)
                catalogImageDownloadProvider.downloadProcess(this);

        } else if (catalogImageDownloadProvider.getCatalogDownloadStatus().equals(CatalogDownloadConstants.UNZIP)) {

            changeButtonState(View.INVISIBLE);

            tvDownloadStatus.setText("UnZipping");

            if (!CatalogImageDownloadService.isServiceRunning) {
                Intent intent = new Intent(CatalogImagesDownlaod.this, CatalogImageDownloadService.class);
                startService(intent);
            }

        } else {
            updateStatus();
        }
    }

    private void changeButtonState(int visibleState){
        catalogFullDownloadButton.setVisibility(visibleState);
        catalogRefreshButton.setVisibility(visibleState);
    }

    private void updateStatus(){
        try {

            int filesCount = 0;

            filesCount = FileUtils.getFilesCount(catalogImageDownloadProvider.getStorageDir(getResources().getString(R.string.app_name))
                    + "/"
                    + DataMembers.CATALOG);
            lastDownloadTime = catalogImageDownloadProvider.getLastDownloadedDateTime();

            tvDownloadStatus.setText("Downloaded " + filesCount + "/" + filesCount);
            last_download_time.setText(getResources().getString(R.string.last_image_download_time) + lastDownloadTime);
            changeButtonState(View.VISIBLE);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public class CatalogImagesFolderRefresh extends AsyncTask<String, Void, String> {

        private ProgressDialog progressDialogue;

        protected void onPreExecute() {
            changeButtonState(View.INVISIBLE);
            last_download_time.setText("");
            progressDialogue = ProgressDialog.show(CatalogImagesDownlaod.this,
                    DataMembers.SD, getResources().getString(R.string.refreshing),
                    true, false);
        }

        @Override
        protected String doInBackground(String... params) {

//            bmodel.getimageDownloadURL();

            if (bmodel.configurationMasterHelper.IS_AZURE_CLOUD_STORAGE){
                checkLastModifiedFileAzure();
            }else{
                checkLastModifiedFileAmazon();
            }

            filesCount = FileUtils.getFilesCount(catalogImageDownloadProvider.getStorageDir(getResources().getString(R.string.app_name))
                    + "/"
                    + DataMembers.CATALOG);

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialogue.dismiss();

            if (stringUrlList.isEmpty()) {
                Toast.makeText(getApplicationContext(), "All images are upTo date", Toast.LENGTH_LONG).show();
                tvDownloadStatus.setText("Downloaded " + filesCount + "/" + filesCount);
                last_download_time.setText(getResources().getString(R.string.last_image_download_time) + lastDownloadTime);
                changeButtonState(View.VISIBLE);
            }
        }

    }

    private void checkLastModifiedFileAzure(){

        try {
            CloudBlobContainer cloudBlobContainer = AzureConnectionHelper.getInstance().initializeAzureStorageConnection();

            Iterable<ListBlobItem> blobItems = cloudBlobContainer.getDirectoryReference(downloadPath).listBlobs();

            for (ListBlobItem listBlobItem : blobItems){

                CloudBlockBlob cloudBlockBlob = (CloudBlockBlob)listBlobItem;

                if (cloudBlockBlob.getProperties().getLastModified().after(DateTimeUtils.convertStringToDateObject(lastDownloadTime, "MM/dd/yyyy HH:mm:ss"))) {

                    String sasToken = listBlobItem.getContainer().generateSharedAccessSignature(AzureConnectionHelper.getInstance().getAccessPolicy(), null);

                    stringUrlList.add(String.format("%s?%s", listBlobItem.getUri(), sasToken));
                }
            }

            if (!stringUrlList.isEmpty())
                startDownload();

        }catch (Exception e){
            Commons.printException(e);
        }
    }

    private void checkLastModifiedFileAmazon() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            AWSConnectionHelper.getInstance().setAWSDBValues(this);
            AmazonS3Client s3 = AWSConnectionHelper.getInstance().getS3Connection();

            ObjectListing listing = s3.listObjects(DataMembers.S3_BUCKET, DataMembers.IMG_DOWN_URL + downloadPath);

            if (listing.getObjectSummaries().size() > 0) {
                for (S3ObjectSummary fileList : listing.getObjectSummaries()) {
                    if (fileList.getLastModified()
                            .after(DateTimeUtils.convertStringToDateObject(lastDownloadTime, "MM/dd/yyyy HH:mm:ss"))) {

                        String url = AWSConnectionHelper.getInstance().getSignedAwsUrl(fileList.getKey());

                        stringUrlList.add(url);
                    }
                }
            }

            while (listing.isTruncated()) {
                listing = s3.listNextBatchOfObjects(listing);
                if (listing.getObjectSummaries().size() > 0) {
                    for (S3ObjectSummary fileList : listing.getObjectSummaries()) {
                        if (fileList.getLastModified()
                                .after(DateTimeUtils.convertStringToDateObject(lastDownloadTime, "MM/dd/yyyy HH:mm:ss"))) {

                            String url = AWSConnectionHelper.getInstance().getSignedAwsUrl(fileList.getKey());

                            stringUrlList.add(url);
                        }
                    }
                }
            }

            if (!stringUrlList.isEmpty()) {

                deleteIfImagesExist(stringUrlList);

                startDownload();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void startDownload() {
        PRDownloader.download(stringUrlList.get(downloadIndex)
                , getFile().toString()
                , getFileName(stringUrlList.get(downloadIndex))).build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {

                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        downloadIndex = downloadIndex+1;
                        if (downloadIndex < stringUrlList.size()) {
                            startDownload();
                        }else {
                            downloadIndex = 0;
                            stringUrlList.clear();

                            catalogImageDownloadProvider.setCatalogImageDownloadFinishTime(filesCount + "", DateTimeUtils.now(DateTimeUtils.DATE_TIME));

                            updateStatus();
                        }
                    }

                    @Override
                    public void onError(Error error) {
                        downloadIndex = downloadIndex+1;
                        if (downloadIndex < stringUrlList.size()) {
                            startDownload();
                        }else {
                            downloadIndex = 0;
                            stringUrlList.clear();

                            updateStatus();
                        }

                    }
                });

    }

    private File getFile(){
        String folderName = DataMembers.CATALOG;

        File mTranDevicePath = catalogImageDownloadProvider.getStorageDir(getResources().getString(R.string.app_name));
        File mFolderPath = new File(mTranDevicePath, folderName);

        if (!mFolderPath.exists())
            mFolderPath.mkdirs();

        return new File(mTranDevicePath + "/" + folderName + "/" );
    }

    private String getFileName(String imagurl){
        String mFileName = "file.bin";

        int index = imagurl.lastIndexOf('/');

        if (index >= 0) {
            mFileName = imagurl.substring(index + 1);

            String[] file = mFileName.split("\\?");
            if (file.length > 0)
                mFileName = file[0];
        }
        if (mFileName.trim().equals("")) {
            mFileName = "file.bin";
        }
        return mFileName;
    }

    private void deleteIfImagesExist(ArrayList<String> deleteList) {
        if (FileUtils.isExternalStorageAvailable(10)) {
            try {
                File folderImage = new File(catalogImageDownloadProvider.getStorageDir(getResources().getString(R.string.app_name))
                        + "/"
                        + DataMembers.CATALOG);

                if (folderImage.exists()) {
                    for (String url : deleteList) {

                        String fileNames[] = url.split("/");
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

    private BroadcastReceiver downloadInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent

            if (intent.getStringExtra(CatalogDownloadConstants.DOWNLOAD_DETAIL) != null) {

                String message = intent.getStringExtra(CatalogDownloadConstants.DOWNLOAD_DETAIL);
                tvDownloadStatus.setText("Downloading " + message);

            }else if(intent.getStringExtra(CatalogDownloadConstants.STATUS) != null
                    && intent.getStringExtra(CatalogDownloadConstants.STATUS).equalsIgnoreCase(CatalogDownloadConstants.ERROR)){

                Toast.makeText(context, getResources().getString(R.string.connection_error_please_try_again), Toast.LENGTH_SHORT).show();
                finish();

            }else if (intent.getStringExtra(CatalogDownloadConstants.STATUS) != null
                    && intent.getStringExtra(CatalogDownloadConstants.STATUS).equalsIgnoreCase(CatalogDownloadConstants.COMPLETE)) {

                updateStatus();
            }


        }
    };

    public void updateDownloadStatus(Intent intent) {
        if (intent != null) {
            if (intent.getExtras() != null) {

                changeButtonState(View.INVISIBLE);
                Bundle b = intent.getExtras();
                if (b.getString("Error") != null) {
                    Toast.makeText(getApplicationContext(), b.getString("Error"), Toast.LENGTH_LONG).show();
                } else if (b.getInt("errorCode") != 0) {
                    if (b.getString("errorMessage") != null)
                        tvDownloadStatus.setText(b.getString("errorMessage"));
                } else if (b.getInt("Status") != 0) {
                    if (b.getInt("Status") == DataMembers.MESSAGE_UNZIPPED) {
                        updateStatus();
                    }
                }
            }

        }

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