package com.ivy.sd.png.bo;

import android.graphics.Bitmap;

import java.io.File;

public class ImageBO implements Comparable<ImageBO> {
	String imagepath, imagename, imagedirectory;
Bitmap filebitmap;
	int count;

	public String getCount() {
		return count + "";
	}

	public void setCount(int count) {
		this.count = count;
	}


	public String getImageDirectory() {
		return imagedirectory;
	}

	public void setImageDirectory(String imagedirectory) {
		this.imagedirectory = imagedirectory;
	}


public Bitmap getFilebitmap() {
	return filebitmap;
}

public void setFilebitmap(Bitmap filebitmap) {
	this.filebitmap = filebitmap;
}

public String getImagepath() {
	return imagepath;
}

public void setImagepath(String imagepath) {
	this.imagepath = imagepath;
}

public String getImagename() {
	return imagename;
}

public void setImagename(String imagename) {
	this.imagename = imagename;
}

	@Override
	public int compareTo(ImageBO another) {
		//new File(imagepath).lastModified()
		return new File(this.imagepath).lastModified() > new File(another.imagepath).lastModified() ? -1 : 1;
	}
}
