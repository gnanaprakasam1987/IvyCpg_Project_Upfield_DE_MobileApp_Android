package com.ivy.ui.task;

import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.ui.task.model.FilterBo;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.ui.task.model.TaskRetailerBo;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskTestDataFactory {

    public static RetailerMasterBO retailerMasterBO = new RetailerMasterBO("1", "abcd");

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

        mockRetList.add(new RetailerMasterBO("1", "abcd"));
        mockRetList.add(new RetailerMasterBO("2", "abcde"));
        mockRetList.add(new RetailerMasterBO("3", "abcdef"));
        mockRetList.add(new RetailerMasterBO("4", "abcdefg"));
        mockRetList.add(new RetailerMasterBO("5", "abcdefgh"));
        mockRetList.add(new RetailerMasterBO("6", "abcdefghi"));
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

    public static ArrayList<TaskDataBO> getCompletedTask() {
        ArrayList<TaskDataBO> mockTaskList = new ArrayList<>();
        TaskDataBO mockImgBo = new TaskDataBO();

        mockImgBo.setTaskId("1");
        mockImgBo.setUserId(123);
        mockImgBo.setTaskDesc("task Desc");
        mockImgBo.setTasktitle("task title1");
        mockImgBo.setTaskDueDate("12/06/2019");
        mockTaskList.add(mockImgBo);

        mockImgBo.setTaskId("2");
        mockImgBo.setUserId(123);
        mockImgBo.setTaskDesc("task Desc2");
        mockImgBo.setTasktitle("task title2");
        mockImgBo.setTaskDueDate("25/06/2019");
        mockTaskList.add(mockImgBo);

        mockImgBo.setTaskId("3");
        mockImgBo.setUserId(123);
        mockImgBo.setTaskDesc("task Desc3");
        mockImgBo.setTasktitle("task title3");
        mockImgBo.setTaskDueDate("12/07/2019");
        mockTaskList.add(mockImgBo);

        return mockTaskList;
    }

    public static ArrayList<ReasonMaster> getReasonList() {
        ArrayList<ReasonMaster> mockReasonList = new ArrayList<>();

        mockReasonList.add(new ReasonMaster("1", "Reason1"));
        mockReasonList.add(new ReasonMaster("2", "Reason2"));
        mockReasonList.add(new ReasonMaster("3", "Reason3"));
        mockReasonList.add(new ReasonMaster("4", "Reason4"));
        return mockReasonList;
    }

    public static String getChannelIdList() {
        StringBuilder mockChannelIds = new StringBuilder();

        mockChannelIds.append("1213,");
        mockChannelIds.append("1214,");
        mockChannelIds.append("1215,");
        mockChannelIds.append("1216,");
        mockChannelIds.append("1217");
        return mockChannelIds.toString();
    }

    public static ArrayList<TaskDataBO> getTaskWithoutUserId() {
        ArrayList<TaskDataBO> mockTaskList = new ArrayList<>();
        TaskDataBO mockImgBo = new TaskDataBO();

        mockImgBo.setTaskId("1");
        mockImgBo.setTaskDesc("task Desc");
        mockImgBo.setTasktitle("task title1");
        mockImgBo.setTaskDueDate("12/06/2019");
        mockTaskList.add(mockImgBo);

        mockImgBo.setTaskId("2");
        mockImgBo.setTaskDesc("task Desc2");
        mockImgBo.setTasktitle("task title2");
        mockImgBo.setTaskDueDate("25/06/2019");
        mockTaskList.add(mockImgBo);

        mockImgBo.setTaskId("3");
        mockImgBo.setTaskDesc("task Desc3");
        mockImgBo.setTasktitle("task title3");
        mockImgBo.setTaskDueDate("12/07/2019");
        mockTaskList.add(mockImgBo);

        return mockTaskList;
    }

    public static ArrayList<TaskDataBO> getTaskByRetailerWise() {
        ArrayList<TaskDataBO> mockTaskList = new ArrayList<>();
        TaskDataBO mockBo = new TaskDataBO();

        mockBo.setTaskId("11");
        mockBo.setUserId(123);
        mockBo.setRid(1);
        mockBo.setChannelId(1213);
        mockBo.setTaskDesc("task Desc");
        mockBo.setTasktitle("task title1");
        mockBo.setTaskDueDate("12/06/2019");
        mockTaskList.add(mockBo);

        mockBo.setTaskId("1");
        mockBo.setUserId(123);
        mockBo.setRid(1);
        mockBo.setChannelId(1213);
        mockBo.setTaskDesc("task Desc");
        mockBo.setTasktitle("task title1");
        mockBo.setTaskDueDate("12/06/2019");
        mockTaskList.add(mockBo);

        mockBo.setTaskId("2");
        mockBo.setUserId(123);
        mockBo.setChannelId(1214);
        mockBo.setRid(1);
        mockBo.setTaskDesc("task Desc2");
        mockBo.setTasktitle("task title2");
        mockBo.setTaskDueDate("25/06/2019");
        mockTaskList.add(mockBo);

        mockBo.setTaskId("3");
        mockBo.setUserId(123);
        mockBo.setChannelId(1215);
        mockBo.setRid(1);
        mockBo.setTaskDesc("task Desc3");
        mockBo.setTasktitle("task title3");
        mockBo.setTaskDueDate("12/07/2019");
        mockTaskList.add(mockBo);

        return mockTaskList;
    }

    public static TaskDataBO getMockTaskBo() {
        TaskDataBO taskDataBO = new TaskDataBO();

        taskDataBO.setTaskId("1");
        taskDataBO.setUserId(123);
        taskDataBO.setRid(0);
        taskDataBO.setMode(TaskConstant.SELLER_WISE);
        taskDataBO.setTaskDesc("task Desc");
        taskDataBO.setTasktitle("task title1");
        taskDataBO.setTaskDueDate("12/06/2019");
        return taskDataBO;
    }

    public static TaskDataBO getMockTaskBoWithDueDateNull() {
        TaskDataBO taskDataBO = new TaskDataBO();

        taskDataBO.setTaskId(null);
        taskDataBO.setUserId(123);
        taskDataBO.setRid(0);
        taskDataBO.setTaskDueDate(null);
        taskDataBO.setMode(TaskConstant.SELLER_WISE);
        taskDataBO.setTaskDesc(null);
        taskDataBO.setTasktitle("task title1");
        return taskDataBO;
    }


    public static ArrayList<TaskRetailerBo> getMockRetTaskList() {
        ArrayList<TaskRetailerBo> mockRetTaskList = new ArrayList<>();
        TaskRetailerBo taskRetailerBo = new TaskRetailerBo();

        taskRetailerBo.setRetailerId("1");
        taskRetailerBo.setRetailerName("Retailer1");
        taskRetailerBo.setRetAddress("Address1");
        taskRetailerBo.setNextVisitDaysCount(5);
        taskRetailerBo.setLastVisitDate("12/06/2019");
        mockRetTaskList.add(taskRetailerBo);

        return mockRetTaskList;
    }


    public static HashMap<String, ArrayList<TaskDataBO>> getMockHashList() {
        ArrayList<TaskDataBO> mockTaskList = new ArrayList<>();
        HashMap<String, ArrayList<TaskDataBO>> mockHashList = new HashMap<>();
        TaskDataBO mockImgBo = new TaskDataBO();

        mockImgBo.setTaskId("1");
        mockImgBo.setUserId(123);
        mockImgBo.setRid(1);
        mockImgBo.setTaskDesc("task Desc");
        mockImgBo.setTasktitle("task title1");
        mockImgBo.setTaskDueDate("12/06/2019");
        mockTaskList.add(mockImgBo);

        mockImgBo.setTaskId("2");
        mockImgBo.setUserId(123);
        mockImgBo.setRid(1);
        mockImgBo.setTaskDesc("task Desc2");
        mockImgBo.setTasktitle("task title2");
        mockImgBo.setTaskDueDate("25/06/2019");
        mockTaskList.add(mockImgBo);

        mockImgBo.setTaskId("3");
        mockImgBo.setUserId(123);
        mockImgBo.setRid(1);
        mockImgBo.setTaskDesc("task Desc3");
        mockImgBo.setTasktitle("task title3");
        mockImgBo.setTaskDueDate("12/07/2019");
        mockTaskList.add(mockImgBo);

        mockHashList.put("1", mockTaskList);
        return mockHashList;
    }

    public static HashMap<String, ArrayList<FilterBo>> getMockFilterList() {
        ArrayList<FilterBo> mockTaskList = new ArrayList<>();
        HashMap<String, ArrayList<FilterBo>> mockHashList = new HashMap<>();
        FilterBo mockfilterBo = new FilterBo();


        mockfilterBo.setFilterId(123);
        mockfilterBo.setFilterName("filter1");
        mockTaskList.add(mockfilterBo);


        mockfilterBo.setFilterId(123);
        mockfilterBo.setFilterName("filter1");
        mockTaskList.add(mockfilterBo);


        mockfilterBo.setFilterId(123);
        mockfilterBo.setFilterName("filter1");
        mockTaskList.add(mockfilterBo);

        mockHashList.put("Retailer", mockTaskList);
        return mockHashList;
    }


}
