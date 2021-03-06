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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class ThreadPoolExecutorTest
{
	private static final String NAME = "TestThreadFactory";
	
	@Test
	public void testConstruct1()
	{
		ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(NAME));
		Assert.assertFalse(executor.isShutdown());
		executor.shutdown();
		Assert.assertTrue(executor.isShutdown());
	}
	
	@Test
	public void testConstruct2()
	{
		ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(NAME),
				new NullRejectedExecutionHandler());
		Assert.assertFalse(executor.isShutdown());
		executor.shutdown();
		Assert.assertTrue(executor.isShutdown());
	}
	
	@Test
	public void testSetThreadFactory()
	{
		ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(NAME));
		NamedThreadFactory threadFactory = new NamedThreadFactory(NAME);
		executor.setThreadFactory(threadFactory);
		Assert.assertSame(threadFactory, executor.getThreadFactory());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetThreadFactoryFail()
	{
		ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(NAME));
		executor.setThreadFactory(new NotNamedThreadFactory());
	}
}
