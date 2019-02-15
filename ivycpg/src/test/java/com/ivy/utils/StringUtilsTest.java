package com.ivy.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({StringUtils.class})
public class StringUtilsTest {


    @Test
    public void testRemoveQuotes(){
        Assert.assertEquals("a b c d e f",StringUtils.removeQuotes("a'b'c'd'e'f"));
    }

    @Test
    public void testIsEmptyStringEmpty(){
        Assert.assertTrue(StringUtils.isEmptyString(""));
    }

    @Test
    public void testIsEmptyStringNull(){
        Assert.assertTrue(StringUtils.isEmptyString(null));
    }

    @Test
    public void testIsEmptyStringNullString(){
        Assert.assertTrue(StringUtils.isEmptyString("null"));
    }

    @Test
    public void testIsNotEmptyString(){
        Assert.assertFalse(StringUtils.isEmptyString("Hello"));
    }
}
