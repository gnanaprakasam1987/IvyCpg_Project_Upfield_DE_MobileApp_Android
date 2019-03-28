package com.ivy.sd.png.commons;

import com.ivy.sd.png.commons.SDUtil;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Calendar;

import static org.mockito.BDDMockito.given;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by abbas.a on 01/08/18.
 * On
 */

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({SDUtil.class})
public class SDUtilTest {

    @Test
    public void checkFormat(){
        String str = SDUtil.format(12.345,2,0);
        Assert.assertEquals("12.35", str);
    }

    @Test
    public void testConvertToDoubleNull(){
        Assert.assertEquals(0.0,SDUtil.convertToDouble(null),1e-15);
    }

    @Test
    public void testConvertToDouble(){
        Assert.assertEquals(15.60,SDUtil.convertToDouble("15.60"),1e-15);
    }

    @Test
    public void testConvertToDoubleStringValue(){
        Assert.assertEquals(0.0,SDUtil.convertToDouble("abcd"),1e-15);
    }

    @Test
    public void testConvertToFloatNull(){
        Assert.assertEquals(0.0f,SDUtil.convertToFloat(null),1e-15);
    }

    @Test
    public void testConvertToFloat(){
        Assert.assertEquals(15f,SDUtil.convertToFloat("15.00"),1e-15);
    }

    @Test
    public void testConvertToFloatStringValue(){
        Assert.assertEquals(0.0f,SDUtil.convertToFloat("abcd"),1e-15);
    }


    @Test
    public void testConvertToLongNull(){
        Assert.assertEquals(0.0,SDUtil.convertToLong(null),1e-15);
    }

    @Test
    public void testConvertToLongEmpty(){
        Assert.assertEquals(0,SDUtil.convertToLong(""),1e-15);
    }

    @Test
    public void testConvertToLong(){
        Assert.assertEquals(15,SDUtil.convertToLong("15"),1e-15);
    }

    @Test
    public void testConvertToLongStringValue(){
        Assert.assertEquals(-1,SDUtil.convertToLong("abcd"),1e-15);
    }


    @Test
    public void testConvertToIntegerNull(){
        Assert.assertEquals(0,SDUtil.convertToInt(null),1e-15);
    }

    @Test
    public void testConvertToInteger(){
        Assert.assertEquals(88,SDUtil.convertToInt("88"),1e-15);
    }

    @Test
    public void testConvertToIntegerStringValue(){
        Assert.assertEquals(0,SDUtil.convertToInt("abcd"),1e-15);
    }



}
