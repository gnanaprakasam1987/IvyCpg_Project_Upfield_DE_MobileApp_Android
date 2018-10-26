package com.ivy.utils;

import android.content.Context;
import android.graphics.Typeface;

public class FontUtils {


    public enum FontType {
        LIGHT, MEDIUM, THIN, REGULAR
    }


    public static Typeface getFontBalooHai(Context mContext, FontType mFontType) {
        Typeface mFontBaloobhaiRegular;
        if (mFontType == FontType.REGULAR) {
            mFontBaloobhaiRegular = Typeface.createFromAsset(mContext.getAssets(), "font/baloobhai_regular.ttf");

            return mFontBaloobhaiRegular;
        }

        return Typeface.createFromAsset(mContext.getAssets(), "font/baloobhai_regular.ttf");
    }

    public static Typeface getFontRoboto(Context context, FontType mFontType) {

        Typeface mFontRobotoThin;
        if (mFontType == FontType.LIGHT) {
            mFontRobotoThin = Typeface.createFromAsset(context.getAssets(), "font/Roboto-Light.ttf");
            return mFontRobotoThin;
        } else if (mFontType == FontType.MEDIUM) {
            mFontRobotoThin = Typeface.createFromAsset(context.getAssets(), "font/Roboto-Medium.ttf");
            return mFontRobotoThin;
        } else if (mFontType == FontType.THIN) {
            mFontRobotoThin = Typeface.createFromAsset(context.getAssets(), "font/Roboto-Thin.ttf");
            return mFontRobotoThin;
        }

        return Typeface.createFromAsset(context.getAssets(), "font/Roboto-Medium.ttf");
    }

    public static Typeface getProductNameFont(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "font/Roboto-Medium.ttf");
    }
}
