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
package com.brienwheeler.lib.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Extension to the standard java.util.concurrent.ThreadPoolExecutor that enforces the best practice
 * of always using a NamedThreadFactory to provide semantically useful thread names in thread pools.
 * 
 * @author bwheeler
 */
public class ThreadPoolExecutor extends java.util.concurrent.ThreadPoolExecutor
{
	protected final Log log = LogFactory.getLog(getClass());
	
	public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, NamedThreadFactory threadFactory)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory);
		onStart();
	}

	public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, NamedThreadFactory threadFactory,
			RejectedExecutionHandler handler)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, handler);
		onStart();
	}

	@Override
	public void setThreadFactory(ThreadFactory threadFactory)
	{
		if (!(threadFactory instanceof NamedThreadFactory))
			throw new IllegalArgumentException("com.brienwheeler.lib.concurrent.ScheduledThreadPoolExecutor requires a NamedThreadFactory");
		super.setThreadFactory(threadFactory);
	}

	private void onStart()
	{
		log.info("starting " + ((NamedThreadFactory) getThreadFactory()).getName());
	}

	@Override
	protected void terminated()
	{
		log.info("stopped " + ((NamedThreadFactory) getThreadFactory()).getName());
		super.terminated();
	}

}