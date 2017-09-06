package com.ivy.sd.png.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.NewOutletAttributeHelper;

import java.security.MessageDigest;

/**
 * Created by anish.k on 8/23/2017.
 */

public class CircleTransform extends BitmapTransformation {
    private BusinessModel bmodel;
    static CircleTransform instance = null;
    Context context;


    public static CircleTransform getInstance(Context context) {
        if (instance == null) {
            instance = new CircleTransform(context);
        }
        return instance;
    }

    public CircleTransform(Context context)
    {
        super(context);
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return circleCrop(pool, toTransform);
    }

    private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
        if (source == null) return null;

        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

        Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        return result;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

//    @Override
//    public void updateDiskCacheKey(MessageDigest messageDigest) {
//
//    }
}

