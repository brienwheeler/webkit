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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.brienwheeler.lib.concurrent.StoppableThread;
import com.brienwheeler.lib.monitor.telemetry.ITelemetryInfoProcessor;
import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;
import com.brienwheeler.lib.svc.GracefulShutdown;
import com.brienwheeler.lib.svc.ServiceState;
import com.brienwheeler.lib.util.ValidationUtils;

public class AsynchronousTelemetryInfoProcessor extends TelemetryServiceBase
		implements ITelemetryInfoProcessor
{
	public static enum QueueFullPolicy {
		DISCARD_OLDEST,
		DISCARD_OFFERED,
	}
	
	public static enum ShutdownBehavior {
		DISCARD,
		PROCESS,
	}
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private final AtomicReference<StoppableThread> backgroundThread =
			new AtomicReference<StoppableThread>();
	private final AtomicReference<BlockingQueue<TelemetryInfo>> queue =
			new AtomicReference<BlockingQueue<TelemetryInfo>>();
	private final AtomicReference<QueueFullPolicy> queueFullPolicy =
			new AtomicReference<QueueFullPolicy>(QueueFullPolicy.DISCARD_OLDEST);
	private final AtomicReference<ShutdownBehavior> shutdownBehavior =
			new AtomicReference<ShutdownBehavior>(ShutdownBehavior.PROCESS);
	private int maxCapacity = Integer.MAX_VALUE;
	
	public void setQueueFullPolicy(QueueFullPolicy queueFullPolicy)
	{
		ValidationUtils.assertNotNull(queueFullPolicy, "queueFullPolicy cannot be null");
		ensureState(ServiceState.STOPPED, "can't change QueueFullPolicy when not STOPPED");
		this.queueFullPolicy.set(queueFullPolicy);
	}

	public void setShutdownBehavior(ShutdownBehavior shutdownBehavior)
	{
		ValidationUtils.assertNotNull(shutdownBehavior, "shutdownBehavior cannot be null");
		ensureState(ServiceState.STOPPED, "can't change ShutdownBehavior when not STOPPED");
		this.shutdownBehavior.set(shutdownBehavior);
	}

	public void setMaxCapacity(int maxCapacity)
	{
		ValidationUtils.assertTrue(maxCapacity > 0, "maxCapacity must be greater than zero");
		ensureState(ServiceState.STOPPED, "can't change MaxCapacity when not STOPPED");
		this.maxCapacity = maxCapacity;
	}
	
	@Override
	protected void onStart() throws InterruptedException
	{
		super.onStart();
		
		LinkedBlockingQueue<TelemetryInfo> queue = new LinkedBlockingQueue<TelemetryInfo>(maxCapacity);
		this.queue.set(queue);
		
		TelemetryInfoProcessThread backgroundThread = new TelemetryInfoProcessThread(queue);
		this.backgroundThread.set(backgroundThread);
		backgroundThread.start();
	}


	@Override
	protected void onStop() throws InterruptedException
	{
		backgroundThread.get().shutdown();
		super.onStop();
	}


	@Override
	@GracefulShutdown
	public void process(TelemetryInfo telemetryInfo)
	{
		telemetryInfo.checkPublished();

		BlockingQueue<TelemetryInfo> queue = this.queue.get();
		
		switch (queueFullPolicy.get())
		{
			case DISCARD_OFFERED :
				queue.offer(telemetryInfo);
				// return regardless of success
				return;
				
			case DISCARD_OLDEST :
				while (!queue.offer(telemetryInfo))
				{
					// if offer failed, remove and discard one element from queue and try
					// again
					queue.poll();
				}
				return;
		}
	}

	// testability
	protected void beforeTake()
	{
	}
	
	// testability
	protected void afterProcess()
	{
	}
	
	class TelemetryInfoProcessThread extends StoppableThread
	{
		private final BlockingQueue<TelemetryInfo> queue;
		
		TelemetryInfoProcessThread(BlockingQueue<TelemetryInfo> queue)
		{
			super(AsynchronousTelemetryInfoProcessor.class.getSimpleName(), log);
			this.queue = queue;
		}
		
		@Override
		public void onRun()
		{
			while (!isShutdown())
			{
				try {
					beforeTake();
					TelemetryInfo telemetryInfo = queue.take();
					callProcessors(telemetryInfo);
					afterProcess();
				}
				catch (InterruptedException e) {
					// probably isShutdown(), continue and check
					Thread.currentThread().interrupt();
				}
			}
			
			switch (shutdownBehavior.get())
			{
				case DISCARD :
					log.info("discarding " + queue.size() + " queued TelemetryInfo at shutdown");
					break;
					
				case PROCESS :
					log.info("processing " + queue.size() + " queued TelemetryInfo at shutdown");
					for (TelemetryInfo telemetryInfo : queue)
						callProcessors(telemetryInfo);
					break;
			}
			
			queue.clear();
			return;
		}
	}
}
