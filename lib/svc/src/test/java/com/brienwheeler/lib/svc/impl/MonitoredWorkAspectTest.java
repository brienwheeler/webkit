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
package com.brienwheeler.lib.svc.impl;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.monitor.work.WorkRecord;
import com.brienwheeler.lib.monitor.work.WorkRecordCollection;
import com.brienwheeler.lib.svc.ServiceStateException;
import com.brienwheeler.lib.svc.impl.mocks.AnnotatedSpringStoppableService;

public class MonitoredWorkAspectTest
{
	@Test
	public void testWorkMethodName()
	{
		AnnotatedSpringStoppableService service = new AnnotatedSpringStoppableService();
		service.start();
		service.testMethodWorkMethodName(0);
		
		WorkRecordCollection workRecordCollection = ServiceBaseTestUtil.getWorkMonitor(service).rollRecords();
		Assert.assertEquals(1, workRecordCollection.size());
		WorkRecord workRecord = workRecordCollection.getWorkRecord("testMethodWorkMethodName");
		Assert.assertNotNull(workRecord);
		Assert.assertEquals(0, workRecord.getWorkErrorCount());
		Assert.assertEquals(1, workRecord.getWorkOkCount());
	}

	@Test
	public void testWorkName()
	{
		AnnotatedSpringStoppableService service = new AnnotatedSpringStoppableService();
		service.start();
		service.testMethodWorkName(0);
		
		WorkRecordCollection workRecordCollection = ServiceBaseTestUtil.getWorkMonitor(service).rollRecords();
		Assert.assertEquals(1, workRecordCollection.size());
		WorkRecord workRecord = workRecordCollection.getWorkRecord(AnnotatedSpringStoppableService.WORK_NAME);
		Assert.assertNotNull(workRecord);
		Assert.assertEquals(0, workRecord.getWorkErrorCount());
		Assert.assertEquals(1, workRecord.getWorkOkCount());
	}

	@Test
	public void testWorkNameInterruptedException()
	{
		AnnotatedSpringStoppableService service = new AnnotatedSpringStoppableService();
		service.start();
		try {
			service.testMethodWorkNameInterruptedException(0);
			Assert.fail();
		}
		catch (ServiceStateException e) {
			// expected -- StartableServiceBase turns InterruptedException into ServiceStateException
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			Assert.fail();
		}
		
		Assert.assertTrue(Thread.interrupted());
		WorkRecordCollection workRecordCollection = ServiceBaseTestUtil.getWorkMonitor(service).rollRecords();
		Assert.assertEquals(1, workRecordCollection.size());
		WorkRecord workRecord = workRecordCollection.getWorkRecord(AnnotatedSpringStoppableService.WORK_NAME);
		Assert.assertNotNull(workRecord);
		Assert.assertEquals(1, workRecord.getWorkErrorCount());
		Assert.assertEquals(0, workRecord.getWorkOkCount());
	}

	@Test
	public void testWorkNameRuntimeException()
	{
		AnnotatedSpringStoppableService service = new AnnotatedSpringStoppableService();
		service.start();
		try {
			service.testMethodWorkNameRuntimeException(0);
			Assert.fail();
		}
		catch (RuntimeException e) {
			// expected
		}
		
		WorkRecordCollection workRecordCollection = ServiceBaseTestUtil.getWorkMonitor(service).rollRecords();
		Assert.assertEquals(1, workRecordCollection.size());
		WorkRecord workRecord = workRecordCollection.getWorkRecord(AnnotatedSpringStoppableService.WORK_NAME);
		Assert.assertNotNull(workRecord);
		Assert.assertEquals(1, workRecord.getWorkErrorCount());
		Assert.assertEquals(0, workRecord.getWorkOkCount());
	}

	@Test
	public void testWorkNameError()
	{
		AnnotatedSpringStoppableService service = new AnnotatedSpringStoppableService();
		service.start();
		try {
			service.testMethodWorkNameError(0);
			Assert.fail();
		}
		catch (Error e) {
			// expected
		}
		
		WorkRecordCollection workRecordCollection = ServiceBaseTestUtil.getWorkMonitor(service).rollRecords();
		Assert.assertEquals(1, workRecordCollection.size());
		WorkRecord workRecord = workRecordCollection.getWorkRecord(AnnotatedSpringStoppableService.WORK_NAME);
		Assert.assertNotNull(workRecord);
		Assert.assertEquals(1, workRecord.getWorkErrorCount());
		Assert.assertEquals(0, workRecord.getWorkOkCount());
	}

	@Test
	public void testWorkNameThrowable()
	{
		AnnotatedSpringStoppableService service = new AnnotatedSpringStoppableService();
		service.start();
		try {
			service.testMethodWorkNameThrowable(0);
			Assert.fail();
		}
		catch (RuntimeException e) {
			// expected
			Assert.assertEquals(Throwable.class, e.getCause().getClass());
		}
		catch (Throwable e) {
			Assert.fail(); // aspect turns this into RuntimeException
		}
		
		WorkRecordCollection workRecordCollection = ServiceBaseTestUtil.getWorkMonitor(service).rollRecords();
		Assert.assertEquals(1, workRecordCollection.size());
		WorkRecord workRecord = workRecordCollection.getWorkRecord(AnnotatedSpringStoppableService.WORK_NAME);
		Assert.assertNotNull(workRecord);
		Assert.assertEquals(1, workRecord.getWorkErrorCount());
		Assert.assertEquals(0, workRecord.getWorkOkCount());
	}
}
