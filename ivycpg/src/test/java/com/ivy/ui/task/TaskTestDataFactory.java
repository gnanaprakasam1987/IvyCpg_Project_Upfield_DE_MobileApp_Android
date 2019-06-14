package com.ivy.ui.task;

import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.ui.task.model.TaskDataBO;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskTestDataFactory {

    public static RetailerMasterBO retailerMasterBO = new RetailerMasterBO(1, "abcd");

    public static UserMasterBO userMasterBO = new UserMasterBO(2, "user1");

    public static HashMap<String, String> getLabels() {
        HashMap<String, String> labels = new HashMap<>();
        labels.put("acb", "Title");
        labels.put("abcd", "Desc");
        labels.put("abcde", "Date");
        labels.put("abcdef", "Modified");
        return labels;

    }

    public static ArrayList<UserMasterBO> getParentUserList() {

        ArrayList<UserMasterBO> mockUserList = new ArrayList<>();

        mockUserList.add(new UserMasterBO(1, "user1"));
        mockUserList.add(new UserMasterBO(2, "user2"));
        mockUserList.add(new UserMasterBO(3, "user3"));
        mockUserList.add(new UserMasterBO(4, "user4"));
        mockUserList.add(new UserMasterBO(5, "user5"));

        return mockUserList;
    }


    public static ArrayList<UserMasterBO> getChildUserList() {
        ArrayList<UserMasterBO> mockUserList = new ArrayList<>();

        mockUserList.add(new UserMasterBO(11, "child user1"));
        mockUserList.add(new UserMasterBO(22, "child user2"));
        mockUserList.add(new UserMasterBO(33, "child user3"));
        mockUserList.add(new UserMasterBO(44, "child user4"));
        mockUserList.add(new UserMasterBO(55, "child user5"));

        return mockUserList;
    }

    public static ArrayList<UserMasterBO> getPeerUserList() {
        ArrayList<UserMasterBO> mockUserList = new ArrayList<>();

        mockUserList.add(new UserMasterBO(111, "peer user1"));
        mockUserList.add(new UserMasterBO(222, "peer user2"));
        mockUserList.add(new UserMasterBO(333, "peer user3"));
        mockUserList.add(new UserMasterBO(444, "peer user4"));
        mockUserList.add(new UserMasterBO(555, "peeruser5"));

        return mockUserList;
    }

    public static HashMap<String, ArrayList<UserMasterBO>> getLinkUserList() {
        HashMap<String, ArrayList<UserMasterBO>> mockLabels = new HashMap<>();
        mockLabels.put("1", getParentUserList());
        mockLabels.put("2", getParentUserList());
        mockLabels.put("3", getParentUserList());
        mockLabels.put("4", getParentUserList());

        return mockLabels;
    }

    public static ArrayList<RetailerMasterBO> getAllRetailer() {
        ArrayList<RetailerMasterBO> mockRetList = new ArrayList<>();

        mockRetList.add(new RetailerMasterBO(1, "abcd"));
        mockRetList.add(new RetailerMasterBO(2, "abcde"));
        mockRetList.add(new RetailerMasterBO(3, "abcdef"));
        mockRetList.add(new RetailerMasterBO(4, "abcdefg"));
        mockRetList.add(new RetailerMasterBO(5, "abcdefgh"));
        mockRetList.add(new RetailerMasterBO(6, "abcdefghi"));
        return mockRetList;
    }

    public static ArrayList<TaskDataBO> getTaskImgList() {
        ArrayList<TaskDataBO> mockImageList = new ArrayList<>();
        TaskDataBO mockImgBo = new TaskDataBO();

        mockImgBo.setTaskId("1");
        mockImgBo.setTaskImg("image1");
        mockImgBo.setTaskImgPath("image path1");
        mockImageList.add(mockImgBo);

        mockImgBo.setTaskId("2");
        mockImgBo.setTaskImg("image2");
        mockImgBo.setTaskImgPath("image path2");
        mockImageList.add(mockImgBo);

        mockImgBo.setTaskId("3");
        mockImgBo.setTaskImg("image3");
        mockImgBo.setTaskImgPath("image path3");
        mockImageList.add(mockImgBo);

        return mockImageList;
    }

    public static ArrayList<TaskDataBO> getProductList() {
        ArrayList<TaskDataBO> mockProdList = new ArrayList<>();
        TaskDataBO mockImgBo = new TaskDataBO();

        mockImgBo.setTaskCategoryID(10);
        mockImgBo.setTaskCategoryDsc("sku");
        mockProdList.add(mockImgBo);

        mockImgBo.setTaskCategoryID(20);
        mockImgBo.setTaskCategoryDsc("brand");
        mockProdList.add(mockImgBo);

        mockImgBo.setTaskCategoryID(30);
        mockImgBo.setTaskCategoryDsc("sub brand");
        mockProdList.add(mockImgBo);

        return mockProdList;
    }


}
