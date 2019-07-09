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
        Assert.assertTrue(StringUtils.isNullOrEmpty(""));
    }

    @Test
    public void testIsEmptyStringNull(){
        Assert.assertTrue(StringUtils.isNullOrEmpty(null));
    }

    @Test
    public void testIsEmptyStringNullString(){
        Assert.assertTrue(StringUtils.isNullOrEmpty("null"));
    }

    @Test
    public void testIsNotEmptyString(){
        Assert.assertFalse(StringUtils.isNullOrEmpty("Hello"));
    }

    @Test
    public void testValidRegexPositive(){
        Assert.assertTrue(StringUtils.validRegex("^\\s*(?:\\+?(\\d{1,3}))?([-. (]*(\\d{3})[-. )]*)?((\\d{3})[-. ]*(\\d{2,4})(?:[-.x ]*(\\d+))?)\\s*$","+79261234567"));
    }

    @Test
    public void testValidRegexNegative(){
        Assert.assertFalse(StringUtils.validRegex("^\\s*(?:\\+?(\\d{1,3}))?([-. (]*(\\d{3})[-. )]*)?((\\d{3})[-. ]*(\\d{2,4})(?:[-.x ]*(\\d+))?)\\s*$","abcdef"));
    }

    @Test
    public void testValidRegexEmpty(){
        Assert.assertTrue(StringUtils.validRegex("","abcdef"));
    }

    @Test
    public void testIsValidEmailSuccess(){
        Assert.assertTrue(StringUtils.isValidEmail("abc@def.com"));
    }

    @Test
    public void testIsValidEmailFailure(){
        Assert.assertFalse(StringUtils.isValidEmail("abc@def.def..com"));
    }

    @Test
    public void testQT(){
        Assert.assertEquals("'abcdef'",StringUtils.getStringQueryParam("abcdef"));
    }
}
