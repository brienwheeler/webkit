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

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.monitor.intervene.IInterventionListener;
import com.brienwheeler.lib.monitor.intervene.mocks.MockInterventionListener;
import com.brienwheeler.lib.monitor.intervene.mocks.MockInterventionListener2;
import com.brienwheeler.lib.monitor.telemetry.ITelemetryPublishService;
import com.brienwheeler.lib.monitor.telemetry.mocks.MockTelemetryPublishService;
import com.brienwheeler.lib.monitor.telemetry.mocks.MockTelemetryPublishService2;
import com.brienwheeler.lib.monitor.work.IWorkPublishService;
import com.brienwheeler.lib.monitor.work.WorkRecord;
import com.brienwheeler.lib.monitor.work.WorkRecordCollection;
import com.brienwheeler.lib.monitor.work.mocks.MockWorkPublishService;
import com.brienwheeler.lib.svc.ServiceOperationException;
import com.brienwheeler.lib.svc.ServiceState;
import com.brienwheeler.lib.svc.ServiceStateException;
import com.brienwheeler.lib.svc.impl.mocks.AnnotatedSpringStoppableService;
import com.brienwheeler.lib.svc.impl.mocks.NullStartableService;
import com.brienwheeler.lib.svc.impl.mocks.NullStoppableService;
import com.brienwheeler.lib.svc.impl.mocks.NullSubServices;
import com.brienwheeler.lib.svc.impl.mocks.PrivateSubServices;
import com.brienwheeler.lib.svc.impl.mocks.PublicSubServiceArray;
import com.brienwheeler.lib.svc.impl.mocks.PublicSubServices;
import com.brienwheeler.lib.svc.impl.stepper.SteppableService;
import com.brienwheeler.lib.svc.impl.stepper.SteppableServiceThread;
import com.brienwheeler.lib.util.CollectionUtils;

import static com.brienwheeler.lib.svc.impl.ServiceBaseTestUtil.*;

public class StartableServiceBaseTest
{
	@Test
	public void testConstruct()
	{
		StartableServiceBase svc = new SteppableService();
		verifyState(svc, ServiceState.STOPPED);
		verifyRefCount(svc, 0);
	}
	
	@Test
	public void testNoSubsStartOk()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper = new SteppableServiceThread(service, SteppableServiceThread.START);

