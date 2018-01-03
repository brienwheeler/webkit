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
package com.brienwheeler.lib.concurrent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

public class StoppableThreadTest
{
	private static final Log log = LogFactory.getLog(StoppableThreadTest.class);
	private static final String NAME = "TestThread";
	
	@Test
	public void testConstruct1() throws InterruptedException
	{
		StoppableThread thread = new NullStoppableThread(NAME);
		Assert.assertFalse(thread.isAlive());
	}
	
	@Test
	public void testConstruct2() throws InterruptedException
	{
		StoppableThread thread = new NullStoppableThread(NAME, log);
		Assert.assertFalse(thread.isAlive());
	}
	
	@Test
	public void testShutdownForever() throws InterruptedException
	{
		StoppableThread thread = new NullStoppableThread(NAME);
		Assert.assertFalse(thread.isAlive());
		Assert.assertTrue(thread.isDaemon());
		thread.start();
		Assert.assertTrue(thread.isAlive());
		thread.shutdown();
		Assert.assertFalse(thread.isAlive());
	}
	
	@Test
	public void testShutdownTimeout() throws InterruptedException
	{
		StoppableThread thread = new NullStoppableThread(NAME, log);
		Assert.assertFalse(thread.isAlive());
		Assert.assertTrue(thread.isDaemon());
		thread.start();
		Assert.assertTrue(thread.isAlive());
		thread.shutdown(10L);
		Assert.assertFalse(thread.isAlive());
	}
	
	private static class NullStoppableThread extends StoppableThread
	{
		public NullStoppableThread(String name)
		{
			super(name);
		}
		
		public NullStoppableThread(String name, Log log)
		{
			super(name, log);
		}

		@Override
		protected void onRun()
		{
			while (!isShutdown()) {
				try {
					Thread.sleep(Long.MAX_VALUE);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
