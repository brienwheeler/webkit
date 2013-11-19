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
package com.brienwheeler.lib.test.stepper;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class SteppableThread extends Thread
{
	private static final boolean verbose = true; // set to true for easier debugging
	
	protected final Log log = LogFactory.getLog(getClass());
	private final CyclicBarrier stepDone = new CyclicBarrier(2); // this thread and a controlling thread
	private final CyclicBarrier stepStart = new CyclicBarrier(2);
	private volatile boolean firstWait = true;
	private Throwable testThrowable = null;
	private volatile boolean wasInterrupted = false;
	private long joinDelay = 10000L;
	
	protected abstract void onRun();
	
	@Override
	public final void run()
	{
		log.info("starting");
		try {
			onRun();
			wasInterrupted = Thread.currentThread().isInterrupted();
		}
		catch (Throwable e) {
			testThrowable = e;
			if (Thread.currentThread().isInterrupted())
				wasInterrupted = true;
		}
		finally {
			// wait to be released for thread exit
			SteppableThread.waitForNextStep();
			log.info("exiting");
		}
	}

	public void release()
	{
		try {
			verbose("releasing " + getName());
			if (stepStart.await() == 0)
				stepStart.reset();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		catch (BrokenBarrierException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void waitDone()
	{
		try {
			verbose("waiting for done " + getName());
			if (stepDone.await() == 0)
				stepDone.reset();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		catch (BrokenBarrierException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void releaseAndWaitDone()
	{
		release();
		waitDone();
	}

	public Throwable getTestThrowable()
	{
		return testThrowable;
	}
	
	public boolean getWasInterrupted()
	{
		return wasInterrupted;
	}
	
	public void releaseAndJoin()
	{
		try {
			release();
			verbose("joining " + getName());
			join(joinDelay);
			if (isAlive())
				throw new RuntimeException(new TimeoutException());
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}
	
	public static void waitForNextStep()
	{
		if (!(Thread.currentThread() instanceof SteppableThread))
			throw new RuntimeException("can only call waitForNextStep() from inside SteppableThread");
		SteppableThread thread = (SteppableThread) Thread.currentThread();
		
		boolean interrupted = Thread.interrupted(); // test and clear
		
		try {
			if (thread.firstWait) {
				thread.firstWait = false;
			}
			else {
				thread.verbose("signalling done");
				if (thread.stepDone.await() == 0)
					thread.stepDone.reset();
			}
			
			thread.verbose("waiting for release");
			if (thread.stepStart.await() == 0)
				thread.stepStart.reset();
		}
		catch (InterruptedException e) {
			thread.verbose("interrupted");
			interrupted = true;
			thread.stepStart.reset();
		}
		catch (BrokenBarrierException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (interrupted)
				thread.interrupt();
		}
	}
	
	protected void verbose(String message)
	{
		if (verbose)
			log.info(message);
		else
			log.debug(message);
	}
}
