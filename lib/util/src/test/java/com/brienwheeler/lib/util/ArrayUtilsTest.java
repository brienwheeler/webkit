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

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.test.UtilsTestBase;

public class ArrayUtilsTest extends UtilsTestBase<ArrayUtils>
{
    @Override
    protected Class<ArrayUtils> getUtilClass()
    {
        return ArrayUtils.class;
    }

    @Test
    public void testToStringNull()
    {
        Assert.assertEquals("", ArrayUtils.toString(null));
    }

    @Test
    public void testToString()
    {
        Assert.assertEquals("1,2", ArrayUtils.toString(new Integer[] { 1, 2 }));
    }
    
    @Test
    public void testToStringWithNull()
    {
        Assert.assertEquals("1,null,3", ArrayUtils.toString(new Integer[] { 1, null, 3 }));
    }
    
    @Test
    public void testTrimElementsNull()
    {
        Assert.assertNull(ArrayUtils.trimElements(null));
    }

    @Test
    public void testTrimElements()
    {
        String[] array = new String[] { " 1 ", " trimmed ", "array" };
        array = ArrayUtils.trimElements(array);
        Assert.assertNull(ArrayUtils.trimElements(null));
        Assert.assertEquals(3, array.length);
        Assert.assertEquals("1", array[0]);
        Assert.assertEquals("trimmed", array[1]);
        Assert.assertEquals("array", array[2]);
    }
    
    @Test
    public void testContainsNullNull()
    {
    	Object[] array = new Object[2];
    	array[0] = new String();
    	Assert.assertTrue(ArrayUtils.contains(array, null));
    }

    @Test
    public void testContainsContains()
    {
    	Object[] array = new Object[2];
    	array[0] = new String();
    	array[1] = new Long(1);
    	Assert.assertTrue(ArrayUtils.contains(array, new Long(1)));
    }

    @Test
    public void testContainsNotContains()
    {
    	Object[] array = new Long[2];
    	array[0] = new Long(0);
    	array[1] = new Long(1);
    	Assert.assertEquals(false, ArrayUtils.contains(array, new Long[2]));
    }
}
