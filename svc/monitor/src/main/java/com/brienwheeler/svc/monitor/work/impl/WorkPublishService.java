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
package com.brienwheeler.svc.monitor.work.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Required;

import com.brienwheeler.lib.concurrent.StoppableThread;
import com.brienwheeler.lib.monitor.work.IWorkPublishService;
import com.brienwheeler.lib.monitor.work.IWorkRecordCollectionProcessor;
import com.brienwheeler.lib.monitor.work.WorkMonitor;
import com.brienwheeler.lib.monitor.work.WorkRecordCollection;
import com.brienwheeler.lib.svc.impl.SpringStoppableServiceBase;
import com.brienwheeler.lib.util.ValidationUtils;

public class WorkPublishService extends SpringStoppableServiceBase 
		implements IWorkPublishService
{
	private final AtomicInteger publishPeriodicity = new AtomicInteger(60); // default one minute
	private final AtomicReference<WorkPublishThread> workPublishThread =
			new AtomicReference<WorkPublishService.WorkPublishThread>();
	private final CopyOnWriteArraySet<IWorkRecordCollectionProcessor> processors =
			new CopyOnWriteArraySet<IWorkRecordCollectionProcessor>();
	private final CopyOnWriteArraySet<WorkMonitor> workMonitors =
			new CopyOnWriteArraySet<WorkMonitor>();
	private volatile boolean enabled = true;
	
	public WorkPublishService()
	{
		// not allowed for subclasses of this service
		super.setAutowireWorkPublishers(false);
	}

	@Override
	public final void setAutowireWorkPublishers(boolean autowireWorkPublishers)
	{
		// not allowed for subclasses of this service
		throw new IllegalArgumentException("not allowed to autowire work publishers into " + getClass().getSimpleName());
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
	
	@Required
	public void setProcessors(Collection<IWorkRecordCollectionProcessor> processors)
	{
		this.processors.retainAll(processors);
		this.processors.addAll(processors);
	}
	
	public void setPublishPeriodicity(int publishPeriodicity)
	{
		ValidationUtils.assertTrue(publishPeriodicity > 0, "publishPeriodicity must be greater than 0");
		this.publishPeriodicity.set(publishPeriodicity);
	}

	@Override
	public void registerWorkMonitor(WorkMonitor workMonitor)
	{
		if (workMonitors.add(workMonitor))
			start();
	}

	@Override
	public void deregisterWorkMonitor(WorkMonitor workMonitor)
	{
		if (workMonitors.remove(workMonitor))
			stopImmediate();
	}

	@Override
	protected void onStart() throws InterruptedException
	{
		super.onStart();

		WorkPublishThread thread = new WorkPublishThread();
		thread.start();
		workPublishThread.set(thread);
	}

	@Override
	protected void onStop() throws InterruptedException
	{
		WorkPublishThread thread = workPublishThread.getAndSet(null);
		if (thread != null)
			thread.shutdown(stopGracePeriod);

		super.onStop();
	}

	// testability
	protected void afterProcess()
	{
	}
	
	private void processMonitors(long timestamp)
	{
		// first phase, collect records in case any record processing takes a while
		List<WorkRecordCollection> workRecordCollections = new ArrayList<WorkRecordCollection>();
		for (WorkMonitor workMonitor : workMonitors)
			workRecordCollections.add(workMonitor.rollRecords());
		
		for (IWorkRecordCollectionProcessor processor : processors) {
			for (WorkRecordCollection workRecordCollection : workRecordCollections)
				processor.process(timestamp, workRecordCollection);
		}
		
		afterProcess();
	}

	private class WorkPublishThread extends StoppableThread
	{
		public WorkPublishThread()
		{
			super(WorkPublishService.class.getSimpleName(), log);
		}

		@Override
		public void onRun()
		{
			while (!isShutdown()) {
				log.debug("running");
				// compute when we should next wake up -- on next even periodicity
				// boundary from epoch
				long periodicityLong = publishPeriodicity.get() * 1000L;
				long now = System.currentTimeMillis();
				long nextWake = now - (now % periodicityLong) + periodicityLong;
				
				try {
					if (nextWake > now) {
						log.debug("sleeping " + (nextWake - now));
						Thread.sleep(nextWake - now);
					}
					
					if (enabled) {
						log.debug("processing");
						processMonitors(nextWake);
						// check for execution taking longer than desired periodicity
						now = System.currentTimeMillis();
						if (now >= nextWake + periodicityLong)
							log.warn("work publishing took " + (now - nextWake) + "ms, greater than periodicity of " + periodicityLong + "ms");
					}
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					// probably isShutdown(), continue and check
				}
			}
		}
		
	}
}
