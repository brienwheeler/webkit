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
package com.brienwheeler.svc.monitor.telemetry.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;
import com.brienwheeler.lib.svc.ServiceState;
import com.brienwheeler.lib.svc.ServiceStateException;
import com.brienwheeler.lib.svc.impl.mocks.NullSpringStoppableService;
import com.brienwheeler.lib.util.ValidationException;
import com.brienwheeler.svc.monitor.telemetry.impl.AsynchronousTelemetryInfoProcessor.QueueFullPolicy;
import com.brienwheeler.svc.monitor.telemetry.impl.AsynchronousTelemetryInfoProcessor.ShutdownBehavior;

@ContextConfiguration({
    "classpath:com/brienwheeler/lib/spring/beans/contextBeanDumper.xml",
    "classpath:com/brienwheeler/svc/monitor/telemetry/AsynchronousTelemetryInfoProcessor-test1Context.xml" })
public class AsynchronousTelemetryInfoProcessorTest extends AbstractJUnit4SpringContextTests
{
	private static final Log log = LogFactory.getLog(AsynchronousTelemetryInfoProcessorTest.class);
	private static final long TEST_SHUTDOWN_DELAY = 30000L;
	
	private static SteppedAsynchronousTelemetryInfoProcessor processor;
	private static NullSpringStoppableService service;
	private static TelemetryRecordingProcessor recordingProcessor;

	// instead of @BeforeClass this is called for lazy initialization from a @Before so that
	// applicationContext is created and started
	private static void initialize(ApplicationContext applicationContext)
	{
		if (service != null)
			return;
		
		service = applicationContext.getBean(NullSpringStoppableService.class);
		
		TelemetryPublishService telemetryPublishService = applicationContext.getBean(TelemetryPublishService.class);
		processor = TelemetryServiceBaseTestUtils.findProcessor(telemetryPublishService, SteppedAsynchronousTelemetryInfoProcessor.class);
		recordingProcessor = TelemetryServiceBaseTestUtils.findProcessor(processor, TelemetryRecordingProcessor.class);
	}

	@AfterClass
	public static void cleanup() throws InterruptedException
	{
		// let processor go into take() before shutdown()
		if (processor != null)
			processor.releaseThreadInBeforeTake();
	}
	
	@Before
	public void onSetup()
	{
		initialize(applicationContext);
	}
	
	@Test
	public void testConstruct() throws InterruptedException
	{
		SteppedAsynchronousTelemetryInfoProcessor proc = new SteppedAsynchronousTelemetryInfoProcessor();
		Assert.assertNull(proc.getQueue());
		Assert.assertNull(proc.getThread());
		Assert.assertEquals(QueueFullPolicy.DISCARD_OLDEST, proc.getQueueFullPolicy());
		Assert.assertEquals(ShutdownBehavior.PROCESS, proc.getShutdownBehavior());
	}
	
	@Test
	public void testStartStop() throws InterruptedException
	{
		SteppedAsynchronousTelemetryInfoProcessor proc = new SteppedAsynchronousTelemetryInfoProcessor();
		
		proc.start();		
		try {
			Assert.assertEquals(Integer.MAX_VALUE, proc.getQueue().remainingCapacity());
			Assert.assertTrue(proc.getThread().isAlive());
		}
		finally {
			stopSteppedProcessor(proc);
			Assert.assertFalse(proc.getThread().isAlive());
		}
	}
	
	@Test
	public void testSetters() throws InterruptedException
	{
		SteppedAsynchronousTelemetryInfoProcessor proc = new SteppedAsynchronousTelemetryInfoProcessor();
		proc.setMaxCapacity(1);
		proc.setQueueFullPolicy(QueueFullPolicy.DISCARD_OFFERED);
		proc.setShutdownBehavior(ShutdownBehavior.DISCARD);

		Assert.assertEquals(QueueFullPolicy.DISCARD_OFFERED, proc.getQueueFullPolicy());
		Assert.assertEquals(ShutdownBehavior.DISCARD, proc.getShutdownBehavior());
		
		proc.start();
		try {
			Assert.assertEquals(1, proc.getQueue().remainingCapacity());
		}
		finally {
			stopSteppedProcessor(proc);
		}
	}

