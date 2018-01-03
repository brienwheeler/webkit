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
package com.brienwheeler.lib.svc.impl.stepper;

import com.brienwheeler.lib.test.stepper.SteppableThread;

public class SteppableServiceThread extends SteppableThread
{
	public static final int START = 0x0001;
	public static final int STOP  = 0x0002;
	public static final int WORK  = 0x0004;
	
	protected final SteppableService service;
	private final int mode;
	private long stopGracePeriod = 0L;
	private Throwable failOnStart = null;
	private Throwable failOnStop = null;

	public SteppableServiceThread(SteppableService service)
	{
		this(service, START | STOP);
	}

	public SteppableServiceThread(SteppableService service, int mode)
	{
		this.service = service;
		this.mode = mode;
	}
	
	public void setFailOnStart(Throwable failOnStart)
	{
		this.failOnStart = failOnStart;
	}

	public void setFailOnStop(Throwable failOnStop)
	{
		this.failOnStop = failOnStop;
	}

	public void setStopGracePeriod(long stopGracePeriod)
	{
		this.stopGracePeriod = stopGracePeriod;
	}
	
	@Override
	public void onRun()
	{
		if ((mode & START) != 0)
			service.start();
		if ((mode & WORK) != 0)
			service.doSteppedWork();
		if ((mode & STOP) != 0)
			service.stop(stopGracePeriod);
	}

	public static void checkFailOnStart() throws InterruptedException
	{
		if (!(Thread.currentThread() instanceof SteppableServiceThread))
			throw new RuntimeException("can only call waitForNextStep() from inside SteppableServiceThread");
		SteppableServiceThread thread = (SteppableServiceThread) Thread.currentThread();
		
		if (thread.failOnStart != null) {
			thread.log.info("onStart throwing " + thread.failOnStart.getClass().getSimpleName());
			if (thread.failOnStart instanceof InterruptedException)
				throw (InterruptedException) thread.failOnStart;
			if (thread.failOnStart instanceof RuntimeException)
				throw (RuntimeException) thread.failOnStart;
			if (thread.failOnStart instanceof Error)
				throw (Error) thread.failOnStart;
			throw new RuntimeException(thread.failOnStart);
		}
	}

	public static void checkFailOnStop() throws InterruptedException
	{
		if (!(Thread.currentThread() instanceof SteppableServiceThread))
			throw new RuntimeException("can only call waitForNextStep() from inside SteppableServiceThread");
		SteppableServiceThread thread = (SteppableServiceThread) Thread.currentThread();
		
		if (thread.failOnStop != null) {
			thread.log.info("onStop throwing " + thread.failOnStop.getClass().getSimpleName());
			if (thread.failOnStop instanceof InterruptedException)
				throw (InterruptedException) thread.failOnStop;
			if (thread.failOnStop instanceof RuntimeException)
				throw (RuntimeException) thread.failOnStop;
			if (thread.failOnStop instanceof Error)
				throw (Error) thread.failOnStop;
			throw new RuntimeException(thread.failOnStop);
		}
	}
}
