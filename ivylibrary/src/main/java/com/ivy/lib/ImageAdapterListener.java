package com.ivy.lib;

public interface ImageAdapterListener {

    void onTakePhoto();

    void deletePhoto(String fileName,int position);

}
