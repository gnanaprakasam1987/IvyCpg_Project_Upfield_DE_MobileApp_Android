package com.ivy.cpg.view.sync.largefiledownload;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;

import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.database.DownloadModel;
import com.downloader.internal.ComponentHolder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.NetworkUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class FileDownloadProvider {

    public static final int MB_IN_BYTES = 1048576;
    private static final String LARGE_FILE_PREF_LIST = "large_file_download_pref";
    public static final String DIGITALCONTENT = "DC";
    public static final String STATUS_ERROR = "observer_error";
    public static final String DONE = "DONE";
    public static FileDownloadProvider instance;
    private BusinessModel businessModel;

    private FileDownloadProvider(Context context) {
        this.businessModel = (BusinessModel) context.getApplicationContext();
    }

    public static FileDownloadProvider getInstance(Context context) {
        if (instance == null) {
            instance = new FileDownloadProvider(context);
        }
        return instance;
    }

    /**
     * This method will be called from Initial Login Download and Local login and resuming the Download
     * File Download Url prepared and Parceled to Service Class
     */
    public void callFileDownload(Context context) {

        getDigitalDownloadList(context);
        if (NetworkUtils.isNetworkConnected(context) &&
                !FileDownloadIntentService.isServiceRunning &&
                businessModel.getDigitalContentLargeFileURLS().size() > 0){

            Intent intent = new Intent(context,FileDownloadIntentService.class);
            intent.putParcelableArrayListExtra("DigiContent",businessModel.getDigitalContentLargeFileURLS());
            intent.putExtra("DownloadType",businessModel.configurationMasterHelper.IS_AZURE_CLOUD_STORAGE?"AZURE":"AWS");

            context.startService(intent);
        }
    }

    /**
     * Checks the Shared pref Saved list with server list and Update the List values
     * Check the file folder whether already downloaded or not
     */
    public ArrayList<DigitalContentModel> getDigitalDownloadList(Context context){

        businessModel.isDigitalContentAvailable();

        ArrayList<DigitalContentModel> digitalContentSavedList = getDigitalContentList();

        if (businessModel.getDigitalContentLargeFileURLS().size() == 0) {

            if (digitalContentSavedList != null && digitalContentSavedList.size() > 0){
                clearSavedValues(DIGITALCONTENT);
            }
            return new ArrayList<>();
        }

        // Initializing PR-DOWNLOADER
        // Enabling database for resume support even after the application is killed:
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .build();
        PRDownloader.initialize(context.getApplicationContext(), config);

        for (DigitalContentModel digitalContentModel : businessModel.getDigitalContentLargeFileURLS()){
            if (digitalContentSavedList != null && digitalContentSavedList.size() > 0){
              for (int i = 0; i<digitalContentSavedList.size(); i++) {
                  if (digitalContentSavedList.get(i).getImageID() == digitalContentModel.getImageID()) {

                      digitalContentModel.setPercent(digitalContentSavedList.get(i).getPercent());
                      digitalContentModel.setDownloadDetail(digitalContentSavedList.get(i).getDownloadDetail());
                      digitalContentModel.setFileName(digitalContentSavedList.get(i).getFileName());
                      digitalContentModel.setStatus(digitalContentSavedList.get(i).getStatus());
                      digitalContentModel.setDownloadId(digitalContentSavedList.get(i).getDownloadId());
                      digitalContentModel.setSignedUrl(digitalContentSavedList.get(i).getSignedUrl());

                      break;
                  }else if (digitalContentSavedList.size()-1 == i){
                      isAlreadyDownloaded(context, digitalContentModel);
                  }
              }
            }else
                isAlreadyDownloaded(context, digitalContentModel);

        }

        return businessModel.getDigitalContentLargeFileURLS();
    }

    /**
     * Checks Whether already file downloaded or not
     */
    private void isAlreadyDownloaded(Context context,DigitalContentModel digitalContentModel) {

        int userId = digitalContentModel.getUserId();

        File mTranDevicePath, mAppDevicePath,mPrintDevicePath, mPrintFileDevicePath;

        mTranDevicePath = new File(
                context
                        .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        + "/" + userId + DataMembers.DIGITAL_CONTENT);

        mAppDevicePath = new File(
                context
                        .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        + "/"
                        + userId
                        + DataMembers.APP_DIGITAL_CONTENT);

        mPrintDevicePath = new File(
                context
                        .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        + "/"
                        + userId
                        + DataMembers.PRINT);

        mPrintFileDevicePath = new File(
                context
                        .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        + "/"
                        + userId
                        + DataMembers.PRINTFILE);

        String mFileName;
        int index;
        File mfile, appfile, mPrintFile, mPrintFormatFile;
        boolean availe_flag;

        mFileName = "file.bin";

        index = digitalContentModel.getImgUrl().lastIndexOf('/');

        if (index >= 0) {
            mFileName = digitalContentModel.getImgUrl().substring(index + 1);
        }
        if (mFileName.equals("")) {
            mFileName = "file.bin";
        }
        // read and write the content

        mfile = new File(mTranDevicePath + "/" + digitalContentModel.getContentFrom() + "/" + mFileName);
        appfile = new File(mAppDevicePath + "/" + mFileName);
        mPrintFile = new File(mPrintDevicePath + "/" + mFileName);
        mPrintFormatFile = new File(mPrintFileDevicePath + "/" + mFileName);

        String fileSize = "0MB";
        DecimalFormat df = new DecimalFormat("0.000");

        if (mfile.exists()) {
            availe_flag = true;
            fileSize = String.valueOf(df.format((double)mfile.length()/(double) MB_IN_BYTES));
        } else if (appfile.exists()) {
            availe_flag = true;
            fileSize = String.valueOf(df.format((double)appfile.length()/(double) MB_IN_BYTES));
        } else if (mPrintFile.exists()) {
            availe_flag = true;
            fileSize = String.valueOf(df.format((double)mPrintFile.length()/(double) MB_IN_BYTES));
        } else if (mPrintFormatFile.exists()) {
            availe_flag = true;
            fileSize = String.valueOf(df.format((double)mPrintFormatFile.length()/(double) MB_IN_BYTES));
        } else {
            availe_flag = false;
        }

        if (availe_flag) {

            DownloadModel model = ComponentHolder.getInstance().getDbHelper().find(digitalContentModel.getDownloadId());
            if (model == null) {

                fileSize = fileSize+"MB/"+fileSize+"MB";

                digitalContentModel.setDownloadDetail(fileSize);
                digitalContentModel.setPercent(100);
                digitalContentModel.setStatus(DONE);
            }else {
                String fileTtlSize = String.valueOf(df.format((double)model.getTotalBytes()/(double) MB_IN_BYTES));
                String fileDownladSize = String.valueOf(df.format((double)model.getDownloadedBytes()/(double) MB_IN_BYTES));
                fileSize = fileDownladSize+"MB/"+fileTtlSize+"MB";
                digitalContentModel.setDownloadDetail(fileSize);

                int downloadPercentage = (int) (((float) model.getDownloadedBytes() / (float) model.getTotalBytes()) * 100);

                digitalContentModel.setPercent(downloadPercentage);
                digitalContentModel.setStatus(downloadPercentage==100?DONE:"");
            }
        }else{
            digitalContentModel.setDownloadDetail(null);
            digitalContentModel.setPercent(0);
            digitalContentModel.setStatus("");
        }
    }

    /**
     * Returns the Saved list from Shared Preference
     */
    public ArrayList<DigitalContentModel> getDigitalContentList(){
        Gson gson = new Gson();
        String json = businessModel.getSharedPreferences(LARGE_FILE_PREF_LIST, MODE_PRIVATE).getString(DIGITALCONTENT, null);
        Type type = new TypeToken<ArrayList<DigitalContentModel>>() {}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Clear the Saved Preference datas
     */
    private void clearSavedValues( String key){
        SharedPreferences.Editor editor = businessModel.getSharedPreferences(LARGE_FILE_PREF_LIST, MODE_PRIVATE).edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * Save only the OnProgress and Error model
     * Completed File will be removed
     */
    public void prepareDigitalContentSaveList(DigitalContentModel digitalContentModel){

        ArrayList<DigitalContentModel> digitalContentList = getDigitalContentList();

        if (digitalContentList == null){
            digitalContentList = new ArrayList<>();
            digitalContentList.add(digitalContentModel);
        }else {

            boolean isIdFound = false;
            for (DigitalContentModel digitalContentSavedModel : digitalContentList){
                if (digitalContentSavedModel.getImageID() == digitalContentModel.getImageID()){

                    if (digitalContentModel.getPercent() == 100) {
                        isIdFound = true;
                        digitalContentList.remove(digitalContentSavedModel);
                        break;
                    }

                    digitalContentSavedModel.setStatus(digitalContentModel.getStatus());
                    digitalContentSavedModel.setPercent(digitalContentModel.getPercent());
                    digitalContentSavedModel.setDownloadDetail(digitalContentModel.getDownloadDetail());
                    digitalContentSavedModel.setDownloadId(digitalContentModel.getDownloadId());
                    digitalContentSavedModel.setSignedUrl(digitalContentModel.getSignedUrl());

                    isIdFound = true;
                    break;
                }
            }

            if (!isIdFound){
                digitalContentList.add(digitalContentModel);
            }
        }

        saveInSharedPrefList(digitalContentList);

    }

    /**
     * Particular Digital content model will be removed from the Saved Shared Preference list
     */
    public void removeDigitalContentModel(int imageId){

        ArrayList<DigitalContentModel> digitalContentList = getDigitalContentList();
        if (digitalContentList == null)
            return;

        for (DigitalContentModel digitalContentSavedModel : digitalContentList){
            if (digitalContentSavedModel.getImageID() == imageId) {
                digitalContentList.remove(digitalContentSavedModel);
                saveInSharedPrefList(digitalContentList);
                break;
            }
        }
    }

    /**
     * Saving Digital Content list in Shared Preference
     */
    private void saveInSharedPrefList(ArrayList<DigitalContentModel> digitalContentList) {
        SharedPreferences.Editor editor = businessModel.getSharedPreferences(LARGE_FILE_PREF_LIST, MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(digitalContentList);
        editor.putString(DIGITALCONTENT, json);
        editor.apply();
    }

}
