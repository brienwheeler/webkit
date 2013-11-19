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

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.test.UtilsTestBase;

public class CollectionUtilsTest extends UtilsTestBase<CollectionUtils>
{
    @Override
    protected Class<CollectionUtils> getUtilClass()
    {
        return CollectionUtils.class;
    }

    @Test
    public void testSingleObjectNull()
    {
        Assert.assertEquals(null, CollectionUtils.singleObject(null));
    }

    @Test
    public void testSingleObjectEmpty()
    {
        Assert.assertEquals(null, CollectionUtils.singleObject(new ArrayList<Integer>()));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSingleObjectMultiple()
    {
    	ArrayList<Integer> collection = new ArrayList<Integer>();
    	collection.add(1);
    	collection.add(2);
        CollectionUtils.singleObject(collection);
    }

    @Test
    public void testSingleObjectSingle()
    {
    	ArrayList<Integer> collection = new ArrayList<Integer>();
    	collection.add(1);
        Assert.assertEquals(1, CollectionUtils.singleObject(collection).intValue());
    }
    
    @Test
    public void testAreEqualsNullNull()
    {
    	Assert.assertTrue(CollectionUtils.areEqual(null, null));
    }

    @Test
    public void testAreEqualsNullNotNull()
    {
    	ArrayList<Integer> collection1 = new ArrayList<Integer>();
    	Assert.assertFalse(CollectionUtils.areEqual(null, collection1));
    }

    @Test
    public void testAreEqualsNotNullNull()
    {
    	ArrayList<Integer> collection1 = new ArrayList<Integer>();
    	Assert.assertFalse(CollectionUtils.areEqual(collection1, null));
    }

    @Test
    public void testAreEqualsDifferentSize()
    {
    	ArrayList<Integer> collection1 = new ArrayList<Integer>();
    	ArrayList<Integer> collection2 = new ArrayList<Integer>();
    	collection1.add(1);
    	Assert.assertFalse(CollectionUtils.areEqual(collection1, collection2));
    }

    @Test
    public void testAreEqualsEquals()
    {
    	ArrayList<Integer> collection1 = new ArrayList<Integer>();
    	ArrayList<Integer> collection2 = new ArrayList<Integer>();
    	collection1.add(1);
    	collection2.add(1);
    	Assert.assertTrue(CollectionUtils.areEqual(collection1, collection2));
    }

    @Test
    public void testAreEqualsNotEquals()
    {
    	ArrayList<Integer> collection1 = new ArrayList<Integer>();
    	ArrayList<Integer> collection2 = new ArrayList<Integer>();
    	collection1.add(1);
    	collection2.add(2);
    	Assert.assertFalse(CollectionUtils.areEqual(collection1, collection2));
    }

}
