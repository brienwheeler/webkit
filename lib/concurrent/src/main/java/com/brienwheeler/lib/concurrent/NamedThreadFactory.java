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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import com.brienwheeler.lib.util.ValidationUtils;

/**
 * A {@link ThreadFactory} that creates threads belonging to a single parent {@link ThreadGroup}
 * and names each thread <i>name-count</i> where <i>name</i> is the value passed to setName() and
 * <i>count</i> is an increasing index for this NamedThreadFactory.
 * <br/>
 * <br/>
 * setName() must be called before newThread() or getThreadGroup() or an {@link IllegalStateException} will be thrown
 * <br/>
 * setName() cannot be called more than once  or an {@link IllegalStateException} will be thrown
 * <br/>
 * Also enforces the best practice of using an UncaughtExceptionHandler on each thread pool thread.  The default
 * uncaught exception handler simply logs the exception.
 * 
 * @author bwheeler
 */
public class NamedThreadFactory implements ThreadFactory
{
	private static final Log log = LogFactory.getLog(NamedThreadFactory.class);
	
	protected final AtomicInteger threadCount = new AtomicInteger();
	protected final AtomicReference<ThreadGroup> threadGroup = new AtomicReference<ThreadGroup>();
	protected final AtomicReference<Thread.UncaughtExceptionHandler> uncaughtExceptionHandler =
			new AtomicReference<Thread.UncaughtExceptionHandler>(new UncaughtExceptionHandler());
	
	public NamedThreadFactory()
	{
	}
	
	public NamedThreadFactory(String name)
	{
		setName(name);
	}
	
	@Override
	public Thread newThread(Runnable target)
	{
		ThreadGroup threadGroup = this.threadGroup.get();
		if (threadGroup == null)
			throw new IllegalStateException("not allowed to call newThread() before calling setName() on " + getClass().getSimpleName());

		log.debug("allocating new thread in " + getName());
		Thread thread = new Thread(threadGroup, target, threadGroup.getName() + "-" + threadCount.incrementAndGet());
		thread.setUncaughtExceptionHandler(uncaughtExceptionHandler.get());
		return thread;
	}

	public String getName()
	{
		ThreadGroup threadGroup = this.threadGroup.get();
		if (threadGroup == null)
			throw new IllegalStateException("not allowed to call getName() before calling setName() on " + getClass().getSimpleName());
		return threadGroup.getName();
	}

	@Required
	public void setName(String name)
	{
		ValidationUtils.assertNotNull(name, "name cannot be null");

		ThreadGroup threadGroup = new ThreadGroup(name);
		if (!this.threadGroup.compareAndSet(null, threadGroup))
			throw new IllegalStateException("not allowed to rename " + getClass().getSimpleName());
	}

	public ThreadGroup getThreadGroup()
	{
		ThreadGroup threadGroup = this.threadGroup.get();
		if (threadGroup == null)
			throw new IllegalStateException("not allowed to call getThreadGroup() before calling setName() on " + getClass().getSimpleName());
		return threadGroup;
	}

	public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler)
	{
		this.uncaughtExceptionHandler.set(uncaughtExceptionHandler);
	}

}
