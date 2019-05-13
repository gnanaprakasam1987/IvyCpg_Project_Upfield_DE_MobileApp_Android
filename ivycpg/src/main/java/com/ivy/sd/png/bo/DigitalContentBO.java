package com.ivy.sd.png.bo;

import android.graphics.Bitmap;

import java.util.Comparator;

public class DigitalContentBO {
	private int productID, imageID;

	private String fileName;
	private String description;
	private String imageDate, productName;
	private int textbgcolor;
	private int textcolor;
	private Bitmap bitmap;
	private boolean lessimagewidth;
	private int imgFlag;
	private String groupName;
	private String parentHierarchy;
	private int sequenceNo;

	public boolean isAllowSharing() {
		return isAllowSharing;
	}


	public void setAllowSharing(boolean allowSharing) {
		isAllowSharing = allowSharing;
	}

	private boolean isAllowSharing;

	public DigitalContentBO() {
	}

	public boolean isHeader() {
		return isHeader;
	}

	public void setHeader(boolean header) {
		isHeader = header;
	}

	private boolean isHeader;
	private boolean isGroupHeader;

	public boolean isGroupHeader() {
		return isGroupHeader;
	}

	public void setGroupHeader(boolean groupHeader) {
		isGroupHeader = groupHeader;
	}

	public int getSequenceNo() {
		return sequenceNo;
	}

	public void setSequenceNo(int sequenceNo) {
		this.sequenceNo = sequenceNo;
	}

	public String getHeaderTitle() {
		return headerTitle;
	}

	public void setHeaderTitle(String headerTitle) {
		this.headerTitle = headerTitle;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	private String headerTitle;
	public boolean isLessimagewidth() {
		return lessimagewidth;
	}

	public void setLessimagewidth(boolean lessimagewidth) {
		this.lessimagewidth = lessimagewidth;
	}

	public int getTextcolor() {
		return textcolor;
	}

	public void setTextcolor(int textcolor) {
		this.textcolor = textcolor;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public Integer getTextbgcolor() {
		return textbgcolor;
	}

	public void setTextbgcolor(int textbgcolor) {
		this.textbgcolor = textbgcolor;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getProductID() {
		return productID;
	}

	public void setProductID(int productID) {
		this.productID = productID;
	}

	public int getImageID() {
		return imageID;
	}

	public void setImageID(int imageID) {
		this.imageID = imageID;
	}

	public String getImageDate() {
		return imageDate;
	}

	public void setImageDate(String imageDate) {
		this.imageDate = imageDate;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getImgFlag() {
		return imgFlag;
	}

	public void setImgFlag(int imgFlag) {
		this.imgFlag = imgFlag;
	}

	//used for moving jpg,png type file first
	public static final Comparator<DigitalContentBO> imgFileCompartor = new Comparator<DigitalContentBO>() {

		public int compare(DigitalContentBO file1, DigitalContentBO file2) {


			//acs
			return file1.imgFlag - file2.imgFlag;

			// descending order
			// return file2.imgFlag - file1.imgFlag;
		}

	};

	public static final Comparator<DigitalContentBO> dateCompartor = new Comparator<DigitalContentBO>() {

		public int compare(DigitalContentBO file1, DigitalContentBO file2) {


			return file2.getImageDate().compareTo(file1.getImageDate());

		}

	};

	public static final Comparator<DigitalContentBO> sequenceComparotr = new Comparator<DigitalContentBO>() {
		@Override
		public int compare(DigitalContentBO seq1, DigitalContentBO seq2) {
			if(seq1.getSequenceNo()<seq2.getSequenceNo())
				return seq1.getSequenceNo();
			else
				return seq2.getSequenceNo();
		}
	};

	public String getParentHierarchy() {
		return parentHierarchy;
	}

	public void setParentHierarchy(String parentHierarchy) {
		this.parentHierarchy = parentHierarchy;
	}
}
