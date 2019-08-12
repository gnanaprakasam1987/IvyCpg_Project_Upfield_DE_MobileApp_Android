package com.ivy.ui.gallery;

import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.ui.gallery.model.GalleryBo;

import java.util.ArrayList;
import java.util.HashMap;

public class GalleryDataFactory {

    public static RetailerMasterBO retailerMasterBO = new RetailerMasterBO("1", "abcd");

    public static UserMasterBO userMasterBO = new UserMasterBO(2, "user1");

    public static HashMap<String, ArrayList<GalleryBo>> getMockHashList() {
        ArrayList<GalleryBo> mockimageList = new ArrayList<>();
        HashMap<String, ArrayList<GalleryBo>> mockHashList = new HashMap<>();
        GalleryBo mockImgBo = new GalleryBo();

        mockImgBo.setImageName("ImageName.jpg");
        mockImgBo.setFilePath("dist/ImageName.jpg");
        mockImgBo.setRetailerName("Retailer");
        mockImgBo.setName("Desc");
        mockImgBo.setDate("12/06/2019");
        mockImgBo.setSource("Section");
        mockimageList.add(mockImgBo);

        mockImgBo.setImageName("ImageName1.jpg");
        mockImgBo.setFilePath("dist/ImageName1.jpg");
        mockImgBo.setRetailerName("Retailer1");
        mockImgBo.setName("Desc1");
        mockImgBo.setDate("12/07/2019");
        mockImgBo.setSource("Section");
        mockimageList.add(mockImgBo);

        mockImgBo.setImageName("ImageName2.jpg");
        mockImgBo.setFilePath("dist/ImageName2.jpg");
        mockImgBo.setRetailerName("Retailer2");
        mockImgBo.setName("Desc2");
        mockImgBo.setDate("12/05/2019");
        mockImgBo.setSource("Section");
        mockimageList.add(mockImgBo);

        mockHashList.put("Section", mockimageList);
        return mockHashList;
    }
}
