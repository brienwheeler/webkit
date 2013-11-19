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
package com.brienwheeler.lib.monitor.work;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.util.ValidationException;

public class WorkRecordCollectionTest
{
	public static final String TEST_NAME = "testName";
	
	@Test
	public void testConstruct()
	{
		long now = System.currentTimeMillis();
		WorkRecordCollection workRecordCollection = new WorkRecordCollection(TEST_NAME, now);
		Assert.assertEquals(now, workRecordCollection.getStartTime());
	}
	
	@Test(expected = ValidationException.class)
	public void testConstructFailNullName()
	{
		new WorkRecordCollection(null, System.currentTimeMillis());
	}
	
	@Test
	public void testRecordWorkOk()
	{
		WorkRecordCollection workRecordCollection = new WorkRecordCollection(TEST_NAME,
				System.currentTimeMillis());
		workRecordCollection.recordWorkOk("workName", 100);
		workRecordCollection.recordWorkOk("workName2", 200);
		Assert.assertEquals(2, workRecordCollection.size());
	}
	
	@Test
	public void testRecordWorkError()
	{
		WorkRecordCollection workRecordCollection = new WorkRecordCollection(TEST_NAME,
				System.currentTimeMillis());
		workRecordCollection.recordWorkError("workName", 100);
		workRecordCollection.recordWorkError("workName2", 200);
		Assert.assertEquals(2, workRecordCollection.size());
	}
	
	@Test
	public void testSetEndTime()
	{
		WorkRecordCollection workRecordCollection = new WorkRecordCollection(TEST_NAME,
				System.currentTimeMillis());
		long now = System.currentTimeMillis();
		workRecordCollection.setEndTime(now);
		Assert.assertEquals(now, workRecordCollection.getEndTime());
	}
	
	@Test
	public void testGetWorkRecord()
	{
		WorkRecordCollection workRecordCollection = new WorkRecordCollection(TEST_NAME,
				System.currentTimeMillis());
		workRecordCollection.recordWorkOk("workName", 100);
		workRecordCollection.recordWorkOk("workName2", 200);
		workRecordCollection.recordWorkError("workName", 300);
		workRecordCollection.recordWorkError("workName2", 400);
		
		WorkRecord workRecord = workRecordCollection.getWorkRecord("workName");
		Assert.assertNotNull(workRecord);
		Assert.assertEquals(WorkRecord.class, workRecord.getClass());
		workRecord = workRecordCollection.getWorkRecord("workName2");
		Assert.assertNotNull(workRecord);
		Assert.assertEquals(WorkRecord.class, workRecord.getClass());
		workRecord = workRecordCollection.getWorkRecord("workName3");
		Assert.assertNull(workRecord);
	}
}
