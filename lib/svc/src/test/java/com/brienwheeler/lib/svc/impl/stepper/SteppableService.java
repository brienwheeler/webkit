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
package com.brienwheeler.lib.svc.impl.stepper;

import java.util.concurrent.atomic.AtomicBoolean;

import com.brienwheeler.lib.svc.GracefulShutdown;
import com.brienwheeler.lib.svc.ServiceState;
import com.brienwheeler.lib.svc.impl.StoppableServiceBase;
import com.brienwheeler.lib.test.stepper.SteppableThread;

public class SteppableService extends StoppableServiceBase
{
	private AtomicBoolean stepping = new AtomicBoolean(true);
	@SuppressWarnings("unused")
	private SteppableService child1 = null;
	@SuppressWarnings("unused")
	private SteppableService child2 = null;
	
	public SteppableService()
	{
	}
	
	public SteppableService(SteppableService child1, SteppableService child2)
	{
		this.child1 = child1;
		this.child2 = child2;
	}
		
	public void setStepping(boolean stepping)
	{
		this.stepping.set(stepping);
	}
	
	@Override
	public void start()
	{
		if (stepping.get())
			SteppableThread.waitForNextStep();
		super.start();
	}

	@Override
	protected void onStart() throws InterruptedException
	{
		if (stepping.get())
			SteppableThread.waitForNextStep();
		SteppableServiceThread.checkFailOnStart();
	}
	
	@Override
	public void stop(long gracePeriod)
	{
		if (stepping.get())
			SteppableThread.waitForNextStep();
		super.stop(gracePeriod);
	}

	@Override
	protected void onStop() throws InterruptedException
	{
		if (stepping.get())
			SteppableThread.waitForNextStep();
		SteppableServiceThread.checkFailOnStop();
	}
	
	@Override
	protected ServiceState waitForStateChangeEx()
	{
		log.info("waiting for state change");
		if (stepping.get())
			SteppableThread.waitForNextStep();
		return super.waitForStateChangeEx();
	}
	
	public void doSteppedWork()
	{
		// wait before entering service method
		if (stepping.get())
			SteppableThread.waitForNextStep();
		testWork();
	}
	
	@GracefulShutdown
	public void testWork()
	{
		log.info("in testWork");
		// wait before leaving service method
		if (stepping.get())
			SteppableThread.waitForNextStep();
		log.info("leaving testWork");
	}

}