	@Test(expected = ValidationException.class)
	public void testSetQueueFullPolicyFailNull() throws InterruptedException
	{
		SteppedAsynchronousTelemetryInfoProcessor proc = new SteppedAsynchronousTelemetryInfoProcessor();
		proc.setQueueFullPolicy(null);
	}

	@Test(expected = ServiceStateException.class)
	public void testSetQueueFullPolicyFailRunning() throws InterruptedException
	{
		SteppedAsynchronousTelemetryInfoProcessor proc = new SteppedAsynchronousTelemetryInfoProcessor();
		proc.start();
		try {
			proc.setQueueFullPolicy(QueueFullPolicy.DISCARD_OFFERED);
		}
		finally {
			stopSteppedProcessor(proc);
		}
	}
	
	@Test(expected = ValidationException.class)
	public void testSetShutdownBehaviorFailNull() throws InterruptedException
	{
		SteppedAsynchronousTelemetryInfoProcessor proc = new SteppedAsynchronousTelemetryInfoProcessor();
		proc.setShutdownBehavior(null);
	}

	@Test(expected = ServiceStateException.class)
	public void testSetShutdownBehaviorFailRunning() throws InterruptedException
	{
		SteppedAsynchronousTelemetryInfoProcessor proc = new SteppedAsynchronousTelemetryInfoProcessor();
		proc.start();
		try {
			proc.setShutdownBehavior(ShutdownBehavior.DISCARD);
		}
		finally {
			stopSteppedProcessor(proc);
		}
	}
	
	@Test(expected = ValidationException.class)
	public void testSetMaxCapacityLessThanOne() throws InterruptedException
	{
		SteppedAsynchronousTelemetryInfoProcessor proc = new SteppedAsynchronousTelemetryInfoProcessor();
		proc.setMaxCapacity(0);
	}

	@Test(expected = ServiceStateException.class)
	public void testSetMaxCapacityFailRunning() throws InterruptedException
	{
		SteppedAsynchronousTelemetryInfoProcessor proc = new SteppedAsynchronousTelemetryInfoProcessor();
		proc.start();
		try {
			proc.setMaxCapacity(1);
		}
		finally {
			stopSteppedProcessor(proc);
		}
	}
	
	@Test
	public void testProcess() throws InterruptedException
	{
		service.publishTelemetry(new TelemetryInfo("telemetryInfo", log));
		
		int startCount = recordingProcessor.getCount();
		// let the thread in the AsyncProcessor pick up published event and wait
		// until it finishes processing it.
		processor.releaseThreadInBeforeTake();
		processor.waitForThreadToGetToAfterProcess();
		int endCount = recordingProcessor.getCount();
		Assert.assertEquals(1, endCount - startCount);
	}
	
	@Test
	public void testMaxCapacity() throws InterruptedException
	{
		int stopCount = stopContextProcessor();
		processor.setMaxCapacity(2);
		startContextProcessor(stopCount);

		Assert.assertEquals(0,  processor.getQueue().size());
		service.publishTelemetry(new TelemetryInfo("telemetryInfo1", log));
		Assert.assertEquals(1,  processor.getQueue().size());
		service.publishTelemetry(new TelemetryInfo("telemetryInfo2", log));
		Assert.assertEquals(2,  processor.getQueue().size());
		service.publishTelemetry(new TelemetryInfo("telemetryInfo3", log));
		Assert.assertEquals(2,  processor.getQueue().size());

		int startCount = recordingProcessor.getCount();
		// let the thread in the AsyncProcessor pick up published events and wait
		// until it finishes processing them.
		processor.releaseThreadInBeforeTake();
		processor.waitForThreadToGetToAfterProcess();
		processor.releaseThreadInBeforeTake();
		processor.waitForThreadToGetToAfterProcess();
		int endCount = recordingProcessor.getCount();
		Assert.assertEquals(2, endCount - startCount);
	}
	
