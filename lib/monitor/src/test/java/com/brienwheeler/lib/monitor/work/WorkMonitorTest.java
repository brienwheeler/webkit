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

import com.brienwheeler.lib.monitor.work.WorkMonitor;
import com.brienwheeler.lib.util.ValidationException;

public class WorkMonitorTest
{
	public static final String TEST_NAME = "testName";
	
	@Test
	public void testConstruct()
	{
		WorkMonitor workMonitor = new WorkMonitor(TEST_NAME);
		Assert.assertEquals(TEST_NAME, workMonitor.getSourceName());
		WorkRecordCollection workRecordCollection = workMonitor.rollRecords();
		Assert.assertEquals(0, workRecordCollection.size());
	}
	
	@Test(expected = ValidationException.class)
	public void testConstructFailNullName()
	{
		new WorkMonitor(null);
	}
	
	@Test
	public void testRecordWorkOk()
	{
		WorkMonitor workMonitor = new WorkMonitor(TEST_NAME);
		workMonitor.recordWorkOk("workName", 100);
		workMonitor.recordWorkOk("workName", 150);
		WorkRecordCollection workRecordCollection = workMonitor.rollRecords();
		Assert.assertEquals(1, workRecordCollection.size());
	}

	@Test
	public void testRecordWorkError()
	{
		WorkMonitor workMonitor = new WorkMonitor(TEST_NAME);
		workMonitor.recordWorkError("workName", 100);
		workMonitor.recordWorkError("workName", 150);
		WorkRecordCollection workRecordCollection = workMonitor.rollRecords();
		Assert.assertEquals(1, workRecordCollection.size());
	}
}
