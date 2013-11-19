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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.brienwheeler.lib.util.ObjectUtils;

/**
 * Extension of Thread that logs thread start/stop and includes a shutdown flag that subclasses can poll within
 * their onRun() method to detect if they are supposed to exit.
 * 
 * When shutdown() is called, the shutdown flag is set to true and the thread is interrupted.
 * 
 * @author bwheeler
 */
public abstract class StoppableThread extends Thread
{
	private final Log log;
	private final AtomicBoolean isShutdown = new AtomicBoolean(false);
	
	protected abstract void onRun();
	
	public StoppableThread(String name)
	{
		this(name, null);
	}
	
	public StoppableThread(String name, Log log)
	{
		super(name);
		setDaemon(true);
		if (log != null)
			this.log = log;
		else
			this.log = LogFactory.getLog(getClass());
	}
	
	public void shutdown(long maxTimeout) throws InterruptedException
	{
		isShutdown.set(true);
		interrupt();
		join(maxTimeout);
	}
	
	public void shutdown() throws InterruptedException
	{
		shutdown(Long.MAX_VALUE);
	}

	public boolean isShutdown()
	{
		return isShutdown.get();
	}

	@Override
	public final void run()
	{
		log.info(ObjectUtils.getUniqueId(this) + " starting");
		try {
			onRun();
		}
		finally {
			log.info(ObjectUtils.getUniqueId(this) + " exiting");
		}
	}
	
	
}
