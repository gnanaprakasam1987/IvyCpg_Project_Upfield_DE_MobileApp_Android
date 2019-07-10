package com.ivy.ui.Announcement;

import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.ui.announcement.model.AnnouncementBo;

import java.util.ArrayList;

public class AnnouncementTestDataFactory {

    public static RetailerMasterBO retailerMasterBO = new RetailerMasterBO("1", "abcd");

    public static UserMasterBO userMasterBO = new UserMasterBO(2, "user1");

    public static ArrayList<AnnouncementBo> getMockList() {
        ArrayList<AnnouncementBo> mockArrayList = new ArrayList<>();
        AnnouncementBo mockBo = new AnnouncementBo();
        mockBo.setDescription("Description1");
        mockBo.setType("Type01");
        mockBo.setDate("26/05/2019");
        mockArrayList.add(mockBo);

        mockBo.setDescription("Description2");
        mockBo.setType("Type02");
        mockBo.setDate("26/04/2019");
        mockArrayList.add(mockBo);

        mockBo.setDescription("Description3");
        mockBo.setType("Type03");
        mockBo.setDate("26/85/2019");
        mockArrayList.add(mockBo);

        return mockArrayList;

    }


    public static ArrayList<AnnouncementBo> getRetMockList() {
        ArrayList<AnnouncementBo> mockArrayList = new ArrayList<>();
        AnnouncementBo mockBo = new AnnouncementBo();
        mockBo.setDescription("Description11");
        mockBo.setType("Type011");
        mockBo.setDate("26/05/2019");
        mockArrayList.add(mockBo);

        mockBo.setDescription("Description22");
        mockBo.setType("Type022");
        mockBo.setDate("26/04/2019");
        mockArrayList.add(mockBo);

        mockBo.setDescription("Description33");
        mockBo.setType("Type033");
        mockBo.setDate("26/85/2019");
        mockArrayList.add(mockBo);

        return mockArrayList;

    }
}
