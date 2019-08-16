package com.ivy.sd.camera;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;

import java.io.File;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CameraActivity extends IvyBaseActivityNoActionBar {

    public static final String QUALITY = "quality";
    public static final String PATH = "path";
    public static final String ISSAVEREQUIRED = "IsSaveRequired";

    private static final int CAMERA_REQUEST = 1888;
    private static final int SUCCESS = 1;
    private String path = "";
    CameraHelper cameraHelper;
    boolean isCaptured = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraHelper = CameraHelper.getInstance(this);
        path = getIntent().getStringExtra("path");
        checkAndRequestPermissionAtRunTime(2);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!isCaptured) {
            cameraHelper.loadCameraPictureSize();
            cameraHelper.loadCameraConfiguration();
            int dbImageCount = cameraHelper.countImageFiles();
            if (dbImageCount >= cameraHelper.photocount) {
                Toast.makeText(this,
                        getResources()
                                .getString(
                                        R.string.you_have_already_taken_maximun_images)
                        , Toast.LENGTH_LONG).show();
                finish();
            } else {
                int permissionStatus = ContextCompat.checkSelfPermission(CameraActivity.this,
                        Manifest.permission.CAMERA);
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {

                    if (path != null && checkCreateDir(path)) {
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        if (Build.VERSION.SDK_INT >= 24) {
                            // set flag to give temporary permission to external app to use your FileProvider
                            cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                    FileProvider.getUriForFile(CameraActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(path)));

                            // validateData that the device can open your File!
                            PackageManager pm = this.getPackageManager();
                            if (cameraIntent.resolveActivity(pm) != null) {
                                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                            }
                        } else {
                            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(new File(path)));
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        }
                    } else {
                        Toast.makeText(this, "Image Path Not Found", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this,
                            getResources().getString(R.string.permission_enable_msg)
                                    + " " + getResources().getString(R.string.permission_camera)
                            , Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    Disposable disposable;
    AlertDialog alertDialog;
    AlertDialog.Builder builder;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            isCaptured = true;
//            new SaveNewOutlet().execute();
            builder = new AlertDialog.Builder(this);
            customProgressDialog(builder, getResources().getString(R.string.compressing_saving));
            alertDialog = builder.create();
            alertDialog.show();
            cameraHelper.compressTask(CameraActivity.this, path)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getCameraObserver());
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    private CompletableObserver getCameraObserver() {
        return new CompletableObserver(){
            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onComplete() {
                alertDialog.dismiss();
                CameraActivity.this.setResult(SUCCESS);
                CameraActivity.this.finish();
            }

            @Override
            public void onError(Throwable e) {
                alertDialog.dismiss();
            }
        };
    }

    private boolean checkCreateDir(String folderPath) {
        try {
            String folders[] = folderPath.split("/");
            String path = "";
            for (String folderName : folders) {
                if (!folderName
                        .endsWith(getResources().getString(R.string.jpg))
                        && !folderName.endsWith(getResources().getString(
                        R.string.png))
                        && !folderName.endsWith(getResources().getString(
                        R.string.gif)))
                    path += folderName
                            + "/";
                File SDPath = new File(path);
                if (!SDPath.exists()) {
                    if (!SDPath.mkdir()) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(disposable!=null)
            disposable.dispose();
    }

}