package com.ivy.sd.camera;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Environment;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.data.datamanager.DataManagerImpl;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.RetailerHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class CameraHelper {
    private Context mContext;
    private BusinessModel bmodel;
    private static CameraHelper instance = null;

    public boolean IS_ENABLE_CAMERA_PICTURE_SIZE = false;
    public int CAMERA_PICTURE_WIDTH = 640;
    public int CAMERA_PICTURE_HEIGHT = 480;
    public int CAMERA_PICTURE_QUALITY = 40;

    public int photocount;

    protected CameraHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static CameraHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CameraHelper(context);
        }
        return instance;
    }

    public void loadCameraPictureSize() {
        try {

            String codeValue;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='PHOTOCAP04' and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                    String[] camera_params = codeValue.split(",");
                    CAMERA_PICTURE_WIDTH = SDUtil.convertToInt(camera_params[0]);
                    CAMERA_PICTURE_HEIGHT = SDUtil.convertToInt(camera_params[1]);
                    CAMERA_PICTURE_QUALITY = SDUtil.convertToInt(camera_params[2]) >= 40 ? SDUtil.convertToInt(camera_params[2]) : 40;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    /**
     * @return imageCount
     */
    public int countImageFiles() {
        int imageSize = 0;
        try {
            File f = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    + "/" + DataMembers.photoFolderName + "/");
            if (f.exists()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        if (fileName.endsWith(".pdf")) {
                            return fileName.endsWith(".pdf");
                        }
                        return fileName.endsWith(".jpg");
                    }
                });

                File printfiles[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        return fileName.startsWith("PF");
                    }
                });

                imageSize = (bmodel.configurationMasterHelper.IS_PRINT_FILE_SAVE) ? files.length + printfiles.length
                        : files.length;
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return imageSize;
    }

    public void loadCameraConfiguration() {
        try {
            photocount = 0;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            String sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode= 'PHOTOCAP01' and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    photocount = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public Completable compressTask(Context context,String path){
//        ((BaseActivity) context).showLoading();
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) {
                compressImage(path);
                if (!emitter.isDisposed()) {
                    emitter.onComplete();
                }
            }
        });
    }

    /**
     * Method is used to compress the image file
     *
     * @param path      - path of the image file
     * @param maxHeight - define the maximum height of the image file
     * @param maxWidth  - define the maximum height of the image file
     */
    public void compressImage(String path) {
        float maxHeight = CAMERA_PICTURE_WIDTH;
        float maxWidth = CAMERA_PICTURE_HEIGHT;
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
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, CAMERA_PICTURE_QUALITY, out);
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


}
