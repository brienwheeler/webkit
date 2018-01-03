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
package com.brienwheeler.lib.test.stepper;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class SteppableThreadTest
{
	private static enum TestMode {
		RELEASE,
		WAITDONE,
		JOIN,
	}
	
	@Test
	public void testConstruct()
	{
		new SteppableNop();
	}

	@Test
	public void testSteppedNop()
	{
		SteppableThread stepper = new SteppableNop();
		stepper.start();
		stepper.releaseAndJoin();
	}

	@Test
	public void testSteppedThrows()
	{
		SteppableThread stepper = new SteppableThrows();
		stepper.start();
		stepper.releaseAndJoin();
		Assert.assertEquals(RuntimeException.class, stepper.getTestThrowable().getClass());
	}

	@Test
	public void testSteppedInterruptFlag()
	{
		SteppableThread stepper = new SteppableSetInterruptAndThrow();
		stepper.start();
		stepper.releaseAndJoin();
		Assert.assertTrue(stepper.getWasInterrupted());
	}

	@Test
	public void testSteppedOneStep()
	{
		SteppableThread stepper = new SteppableOneStep();
		stepper.start();
		stepper.releaseAndWaitDone();
		stepper.releaseAndJoin();
	}
	
	@Test
	public void testReleaseInterrupted() throws InterruptedException
	{
		CountDownLatch latch = new CountDownLatch(1);
		SteppableThread latchStepper = new SteppableWaitForLatch(latch);
		
		interruptInWait(latchStepper, TestMode.RELEASE);

		((CyclicBarrier) ReflectionTestUtils.getField(latchStepper, "stepStart")).reset();
		latchStepper.start();
		latch.countDown();
		latchStepper.releaseAndJoin();
	}

	@Test
	public void testReleaseBrokenBarrier() throws InterruptedException
	{
		CountDownLatch latch = new CountDownLatch(1);
		SteppableThread latchStepper = new SteppableWaitForLatch(latch);
		
		interruptInWait(latchStepper, TestMode.RELEASE);

		try {
			latchStepper.release();
			Assert.fail();
		}
		catch (RuntimeException e)
		{
			Assert.assertEquals(BrokenBarrierException.class, e.getCause().getClass());
		}
		
		((CyclicBarrier) ReflectionTestUtils.getField(latchStepper, "stepStart")).reset();
		latchStepper.start();
		latch.countDown();
		latchStepper.releaseAndJoin();
	}

	@Test
	public void testWaitDoneInterrupted() throws InterruptedException
	{
		SteppableThread stepper = new SteppableOneStep();

		interruptInWait(stepper, TestMode.WAITDONE);

		((CyclicBarrier) ReflectionTestUtils.getField(stepper, "stepDone")).reset();
		stepper.start();
		stepper.releaseAndWaitDone();
		stepper.releaseAndJoin();
	}
	
	@Test
	public void testWaitDoneBrokenBarrier() throws InterruptedException
	{
		SteppableThread stepper = new SteppableOneStep();

		interruptInWait(stepper, TestMode.WAITDONE);

		try {
			stepper.waitDone();
			Assert.fail();
		}
		catch (RuntimeException e)
		{
			Assert.assertEquals(BrokenBarrierException.class, e.getCause().getClass());
		}
		
		((CyclicBarrier) ReflectionTestUtils.getField(stepper, "stepDone")).reset();
		stepper.start();
		stepper.releaseAndWaitDone();
		stepper.releaseAndJoin();
	}
	
	@Test
	public void testJoinInterrupted() throws InterruptedException
	{
		SteppableThread stepper = new SteppableOneStep();
		
		stepper.start();
		interruptInWait(stepper, TestMode.JOIN);

		((CyclicBarrier) ReflectionTestUtils.getField(stepper, "stepStart")).reset();
		stepper.waitDone();
		stepper.releaseAndJoin();
	}

	@Test
	public void testJoinTimeout()
	{
		SteppableThread stepper = new SteppableOneStep();
		ReflectionTestUtils.setField(stepper, "joinDelay", 10L);
		stepper.start();
		
		try {
			stepper.releaseAndJoin();
			Assert.fail();
		}
		catch (RuntimeException e) {
			Assert.assertEquals(TimeoutException.class, e.getCause().getClass());
		}

		ReflectionTestUtils.setField(stepper, "joinDelay", 10000L);
		stepper.waitDone();
		stepper.releaseAndJoin();
	}
	
	@Test (expected = RuntimeException.class)
	public void callFromNonSteppableThread()
	{
		SteppableThread.waitForNextStep();
	}
	
	@Test
	public void testStepperInterrupt() throws InterruptedException
	{
		SteppableThread stepper = new SteppableOneStep();
		stepper.start();
		Thread.sleep(5L);
		stepper.interruptAndWait();
		stepper.releaseAndWaitDone();
		stepper.releaseAndJoin();
		Assert.assertTrue(stepper.getWasInterrupted());
	}
	
	@Test
	public void testStepperBrokenBarrier() throws InterruptedException, BrokenBarrierException
	{
		SteppableThread stepper = new SteppableOneStep();
		CyclicBarrier barrier = (CyclicBarrier) ReflectionTestUtils.getField(stepper, "stepStart");
		breakBarrier(barrier);
		stepper.start();
		Thread.sleep(5L);
		((CyclicBarrier) ReflectionTestUtils.getField(stepper, "stepStart")).reset();
		stepper.waitDone();
		stepper.releaseAndJoin();
		Assert.assertEquals(BrokenBarrierException.class, stepper.getTestThrowable().getCause().getClass());
	}

	private void interruptInWait(SteppableThread stepper, TestMode mode) throws InterruptedException
	{
		final Thread testThread = Thread.currentThread();
		Thread interruptThread = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(10L);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				
				testThread.interrupt();
			}
		};
		interruptThread.start();
		
		try {
			if (mode == TestMode.RELEASE)
				stepper.release();
			else if (mode == TestMode.WAITDONE)
				stepper.waitDone();
			else if (mode == TestMode.JOIN)
				stepper.releaseAndJoin();
			Assert.fail();
		}
		catch (RuntimeException e) {
			Assert.assertTrue(Thread.interrupted()); // test and clear
			Assert.assertEquals(InterruptedException.class, e.getCause().getClass());
		}
		
		interruptThread.join();
	}
	
	private void breakBarrier(CyclicBarrier barrier) throws BrokenBarrierException
	{
		final Thread testThread = Thread.currentThread();
		Thread interruptThread = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(10L);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				
				testThread.interrupt();
			}
		};
		interruptThread.start();
		
		try {
			barrier.await();
			Assert.fail();
		}
		catch (InterruptedException e) {
		}
	}
}
