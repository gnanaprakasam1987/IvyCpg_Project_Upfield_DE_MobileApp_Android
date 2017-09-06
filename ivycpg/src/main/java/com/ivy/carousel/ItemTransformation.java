package com.ivy.carousel;

/**
 * Created by dharmapriya.k on 10/20/2016,3:52 PM.
 */
public class ItemTransformation {

    final float mScaleX;
    final float mScaleY;
    final float mTranslationX;
    final float mTranslationY;

    public ItemTransformation(final float scaleX, final float scaleY, final float translationX, final float translationY) {
        mScaleX = scaleX;
        mScaleY = scaleY;
        mTranslationX = translationX;
        mTranslationY = translationY;
    }
}
