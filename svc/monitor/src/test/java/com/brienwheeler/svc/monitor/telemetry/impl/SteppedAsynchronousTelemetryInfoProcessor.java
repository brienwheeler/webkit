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
package com.brienwheeler.svc.monitor.telemetry.impl;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.test.util.ReflectionTestUtils;

import com.brienwheeler.lib.concurrent.StoppableThread;
import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;

public class SteppedAsynchronousTelemetryInfoProcessor
		extends AsynchronousTelemetryInfoProcessor
{
	public final CyclicBarrier beforeTakeBarrier = new CyclicBarrier(2);
	public final CyclicBarrier afterProcessBarrier = new CyclicBarrier(2);
	public final AtomicBoolean interruptExpected = new AtomicBoolean(false);
	
	@Override
	protected void beforeTake()
	{
		try {
			beforeTakeBarrier.await();
			if (interruptExpected.get()) {
				// interrupt is expected, now that we have synced up with thread that will
				// interrupt us, wait a second time 
				beforeTakeBarrier.await();
			}
		}
		catch (InterruptedException e) {
			beforeTakeBarrier.reset();
			// re-post interrupt so next take() throws InterruptedException
			Thread.currentThread().interrupt();
			if (!interruptExpected.compareAndSet(true,  false)) {
				// not expected, log error
				log.error("interrupted in wait for test to signal go-ahead", e);
			}
			else {
				// expected was true and expected now reset to false
				log.info("interrupted, re-posted interrupt");
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void afterProcess()
	{
		try {
			afterProcessBarrier.await();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void releaseThreadInBeforeTake()
	{
		try {
			beforeTakeBarrier.await();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void waitForThreadToGetToAfterProcess()
	{
		try {
			afterProcessBarrier.await();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void stopBackgroundThread() throws InterruptedException
	{
		StoppableThread backgroundThread = getThread();
		interruptExpected.set(true);

		// release once so we know it's in second await() before interrupting it with shutdown
		releaseThreadInBeforeTake();
		
		// should now be in second await(), OK to interrupt
		backgroundThread.shutdown(1L);
		backgroundThread.join();
	}
	
	@SuppressWarnings("unchecked")
	public void startNewBackgroundThread() throws InterruptedException
	{
		TelemetryInfoProcessThread backgroundThread = new TelemetryInfoProcessThread(getQueue());
		((AtomicReference<StoppableThread>) ReflectionTestUtils.getField(this, "backgroundThread")).set(backgroundThread);
		backgroundThread.start();
	}
	
	@SuppressWarnings("unchecked")
	public LinkedBlockingQueue<TelemetryInfo> getQueue()
	{
		return ((AtomicReference<LinkedBlockingQueue<TelemetryInfo>>) ReflectionTestUtils.getField(this, "queue")).get();
	}

	@SuppressWarnings("unchecked")
	public StoppableThread getThread()
	{
		return ((AtomicReference<StoppableThread>) ReflectionTestUtils.getField(this, "backgroundThread")).get();
	}

	@SuppressWarnings("unchecked")
	public QueueFullPolicy getQueueFullPolicy()
	{
		return ((AtomicReference<QueueFullPolicy>) ReflectionTestUtils.getField(this, "queueFullPolicy")).get();
	}

	@SuppressWarnings("unchecked")
	public ShutdownBehavior getShutdownBehavior()
	{
		return ((AtomicReference<ShutdownBehavior>) ReflectionTestUtils.getField(this, "shutdownBehavior")).get();
	}

}
