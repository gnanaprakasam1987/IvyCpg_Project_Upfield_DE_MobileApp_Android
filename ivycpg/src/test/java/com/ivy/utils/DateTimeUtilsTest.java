package com.ivy.utils;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Calendar;

import static org.mockito.BDDMockito.given;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DateTimeUtils.class})
public class DateTimeUtilsTest {

    @Test
    public void testGetTodaySunday(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 10);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("Sunday",DateTimeUtils.today());
    }

    @Test
    public void testGetTodayMonday(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 11);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("Monday",DateTimeUtils.today());
    }

    @Test
    public void testGetTodayTuesday(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 12);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("Tuesday",DateTimeUtils.today());
    }

    @Test
    public void testGetTodayWednesday(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 13);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("Wednesday",DateTimeUtils.today());
    }


    @Test
    public void testGetTodayThursday(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 14);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("Thursday",DateTimeUtils.today());
    }

    @Test
    public void testGetTodayFriday(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 15);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("Friday",DateTimeUtils.today());
    }

    @Test
    public void testGetTodaySaturday(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 16);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("Saturday",DateTimeUtils.today());
    }

    @Test
    public void testCompareGreaterDate(){
        Assert.assertEquals(-1,DateTimeUtils.compareDate("14, Feb 2019","24, Feb 2019","dd, MMM yyyy"));
    }

    @Test
    public void testCompareSmallerDate(){
        Assert.assertEquals(1,DateTimeUtils.compareDate("14, Feb 2019","04, Feb 2019","dd, MMM yyyy"));
    }

    @Test
    public void testCompareEqualDate(){
        Assert.assertEquals(0,DateTimeUtils.compareDate("14, Feb 2019","14, Feb 2019","dd, MMM yyyy"));
    }

    @Test
    public void testCompareInvalidFormat(){
        Assert.assertEquals(0,DateTimeUtils.compareDate("14, Feb 2019","14, Feb 2019","dd, MM yyyy"));
    }


    @Test
    public void testNowTime(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 14,15,40,33);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("15:40:33",DateTimeUtils.now(DateTimeUtils.TIME));
    }

    @Test
    public void testNowDate(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 14,15,40,33);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("02/14/2019",DateTimeUtils.now(DateTimeUtils.DATE));
    }

    @Test
    public void testNowDateTime(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 14,15,40,33);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("02/14/2019 15:40:33",DateTimeUtils.now(DateTimeUtils.DATE_TIME));
    }

    @Test
    public void testNowDateTimeNew(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 14,15,40,33);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("2019/02/14 15:40:33",DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
    }


    @Test
    public void testNowGlobal(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 14,15,40,33);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("2019/02/14",DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
    }

    @Test
    public void testNowDateTimeMilliSecs(){

        Calendar valentinesDay = Calendar.getInstance();
        //valentinesDay.set(2019, Calendar.FEBRUARY, 14,15,40,33);
        valentinesDay.setTimeInMillis(1550226097);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("01191970040706097",DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));
    }

    @Test
    public void testNowGMT(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 14,15,40,33);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("2019/02/14 10:10:33",DateTimeUtils.now(DateTimeUtils.GMT_DATE_TIME));
    }

    @Test
    public void testNowGlobalPlain(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 14,15,40,33);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("20190214",DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL_PLAIN));
    }

    @Test
    public void testNowGlobalHyphen(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 14,15,40,33);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("2019-02-14",DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL_HYPHEN));
    }


    @Test
    public void testNowDOBPlain(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 14,15,40,33);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("02142019",DateTimeUtils.now(DateTimeUtils.DATE_DOB_FORMAT_PLAIN));
    }

    @Test
    public void testNowDefault(){

        Calendar valentinesDay = Calendar.getInstance();
        valentinesDay.set(2019, Calendar.FEBRUARY, 14,15,40,33);
        mockStatic(Calendar.class);

        given(Calendar.getInstance()).willReturn(valentinesDay);

        Assert.assertEquals("02142019154033",DateTimeUtils.now(-1));
    }
}
