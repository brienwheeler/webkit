/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Brien L. Wheeler (brienwheeler@yahoo.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.brienwheeler.lib.util;

import org.junit.Test;

import com.brienwheeler.lib.test.UtilsTestBase;

public class ValidationUtilsTest extends UtilsTestBase<ValidationUtils>
{
    @Override
    protected Class<ValidationUtils> getUtilClass()
    {
        return ValidationUtils.class;
    }

    @Test
    public void testAssertNotEmptyNotEmpty()
    {
        ValidationUtils.assertNotEmpty("not empty", "error");
    }

    @Test(expected = ValidationException.class)
    public void testAssertNotEmptyNull()
    {
        ValidationUtils.assertNotEmpty(null, "error");
    }

    @Test(expected = ValidationException.class)
    public void testAssertNotEmptyEmpty()
    {
        ValidationUtils.assertNotEmpty("", "error");
    }

    @Test(expected = ValidationException.class)
    public void testAssertNotEmptyWhitespace()
    {
        ValidationUtils.assertNotEmpty(" ", "error");
    }

    @Test
    public void testAssertNotNullNotNull()
    {
        ValidationUtils.assertNotNull(new Object(), "error");
    }

    @Test(expected = ValidationException.class)
    public void testAssertNotNullNull()
    {
        ValidationUtils.assertNotNull(null, "error");
    }

    @Test
    public void testAssertTrueTrue()
    {
        ValidationUtils.assertTrue(true, "error");
    }

    @Test(expected = ValidationException.class)
    public void testAssertTrueFalse()
    {
        ValidationUtils.assertTrue(false, "error");
    }

    @Test
    public void testAssertFalseFalse()
    {
        ValidationUtils.assertFalse(false, "error");
    }

    @Test(expected = ValidationException.class)
    public void testAssertFalseTrue()
    {
        ValidationUtils.assertFalse(true, "error");
    }

}