	@Test
	public void testDiscardOldest() throws InterruptedException
	{
		int stopCount = stopContextProcessor();
		processor.setMaxCapacity(1);
		processor.setQueueFullPolicy(QueueFullPolicy.DISCARD_OLDEST);
		startContextProcessor(stopCount);

		service.publishTelemetry(new TelemetryInfo("telemetryInfoOlder", log));
		service.publishTelemetry(new TelemetryInfo("telemetryInfoNewer", log));
		Assert.assertEquals("telemetryInfoNewer", processor.getQueue().peek().getName());

		int startCount = recordingProcessor.getCount();
		// let the thread in the AsyncProcessor pick up published event and wait
		// until it finishes processing it.
		processor.releaseThreadInBeforeTake();
		processor.waitForThreadToGetToAfterProcess();
		int endCount = recordingProcessor.getCount();
		Assert.assertEquals(1, endCount - startCount);
	}
	
	@Test
	public void testDiscardOffered() throws InterruptedException
	{
		int stopCount = stopContextProcessor();
		processor.setMaxCapacity(1);
		processor.setQueueFullPolicy(QueueFullPolicy.DISCARD_OFFERED);
		startContextProcessor(stopCount);

		service.publishTelemetry(new TelemetryInfo("telemetryInfoOlder", log));
		service.publishTelemetry(new TelemetryInfo("telemetryInfoNewer", log));
		Assert.assertEquals("telemetryInfoOlder", processor.getQueue().peek().getName());

		int startCount = recordingProcessor.getCount();
		// let the thread in the AsyncProcessor pick up published event and wait
		// until it finishes processing it.
		processor.releaseThreadInBeforeTake();
		processor.waitForThreadToGetToAfterProcess();
		int endCount = recordingProcessor.getCount();
		Assert.assertEquals(1, endCount - startCount);
	}
	
	@Test
	public void testShutdownBehaviorDiscard() throws InterruptedException
	{
		int stopCount = stopContextProcessor();
		processor.setMaxCapacity(2);
		processor.setShutdownBehavior(ShutdownBehavior.DISCARD);
		startContextProcessor(stopCount);
		
		service.publishTelemetry(new TelemetryInfo("telemetryInfo1", log));
		service.publishTelemetry(new TelemetryInfo("telemetryInfo2", log));
		Assert.assertEquals(2, processor.getQueue().size());

		int startCount = recordingProcessor.getCount();
		
		processor.stopBackgroundThread();
		int endCount = recordingProcessor.getCount();
		Assert.assertEquals(endCount, startCount);
		
		processor.startNewBackgroundThread();
	}
	
	@Test
	public void testShutdownBehaviorProcess() throws InterruptedException
	{
		int stopCount = stopContextProcessor();
		processor.setMaxCapacity(2);
		processor.setShutdownBehavior(ShutdownBehavior.PROCESS);
		startContextProcessor(stopCount);
		
		service.publishTelemetry(new TelemetryInfo("telemetryInfo1", log));
		service.publishTelemetry(new TelemetryInfo("telemetryInfo2", log));
		Assert.assertEquals(2, processor.getQueue().size());

		int startCount = recordingProcessor.getCount();
		
		processor.stopBackgroundThread();
		int endCount = recordingProcessor.getCount();
		Assert.assertEquals(2, endCount - startCount);
		
		processor.startNewBackgroundThread();
	}
	
	private int stopSteppedProcessor(SteppedAsynchronousTelemetryInfoProcessor processor) throws InterruptedException
	{
		// let processor go into take() before shutdown()
		processor.releaseThreadInBeforeTake();

		int stopCount = 0;
		while (processor.getState() == ServiceState.RUNNING) {
			stopCount++;
			processor.stop(TEST_SHUTDOWN_DELAY);
		}

		return stopCount;
	}

	private int stopContextProcessor() throws InterruptedException
	{
		return stopSteppedProcessor(processor);
	}
	
	private void startContextProcessor(int stopCount)
	{
		for (int i=0; i<stopCount; i++)
			processor.start();
	}
	
}
