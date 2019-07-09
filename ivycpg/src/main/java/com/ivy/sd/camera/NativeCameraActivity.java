package com.ivy.sd.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class NativeCameraActivity extends Activity {
    private static final int CAMERA_REQUEST = 1888;
    private static final int SUCCESS = 1;
    private String path = "";
    private float camera_picture_width;
    private float camera_picture_height;
    private int camera_picture_quality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusinessModel bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        camera_picture_width = (float) bmodel.configurationMasterHelper.CAMERA_PICTURE_WIDTH;
        camera_picture_height = (float) bmodel.configurationMasterHelper.CAMERA_PICTURE_HEIGHT;
        camera_picture_quality = bmodel.configurationMasterHelper.CAMERA_PICTURE_QUALITY;

        path = getIntent().getStringExtra("path");
        if (path != null && checkCreateDir(path)) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            if (Build.VERSION.SDK_INT >= 24) {

                // set flag to give temporary permission to external app to use your FileProvider
                cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(NativeCameraActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(path)));

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            compressImage(path, camera_picture_height, camera_picture_width);
            setResult(SUCCESS);
            finish();
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    /**
     * Method is used to compress the image file
     *
     * @param path      - path of the image file
     * @param maxHeight - define the maximum height of the image file
     * @param maxWidth  - define the maximum height of the image file
     */
    private void compressImage(String path, float maxHeight, float maxWidth) {
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        try {
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;
                }
            }
        } catch (ArithmeticException e) {
            Commons.printException("" + e);
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];
        try {
//          load the bitmap from its path
            bitmap = BitmapFactory.decodeFile(path, options);
        } catch (OutOfMemoryError exception) {
            Commons.printException("" + exception);

        }
        try {
            if (actualWidth > 0 && actualHeight > 0)
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            Commons.printException("" + exception);
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        if (scaledBitmap != null) {
            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

////      check the rotation of the image and display it properly
            ExifInterface exif;
            try {
                exif = new ExifInterface(path);

                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 0);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                        true);
            } catch (IOException e) {
                Commons.printException("" + e);
            }

            try {
                FileOutputStream out;
                try {
                    out = new FileOutputStream(path);

//                  write the compressed bitmap at the destination specified by filename.
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, camera_picture_quality, out);
                    scaledBitmap.recycle();//need to recycle the bitmap to avoid OUTOFMEMORYERROR
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {
                    Commons.printException("" + e);
                }
            } catch (IOException e) {
                Commons.printException("" + e);
            }
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = (float) width * height;
        final float totalReqPixelsCap = (float) reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
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
}
