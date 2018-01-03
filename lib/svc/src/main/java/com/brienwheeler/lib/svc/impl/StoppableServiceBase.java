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

import com.brienwheeler.lib.svc.IStoppableService;
import com.brienwheeler.lib.svc.ServiceOperationException;
import com.brienwheeler.lib.svc.ServiceState;
import com.brienwheeler.lib.util.ObjectUtils;

public abstract class StoppableServiceBase extends StartableServiceBase implements IStoppableService
{
	protected void onStop() throws InterruptedException
	{
	}
	
	@Override
	public void stopImmediate()
	{
		stop(0);
	}

	@Override
	public void stop(long gracePeriod)
	{
		boolean interrupted = false;
		Throwable error = null;

		synchronized (state)
		{
			boolean done = false;
			while (!done)
			{
				switch (state.get())
				{
					case STOPPED :
						return;
						
					case STOPPING :
						if (waitForStateChangeEx() != ServiceState.STOPPED)
							throw new ServiceOperationException("stop operation in other thread failed");
						return;
						
					case STOP_FAILED :
						throw new ServiceOperationException("previous stop operation failed");

					case STARTING :
						waitForStateChangeEx();
						break;
					
					case RUNNING :
						int newRefCount = decrementRefCount();
						if (newRefCount > 0)
							return;
						
						changeState(ServiceState.STOPPING);
						done = true;
						break;
				}
			}
			
			// wait supplied grace period, then interrupt any threads working in the service
			if (!serviceThreads.isEmpty())
			{
				// wait for threads to drain.  < 0 means wait forever, 0 means don't wait, > 0 means number
				// of milliseconds to wait
				long now = System.currentTimeMillis();
				long drainEnd = gracePeriod < 0 ? Long.MAX_VALUE : now + gracePeriod;
				while (!serviceThreads.isEmpty() && (now < drainEnd))
				{
					try {
						if (log.isDebugEnabled())
							log.debug(logId + ": waiting " + (drainEnd - now) + "ms for worker threads to drain");
						state.wait(drainEnd - now);
						now = System.currentTimeMillis();
					}
					catch (InterruptedException e) {
						interrupted = true;
						error = new ServiceOperationException("stop operation thread interrupted", e);
						break;
					}
				}

				for (Thread serviceThread : serviceThreads)
				{
					if (log.isDebugEnabled())
						log.debug(logId + ": interrupting worker thread " + ObjectUtils.getUniqueId(serviceThread));
					serviceThread.interrupt();
				}
			}
		}

		// first call subclass onStop()
		try {
			onStop();
		}
		catch (InterruptedException e) {
			interrupted = true;
			// don't overwrite potential SOE from InterruptedException handler above
			if (error == null)
				error = new ServiceOperationException("stop operation thread interrupted", e);
		}
		catch (RuntimeException e)
		{
			// don't overwrite potential SOE from InterruptedException handler above
			if (error == null)
				error = e;
		}
		catch (Error e)
		{
			// don't overwrite potential SOE from InterruptedException handler above
			if (error == null)
				error = e;
		}
		
		// regardless of errors from onStop(), always call shutdownSubServices()
		try {
			shutdownSubServices();
		}
		catch (RuntimeException e)
		{
			// don't overwrite potential SOE from InterruptedException handlers above
			if (error == null)
				error = e;
		}
		catch (Error e)
		{
			// don't overwrite potential SOE from InterruptedException handlers above
			if (error == null)
				error = e;
		}
		
		if (error == null)
			changeState(ServiceState.STOPPED);
		else
			changeState(ServiceState.STOP_FAILED);
		
		// deregister our WorkMonitor with any work publishers
		deregisterWithWorkPublishers();

		// re-post interrupted flag if needed
		if (interrupted)
			Thread.currentThread().interrupt();
		
		// throw error experienced during shutdown (if any)
		if (error != null && error instanceof RuntimeException)
			throw (RuntimeException) error;
		if (error != null && error instanceof Error)
			throw (Error) error;
	}
}

