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

import com.brienwheeler.lib.svc.ServiceOperationException;
import com.brienwheeler.lib.svc.ServiceState;
import com.brienwheeler.lib.svc.impl.stepper.SteppableService;
import com.brienwheeler.lib.svc.impl.stepper.SteppableServiceThread;

import static com.brienwheeler.lib.svc.impl.ServiceBaseTestUtil.*;

public class StoppableServiceBaseTest
{
	@Test
	public void testConstruct()
	{
		SteppableService svc = new SteppableService();
		verifyState(svc, ServiceState.STOPPED);
		verifyRefCount(svc, 0);
	}
	
	@Test
	public void testStopAlreadyStopped()
	{
		SteppableService svc = new SteppableService();
		verifyState(svc, ServiceState.STOPPED);
		verifyRefCount(svc, 0);
		
		svc.setStepping(false);
		svc.stopImmediate();
		verifyState(svc, ServiceState.STOPPED);
		verifyRefCount(svc, 0);
	}

	@Test
	public void testNoSubsStopOK()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper = new SteppableServiceThread(service);
		
		stepper.start();
		stepper.releaseAndWaitDone();
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);

		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPING);
		verifyRefCount(service, 0);

		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);

		stepper.releaseAndJoin();
	}

	@Test
	public void testNoSubsStopFailIE()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper = new SteppableServiceThread(service);
		stepper.setFailOnStop(new InterruptedException());

		stepper.start();
		stepper.releaseAndWaitDone();
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);

		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPING);
		verifyRefCount(service, 0);

		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STOP_FAILED);
		verifyRefCount(service, 0);
		Assert.assertEquals(ServiceOperationException.class, stepper.getTestThrowable().getClass());
		Assert.assertEquals(InterruptedException.class, stepper.getTestThrowable().getCause().getClass());

		stepper.releaseAndJoin();
	}
	
	@Test
	public void testNoSubsStopFailRE()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper = new SteppableServiceThread(service);
		stepper.setFailOnStop(new RuntimeException());

		stepper.start();
		stepper.releaseAndWaitDone();
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);

		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPING);
		verifyRefCount(service, 0);

		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STOP_FAILED);
		verifyRefCount(service, 0);
		Assert.assertEquals(RuntimeException.class, stepper.getTestThrowable().getClass());

		stepper.releaseAndJoin();
	}
	
	@Test
	public void testNoSubsStopFailError()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper = new SteppableServiceThread(service);
		stepper.setFailOnStop(new Error());

		stepper.start();
		stepper.releaseAndWaitDone();
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);

		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPING);
		verifyRefCount(service, 0);

		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STOP_FAILED);
		verifyRefCount(service, 0);
		Assert.assertEquals(Error.class, stepper.getTestThrowable().getClass());

		stepper.releaseAndJoin();
	}
	
	@Test
	public void testStopFailedStartFail()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper = new SteppableServiceThread(service);
		stepper.setFailOnStop(new InterruptedException());

		stepper.start();
		stepper.releaseAndWaitDone();
		stepper.releaseAndWaitDone();
		stepper.releaseAndWaitDone();
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STOP_FAILED);
		verifyRefCount(service, 0);

		stepper.releaseAndJoin();
	
		service.setStepping(false);
		try
		{
			service.start();
			Assert.fail();
		}
		catch (ServiceOperationException e)
		{}
	}

	@Test
	public void testStopFailedStopFail()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper = new SteppableServiceThread(service);
		stepper.setFailOnStop(new InterruptedException());

		stepper.start();
		stepper.releaseAndWaitDone();
		stepper.releaseAndWaitDone();
		stepper.releaseAndWaitDone();
		stepper.releaseAndWaitDone();
		verifyState(service, ServiceState.STOP_FAILED);
		verifyRefCount(service, 0);

		stepper.releaseAndJoin();

		service.setStepping(false);
		try
		{
			service.stopImmediate();
			Assert.fail();
		}
		catch (ServiceOperationException e)
		{}
	}

	@Test
	public void testTwoThreadsStopOK()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service);
		SteppableServiceThread stepper2 = new SteppableServiceThread(service);

		stepper1.start();
		stepper2.start();
		stepper1.releaseAndWaitDone();
		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);

		stepper2.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 2);

		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);

		stepper2.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPING);
		verifyRefCount(service, 0);
		
		stepper2.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);

		stepper1.releaseAndJoin();
		stepper2.releaseAndJoin();
	}

	@Test
	public void testTwoThreadsStopWait() throws InterruptedException
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service);
		SteppableServiceThread stepper2 = new SteppableServiceThread(service, SteppableServiceThread.STOP);

		stepper1.start();
		stepper2.start();
		stepper1.releaseAndWaitDone();
		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);

		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPING);
		verifyRefCount(service, 0);

		// this thread will block until thread1 finishes onStop
		stepper2.releaseAndWaitDone(); // this step gets it past the state check into waitForStateChange
		stepper2.release(); // this release lets it go into the wait so thread 1 can re-acquire
		
		stepper1.releaseAndWaitDone();
		stepper2.waitDone();

		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		stepper1.releaseAndJoin();
		stepper2.releaseAndJoin();
	}

	@Test
	public void testTwoThreadsFailInterruptedWait()
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service);
		SteppableServiceThread stepper2 = new SteppableServiceThread(service, SteppableServiceThread.STOP);
	
		stepper1.start();
		stepper2.start();
		stepper1.releaseAndWaitDone();
		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);

		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPING);
		verifyRefCount(service, 0);

		// this thread will block until thread1 finishes onStop
		stepper2.releaseAndWaitDone(); // this step gets it past the state check into waitForStateChange
		stepper2.interrupt(); // this thread will be interrupted instead of blocking
		
		stepper1.releaseAndWaitDone();
		stepper2.waitDone();
		
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		Assert.assertEquals(ServiceOperationException.class, stepper2.getTestThrowable().getClass());

		stepper1.releaseAndJoin();
		stepper2.releaseAndJoin();
	}

	@Test
	public void testStopSubsOK()
	{
		SteppableService child1 = new SteppableService();
		SteppableService child2 = new SteppableService();
		SteppableService service = new SteppableService(child1, child2);
		SteppableServiceThread stepper1 = new SteppableServiceThread(service);
		
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
		
		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPING);
		verifyRefCount(service, 0);
		verifySubServiceCount(service, 2);
		verifyState(child1, ServiceState.RUNNING);
		verifyRefCount(child1, 1);
		verifyState(child2, ServiceState.RUNNING);
		verifyRefCount(child2, 1);

		stepper1.releaseAndWaitDone(); // parent onStop
		stepper1.releaseAndWaitDone(); // child2 --> STOPPING (stopped in reverse order of start)
		stepper1.releaseAndWaitDone(); // child2 --> STOPPED
		stepper1.releaseAndWaitDone(); // child1 --> STOPPING
		stepper1.releaseAndWaitDone(); // child1 --> STOPPED, parent --> STOPPED
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);
		verifySubServiceCount(service, 0);
		verifyState(child1, ServiceState.STOPPED);
		verifyRefCount(child1, 0);
		verifyState(child2, ServiceState.STOPPED);
		verifyRefCount(child2, 0);
		
		stepper1.releaseAndJoin();
	}
	
	@Test
	public void testStopSubsFailRE()
	{
		SteppableService child1 = new SteppableService();
		SteppableService child2 = new SteppableService();
		SteppableService service = new SteppableService(child1, child2);
		SteppableServiceThread stepper1 = new SteppableServiceThread(service);
		
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
		
		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPING);
		verifyRefCount(service, 0);
		verifySubServiceCount(service, 2);
		verifyState(child1, ServiceState.RUNNING);
		verifyRefCount(child1, 1);
		verifyState(child2, ServiceState.RUNNING);
		verifyRefCount(child2, 1);

		stepper1.releaseAndWaitDone(); // parent onStop
		stepper1.releaseAndWaitDone(); // child2 --> STOPPING (stopped in reverse order of start)
		stepper1.releaseAndWaitDone(); // child2 --> STOPPED
		stepper1.releaseAndWaitDone(); // child1 --> STOPPING
		stepper1.setFailOnStop(new RuntimeException());
		stepper1.releaseAndWaitDone(); // child1 --> STOP_FAILED, parent --> STOP_FAILED
		verifyState(service, ServiceState.STOP_FAILED);
		verifyRefCount(service, 0);
		verifySubServiceCount(service, 0);
		verifyState(child1, ServiceState.STOP_FAILED);
		verifyRefCount(child1, 0);
		verifyState(child2, ServiceState.STOPPED);
		verifyRefCount(child2, 0);
		Assert.assertEquals(RuntimeException.class, stepper1.getTestThrowable().getClass());
		
		stepper1.releaseAndJoin();
	}

	@Test
	public void testStopSubsFailError()
	{
		SteppableService child1 = new SteppableService();
		SteppableService child2 = new SteppableService();
		SteppableService service = new SteppableService(child1, child2);
		SteppableServiceThread stepper1 = new SteppableServiceThread(service);
		
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
		
		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.STOPPING);
		verifyRefCount(service, 0);
		verifySubServiceCount(service, 2);
		verifyState(child1, ServiceState.RUNNING);
		verifyRefCount(child1, 1);
		verifyState(child2, ServiceState.RUNNING);
		verifyRefCount(child2, 1);

		stepper1.releaseAndWaitDone(); // parent onStop
		stepper1.releaseAndWaitDone(); // child2 --> STOPPING (stopped in reverse order of start)
		stepper1.setFailOnStop(new Error());
		stepper1.releaseAndWaitDone(); // child2 --> STOPPED
		stepper1.releaseAndWaitDone(); // child1 --> STOPPING
		stepper1.setFailOnStop(null);
		stepper1.releaseAndWaitDone(); // child1 --> STOP_FAILED, parent --> STOP_FAILED
		verifyState(service, ServiceState.STOP_FAILED);
		verifyRefCount(service, 0);
		verifySubServiceCount(service, 0);
		verifyState(child1, ServiceState.STOPPED);
		verifyRefCount(child1, 0);
		verifyState(child2, ServiceState.STOP_FAILED);
		verifyRefCount(child2, 0);
		Assert.assertEquals(Error.class, stepper1.getTestThrowable().getClass());
		
		stepper1.releaseAndJoin();
	}

	@Test
	public void testStopWhileStarting() throws InterruptedException
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service, SteppableServiceThread.START);
		SteppableServiceThread stepper2 = new SteppableServiceThread(service, SteppableServiceThread.STOP);

		stepper1.start();
		stepper2.start();
		stepper1.releaseAndWaitDone();
		verifyState(service, ServiceState.STARTING);
		
		// this thread will block until thread1 finishes onStart
		stepper2.releaseAndWaitDone(); // this step gets it past the state check into waitForStateChange
		stepper2.release(); // this release lets it go into the wait so thread 1 can re-acquire

		stepper1.releaseAndWaitDone(); // stepper1 --> RUNNING
		stepper2.waitDone(); // stepper2 --> STOPPING
		stepper2.releaseAndWaitDone(); // stepper2 --> STOPPED
		
		verifyState(service, ServiceState.STOPPED);
		verifyRefCount(service, 0);

		stepper1.releaseAndJoin();
		stepper2.releaseAndJoin();
	}

	@Test
	public void testStartWhileStopping() throws InterruptedException
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service);
		SteppableServiceThread stepper2 = new SteppableServiceThread(service, SteppableServiceThread.START);

		stepper1.start();
		stepper2.start();
		
		stepper1.releaseAndWaitDone(); // STARTING
		stepper1.releaseAndWaitDone(); // RUNNING
		stepper1.releaseAndWaitDone(); // STOPPING
		verifyState(service, ServiceState.STOPPING);

		// this thread will block until thread1 finishes stop
		stepper2.releaseAndWaitDone(); // this step gets it past the state check into waitForStateChange
		stepper2.release(); // this release lets it go into the wait so thread 1 can re-acquire

		stepper1.releaseAndWaitDone(); // STOPPED
		stepper2.waitDone(); // STARTING
		stepper2.releaseAndWaitDone(); // RUNNING

		verifyState(service, ServiceState.RUNNING);
		verifyRefCount(service, 1);

		stepper1.releaseAndJoin();
		stepper2.releaseAndJoin();
	}

	@Test
	public void testStopAndDrain() throws InterruptedException
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service);
		stepper1.setStopGracePeriod(-1L);
		SteppableServiceThread stepper2 = new SteppableServiceThread(service, SteppableServiceThread.WORK);
		
		stepper1.start();
		stepper2.start();
		
		stepper1.releaseAndWaitDone(); // STARTING
		stepper1.releaseAndWaitDone(); // RUNNING
	
		stepper2.releaseAndWaitDone(); // start work
		stepper1.release(); // try to stop -- async release -- need to finish work before this can complete

		stepper2.releaseAndWaitDone(); // finish work
		
		stepper1.waitDone(); // now we're allowed to get to STOPPING
		stepper1.releaseAndWaitDone(); // STOPPED
		
		Assert.assertNull(stepper2.getTestThrowable());
		
		stepper1.releaseAndJoin();
		stepper2.releaseAndJoin();
	}
	
	@Test
	public void testStopAndInterrupt() throws InterruptedException
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service);
		stepper1.setStopGracePeriod(10L);
		SteppableServiceThread stepper2 = new SteppableServiceThread(service, SteppableServiceThread.WORK);
		
		stepper1.start();
		stepper2.start();
		
		stepper1.releaseAndWaitDone(); // STARTING
		stepper1.releaseAndWaitDone(); // RUNNING
	
		stepper2.releaseAndWaitDone(); // start work
		stepper1.releaseAndWaitDone(); // try to stop -- this will interrupt stepper 2 after 10ms

		stepper2.waitDone(); // no need to release to finish work -- interrupt took care of that
		
		stepper1.releaseAndWaitDone(); // STOPPED
		
		stepper1.releaseAndJoin();
		stepper2.releaseAndJoin();

		Assert.assertTrue(stepper2.getWasInterrupted());
		Assert.assertNull(stepper2.getTestThrowable());
	}
	
	@Test
	public void testStopFailDrainInterrupt() throws InterruptedException
	{
		SteppableService service = new SteppableService();
		SteppableServiceThread stepper1 = new SteppableServiceThread(service);
		stepper1.setStopGracePeriod(-1L);
		SteppableServiceThread stepper2 = new SteppableServiceThread(service, SteppableServiceThread.WORK);
		
		stepper1.start();
		stepper2.start();
		
		stepper1.releaseAndWaitDone(); // STARTING
		stepper1.releaseAndWaitDone(); // RUNNING
	
		stepper2.releaseAndWaitDone(); // start work
		stepper1.release(); // try to stop -- async release -- need to finish work before this can complete

		stepper1.interrupt(); // interrupt stop thread while it's waiting for work threads to drain
		stepper1.waitDone(); // after interrupt, stepper1 interrupts all work threads and then proceeds to onStop() -- wait for it to get there
		stepper2.waitDone(); // this was interrupted by stepper1 so no need to releaseAndWaitDone, just waitDone
		
		stepper1.releaseAndWaitDone(); // STOP_FAILED
		
		stepper1.releaseAndJoin();
		stepper2.releaseAndJoin();

		Assert.assertTrue(stepper1.getWasInterrupted());
		Assert.assertTrue(stepper2.getWasInterrupted());
	}
	
}
