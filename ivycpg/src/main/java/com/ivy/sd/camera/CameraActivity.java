package com.ivy.sd.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;

public class CameraActivity extends IvyBaseActivityNoActionBar {

    public static final String QUALITY = "quality";
    public static final String PATH = "path";
    public static final String ISSAVEREQUIRED = "IsSaveRequired";

    private static final int RESULT_SAVE = 1;
    private static final int RESULT_SAVE_NATIVE = 2;
    private BusinessModel bmodel;
    private String path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        path = getIntent().getStringExtra("path");
        checkAndRequestPermissionAtRunTime(2);
    }

    @Override
    public void onResume() {
        super.onResume();
        int dbImageCount = bmodel.synchronizationHelper
                .countImageFiles();
        if (dbImageCount >= bmodel.configurationMasterHelper.photocount) {
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
                 Intent intent = new Intent(CameraActivity.this, NativeCameraActivity.class);
                    intent.putExtra("path", path);
                    startActivityForResult(intent, RESULT_SAVE_NATIVE);

            } else {
                Toast.makeText(this,
                        getResources().getString(R.string.permission_enable_msg)
                                + " " + getResources().getString(R.string.permission_camera)
                        , Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == RESULT_SAVE || requestCode == RESULT_SAVE_NATIVE) && resultCode == RESULT_SAVE) {
            setResult(RESULT_SAVE);
        }
        finish();
    }
}