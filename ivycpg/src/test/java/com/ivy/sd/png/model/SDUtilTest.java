package com.ivy.sd.png.model;

import com.ivy.sd.png.commons.SDUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by abbas.a on 01/08/18.
 * On
 */

public class SDUtilTest {

    @Test
    public void checkFormat(){
        String str = SDUtil.format(12.3449,2,0);
        Assert.assertTrue(str.equals("12.34"));
    }
}