		stepper.start();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STARTING);
		verifyRefCount(service, 0);
		
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);
		
		stepper.releaseAndJoin();
	}

	@Test
	public void testNoSubsStartFailIE()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper = new SteppableServiceThread(service, SteppableServiceThread.START);
		stepper.setFailOnStart(new InterruptedException());

		stepper.start();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STARTING);
		verifyRefCount(service, 0);
		
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		Assert.assertEquals(ServiceOperationException.class, stepper.getTestThrowable().getClass());
		Assert.assertEquals(InterruptedException.class, stepper.getTestThrowable().getCause().getClass());

		stepper.releaseAndJoin();
	}
	
	@Test
	public void testNoSubsStartFailRE()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper = new SteppableServiceThread(service, SteppableServiceThread.START);
		stepper.setFailOnStart(new RuntimeException());

		stepper.start();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STARTING);
		verifyRefCount(service, 0);
		
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		Assert.assertEquals(RuntimeException.class, stepper.getTestThrowable().getClass());

		stepper.releaseAndJoin();
	}
	
	@Test
	public void testNoSubsStartFailError()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper = new SteppableServiceThread(service, SteppableServiceThread.START);
		stepper.setFailOnStart(new Error());
		
		stepper.start();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STARTING);
		verifyRefCount(service, 0);
		
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		Assert.assertEquals(Error.class, stepper.getTestThrowable().getClass());

		stepper.releaseAndJoin();
	}
	
	@Test
	public void testTwoThreadsOK()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service, SteppableServiceThread.START);
		SteppableServiceThread stepper2 = new SteppableServiceThread(service, SteppableServiceThread.START);
		
		stepper1.start();
		stepper2.start();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);

		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.STARTING);
		verifyRefCount(service, 0);
		
		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);

		stepper2.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 2);

		stepper1.releaseAndJoin();
		stepper2.releaseAndJoin();
	}
	
	@Test
	public void testTwoThreadsOKWait()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service, SteppableServiceThread.START);
		SteppableServiceThread stepper2 = new SteppableServiceThread(service, SteppableServiceThread.START);

		stepper1.start();
		stepper2.start();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);

		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.STARTING);
		verifyRefCount(service, 0);

		// this thread will block until thread1 finishes onStart
		stepper2.releaseAndWaitDone(); // this step gets it past the state check into waitForStateChange
		stepper2.release(); // this release lets it go into the wait so thread 1 can re-acquire

		stepper1.releaseAndWaitDone();
		stepper2.waitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 2);

		stepper1.releaseAndJoin();
		stepper2.releaseAndJoin();
	}
	
	@Test
	public void testTwoThreadsFailIE() throws InterruptedException
	{
		// this test has a second thread waiting for the start operation on the first
		// thread and the first thread experiences a failure to get to RUNNING
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service, SteppableServiceThread.START);
		stepper1.setFailOnStart(new InterruptedException());
		SteppableServiceThread stepper2 = new SteppableServiceThread(service, SteppableServiceThread.START);
		
		stepper1.start();
		stepper2.start();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);

		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.STARTING);
		verifyRefCount(service, 0);

		// this thread will block until thread1 finishes onStart
		stepper2.releaseAndWaitDone(); // this step gets it past the state check into waitForStateChange
		stepper2.release(); // this release lets it go into the wait so thread 1 can re-acquire

		stepper1.releaseAndWaitDone();
		stepper2.waitDone();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		Assert.assertEquals(ServiceOperationException.class, stepper1.getTestThrowable().getClass());
		Assert.assertEquals(ServiceOperationException.class, stepper2.getTestThrowable().getClass());

		stepper1.releaseAndJoin();
		stepper2.releaseAndJoin();
	}

	@Test
	public void testTwoThreadsFailInterruptedWait()
	{
		// This is different than the above test where both threads (the thread that is trying to
		// start the service and the thread that is waiting for the start to complete) fail because
		// the service onStart() throws InterruptedException.
		// In this case the service start succeeds for one thread but the second thread is
		// explicitly interrupted while waiting for the service start to complete
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service, SteppableServiceThread.START);
		SteppableServiceThread stepper2 = new SteppableServiceThread(service, SteppableServiceThread.START);

		stepper1.start();
		stepper2.start();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);

		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.STARTING);
		verifyRefCount(service, 0);

		// this thread will block until thread1 finishes onStart
		stepper2.releaseAndWaitDone(); // this step gets it past the state check into waitForStateChange
		stepper2.interruptAndWait(); // this thread will be interrupted instead of blocking
		stepper2.releaseAndWaitDone();
		
		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);
		Assert.assertEquals(ServiceOperationException.class, stepper2.getTestThrowable().getClass());

		stepper1.releaseAndJoin();
		stepper2.releaseAndJoin();
	}

	@Test
	public void testStartSubsOK()
	{
		SteppableService child1 = new SteppableService();
		SteppableService child2 = new SteppableService();
		SteppableService service = new SteppableService(child1, child2);
		SteppableServiceThread stepper1 = new SteppableServiceThread(service, SteppableServiceThread.START);

		stepper1.start();
		stepper1.releaseAndWaitDone(); // parent --> STARTING
		stepper1.releaseAndWaitDone(); // child1 --> STARTING
		stepper1.releaseAndWaitDone(); // child1 --> RUNNING
		stepper1.releaseAndWaitDone(); // child2 --> STARTING
		stepper1.releaseAndWaitDone(); // child2 --> RUNNING
		stepper1.releaseAndWaitDone(); // parent --> RUNNING
		
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);
		verifySubServiceCount(service, 2);
		verifyState(child1, ServiceState.RUNNING);
		verifyRefCount(child1, 1);
		verifyState(child2, ServiceState.RUNNING);
		verifyRefCount(child2, 1);

		stepper1.releaseAndJoin();
	}
	
	@Test
	public void testStartSubsFail()
	{
		SteppableService child1 = new SteppableService();
		SteppableService child2 = new SteppableService();
		SteppableService service = new SteppableService(child1, child2);
		SteppableServiceThread stepper1 = new SteppableServiceThread(service, SteppableServiceThread.START);

		stepper1.start();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		verifySubServiceCount(service, 0);

		stepper1.releaseAndWaitDone(); // parent --> STARTING
		verifyState(service, ServiceState.STARTING);
		verifyRefCount(service, 0);
		verifySubServiceCount(service, 0);

		stepper1.releaseAndWaitDone(); // child1 --> STARTING
		stepper1.releaseAndWaitDone(); // child1 --> RUNNING
		verifyState(service, ServiceState.STARTING);
		verifyRefCount(service, 0);
		verifySubServiceCount(service, 1);

		stepper1.setFailOnStart(new Error());
		stepper1.releaseAndWaitDone(); // child2 --> STARTING
		stepper1.releaseAndWaitDone(); // child2 --> Error, child2 --> STOPPED, parent --> STOPPING
		stepper1.releaseAndWaitDone(); // child1 --> STOPPING
		stepper1.releaseAndWaitDone(); // child1 --> STOPPED, parent --> STOPPED
		
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		verifySubServiceCount(service, 0);

		stepper1.releaseAndJoin();
	}

	@Test
	public void testNoAutoStartSubs() throws Exception
	{
		PublicSubServices service = new PublicSubServices();
		
		service.setAutoStartSubServices(false);
		service.start();
		verifyState(service, ServiceState.RUNNING);
		verifySubServiceCount(service, 0);
		verifyState(service.getChild1(), ServiceState.STOPPED);
		verifyState(service.getChild2(), ServiceState.STOPPED);
	}
	
	@Test
	public void testAutoStartPublicSubs() throws Exception
	{
		PublicSubServices service = new PublicSubServices();
		
		service.start();
		verifyState(service, ServiceState.RUNNING);
		verifySubServiceCount(service, 2);
		verifyState(service.getChild1(), ServiceState.RUNNING);
		verifyState(service.getChild2(), ServiceState.RUNNING);
	}
	
	@Test
	public void testAutoStartPrivateSubs()
	{
		PrivateSubServices service = new PrivateSubServices();
		
		service.start();
		verifyState(service, ServiceState.RUNNING);
		verifySubServiceCount(service, 4);
		verifyState(service.getChild1(), ServiceState.RUNNING);
		verifyState(service.getChild2(), ServiceState.RUNNING);
		verifyState(service.getChild3(), ServiceState.RUNNING);
		verifyState(service.getChild4(), ServiceState.RUNNING);
	}
	
	@Test
	public void testAutoStartNullSubs()
	{
		NullSubServices service = new NullSubServices();
		
		service.start();
		verifyState(service, ServiceState.RUNNING);
		verifySubServiceCount(service, 0);
		Assert.assertNull(service.getChild1());
		Assert.assertNull(service.getChild2());
	}

	@Test
	public void testAutoStartPublicArray() throws Exception
	{
		PublicSubServiceArray service = new PublicSubServiceArray();
		
		service.start();
		verifyState(service, ServiceState.RUNNING);
		verifySubServiceCount(service, 0);
	}
	
	@Test(expected=ServiceStateException.class)
	public void testWorkFailStopped()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service, SteppableServiceThread.START);

		stepper1.start();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		
		service.testWork();
	}

	@Test(expected=ServiceStateException.class)
	public void testWorkFailStarting()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service, SteppableServiceThread.START);

		stepper1.start();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		
		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.STARTING);
		verifyRefCount(service, 0);
		
		service.testWork();
	}

	@Test
	public void testWorkOKRunning()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service, SteppableServiceThread.START);

		stepper1.start();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		
		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.STARTING);
		verifyRefCount(service, 0);
		
		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);

		service.setStepping(false);
		service.testWork();
		service.setStepping(false);
		
		stepper1.releaseAndJoin();
	}

	@Test(expected = ServiceStateException.class)
	public void testWorkFailInterrupted() throws InterruptedException
	{
		try {
			AnnotatedSpringStoppableService service = new AnnotatedSpringStoppableService();

			service.start();
			service.testMethodWorkNameInterruptedException(0L);
		}
		finally {
			Assert.assertTrue(Thread.interrupted());
		}
	}

	@Test
	public void testMonitoredWork()
	{
		AnnotatedSpringStoppableService service = new AnnotatedSpringStoppableService();
		service.start();

		service.testMethodWorkName(0L);
		WorkRecordCollection workRecordCollection = ServiceBaseTestUtil.getWorkMonitor(service).rollRecords();
		Assert.assertEquals(1, workRecordCollection.size());
		WorkRecord workRecord = workRecordCollection.getWorkRecord(AnnotatedSpringStoppableService.WORK_NAME);
		Assert.assertNotNull(workRecord);
		Assert.assertEquals(1, workRecord.getWorkOkCount());

		try {
			service.testMethodWorkNameRuntimeException(0L);
			Assert.fail();
		}
		catch (RuntimeException e) { }
		workRecordCollection = ServiceBaseTestUtil.getWorkMonitor(service).rollRecords();
		Assert.assertEquals(1, workRecordCollection.size());
		workRecord = workRecordCollection.getWorkRecord(AnnotatedSpringStoppableService.WORK_NAME);
		Assert.assertNotNull(workRecord);
		Assert.assertEquals(1, workRecord.getWorkErrorCount());

		try {
			service.testMethodWorkNameError(0L);
			Assert.fail();
		}
		catch (Error e) {}
		workRecordCollection = ServiceBaseTestUtil.getWorkMonitor(service).rollRecords();
		Assert.assertEquals(1, workRecordCollection.size());
		workRecord = workRecordCollection.getWorkRecord(AnnotatedSpringStoppableService.WORK_NAME);
		Assert.assertNotNull(workRecord);
		Assert.assertEquals(1, workRecord.getWorkErrorCount());
	}

	@Test
	public void testSetInterventionListeners()
	{
		NullStartableService service = new NullStartableService();
		
		ArrayList<IInterventionListener> interventionListeners = new ArrayList<IInterventionListener>();
		interventionListeners.add(new MockInterventionListener());
		interventionListeners.add(new MockInterventionListener2());
		service.setInterventionListeners(interventionListeners);
		
		Collection<IInterventionListener> serviceListeners = ServiceBaseTestUtil.getInterventionListeners(service);
		CollectionUtils.areEqual(interventionListeners, serviceListeners);
	}
	
	@Test
	public void testSetTelemetryPublishers()
	{
		NullStartableService service = new NullStartableService();
		
		ArrayList<ITelemetryPublishService> telemetryPublishers = new ArrayList<ITelemetryPublishService>();
		telemetryPublishers.add(new MockTelemetryPublishService());
		telemetryPublishers.add(new MockTelemetryPublishService2());
		service.setTelemetryPublishers(telemetryPublishers);
		
		Collection<ITelemetryPublishService> servicePublishers = ServiceBaseTestUtil.getTelemetryPublishers(service);
		CollectionUtils.areEqual(telemetryPublishers, servicePublishers);
	}
	
	@Test
	public void testRegisterOnWorkPublisherSetWhileRunning()
	{
		NullStoppableService service = new NullStoppableService();
		
		MockWorkPublishService workPublishService = new MockWorkPublishService();
		MockWorkPublishService workPublishService2 = new MockWorkPublishService();
		MockWorkPublishService workPublishService3 = new MockWorkPublishService();
		
		ArrayList<IWorkPublishService> collection = new ArrayList<IWorkPublishService>();
		collection.add(workPublishService);
		collection.add(workPublishService2);
		service.setWorkPublishers(collection);
		
		Assert.assertEquals(0, workPublishService.monitors.size());
		Assert.assertEquals(0, workPublishService2.monitors.size());
		Assert.assertEquals(0, workPublishService3.monitors.size());
		
		service.start();
		Assert.assertEquals(1, workPublishService.monitors.size());
		Assert.assertSame(service.workMonitor, workPublishService.monitors.iterator().next());
		Assert.assertEquals(1, workPublishService2.monitors.size());
		Assert.assertSame(service.workMonitor, workPublishService2.monitors.iterator().next());
		Assert.assertEquals(0, workPublishService3.monitors.size());
		
		collection.clear();
		collection.add(workPublishService2);
		collection.add(workPublishService3);
		service.setWorkPublishers(collection);
		
		Assert.assertEquals(0, workPublishService.monitors.size());
		Assert.assertEquals(1, workPublishService2.monitors.size());
		Assert.assertSame(service.workMonitor, workPublishService2.monitors.iterator().next());
		Assert.assertEquals(1, workPublishService3.monitors.size());
		Assert.assertSame(service.workMonitor, workPublishService3.monitors.iterator().next());
		
		service.stop(10L);
		Assert.assertEquals(0, workPublishService.monitors.size());
		Assert.assertEquals(0, workPublishService2.monitors.size());
		Assert.assertEquals(0, workPublishService3.monitors.size());
		}
}
