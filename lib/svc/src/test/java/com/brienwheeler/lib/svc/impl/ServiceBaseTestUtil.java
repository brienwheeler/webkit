/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2018 Brien L. Wheeler (brienwheeler@yahoo.com)
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

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.springframework.test.util.ReflectionTestUtils;

import com.brienwheeler.lib.monitor.intervene.IInterventionListener;
import com.brienwheeler.lib.monitor.telemetry.ITelemetryPublishService;
import com.brienwheeler.lib.monitor.work.IWorkPublishService;
import com.brienwheeler.lib.monitor.work.WorkMonitor;
import com.brienwheeler.lib.monitor.work.WorkRecordCollection;
import com.brienwheeler.lib.svc.IStartableService;
import com.brienwheeler.lib.svc.ServiceState;
import com.brienwheeler.lib.test.spring.aop.AopTestUtils;

public class ServiceBaseTestUtil
{
	@SuppressWarnings("unchecked")
	public static void verifyState(IStartableService service, ServiceState state)
	{
		AtomicReference<ServiceState> svcState = (AtomicReference<ServiceState>) ReflectionTestUtils.getField(service, "state");
		Assert.assertEquals(state, svcState.get());
	}
	
	public static void verifyRefCount(StartableServiceBase service, int refCount)
	{
		AtomicInteger svcRefCount = (AtomicInteger) ReflectionTestUtils.getField(service, "refCount");
		Assert.assertEquals(refCount, svcRefCount.get());
	}
	
	@SuppressWarnings("unchecked")
	public static void verifySubServiceCount(StartableServiceBase service, int subServiceCount)
	{
		ArrayList<IStartableService> svcRefCount = (ArrayList<IStartableService>) ReflectionTestUtils.getField(service, "subServices");
		Assert.assertEquals(subServiceCount, svcRefCount.size());
	}

	public static WorkMonitor getWorkMonitor(IStartableService service)
	{
		return (WorkMonitor) ReflectionTestUtils.getField(AopTestUtils.<IStartableService>getTarget(service), "workMonitor");
	}
	
	public static void clearWorkRecords(IStartableService service)
	{
		getWorkMonitor(service).rollRecords();
	}
	
	@SuppressWarnings("unchecked")
	public static void verifyWorkRecord(IStartableService service, String workName, int okCount, int errorCount)
	{
		WorkMonitor workMonitor = getWorkMonitor(service);
		AtomicReference<WorkRecordCollection> workRecordCollectionRef = (AtomicReference<WorkRecordCollection>) ReflectionTestUtils.getField(workMonitor,
				"workRecords");
		WorkRecordCollection workRecordCollection = workRecordCollectionRef.get();
		Assert.assertNotNull("no work record for " + workName,
				workRecordCollection.getWorkRecord(workName));
		Assert.assertEquals("work record " + workName + " expected OK=" + okCount + ", actual OK=" + workRecordCollection.getWorkRecord(workName).getWorkOkCount(),
				okCount, workRecordCollection.getWorkRecord(workName).getWorkOkCount());
		Assert.assertEquals("work record " + workName + " expected ERROR=" + errorCount + ", actual ERROR=" + workRecordCollection.getWorkRecord(workName).getWorkErrorCount(),
				errorCount, workRecordCollection.getWorkRecord(workName).getWorkErrorCount());
	}
	
	@SuppressWarnings("unchecked")
	public static CopyOnWriteArraySet<IInterventionListener> getInterventionListeners(StartableServiceBase service)
	{
		return (CopyOnWriteArraySet<IInterventionListener>) ReflectionTestUtils.getField(service, "interventionListeners");
	}

	@SuppressWarnings("unchecked")
	public static CopyOnWriteArraySet<ITelemetryPublishService> getTelemetryPublishers(StartableServiceBase service)
	{
		return (CopyOnWriteArraySet<ITelemetryPublishService>) ReflectionTestUtils.getField(service, "telemetryPublishers");
	}

	@SuppressWarnings("unchecked")
	public static CopyOnWriteArraySet<IWorkPublishService> getWorkPublishers(StartableServiceBase service)
	{
		return (CopyOnWriteArraySet<IWorkPublishService>) ReflectionTestUtils.getField(service, "workPublishers");
	}
	
	public static long getStopGracePeriod(StoppableServiceBase service)
	{
		return ((Long) ReflectionTestUtils.getField(service, "stopGracePeriod")).longValue();
	}
}
