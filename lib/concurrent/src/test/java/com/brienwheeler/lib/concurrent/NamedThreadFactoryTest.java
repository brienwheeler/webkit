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

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class NamedThreadFactoryTest
{
	private static final String NAME = "TestThreadFactory";
	
	@Test
	public void testConstruct()
	{
		NamedThreadFactory factory = new NamedThreadFactory();
		Assert.assertNotNull(factory);
	}
	
	@Test
	public void testConstruct2()
	{
		NamedThreadFactory factory = new NamedThreadFactory(NAME);
		Assert.assertNotNull(factory);
		Assert.assertEquals(NAME, factory.getName());
	}
	
	@Test
	public void testNewThread()
	{
		NamedThreadFactory factory = new NamedThreadFactory();
		factory.setName(NAME);
		Thread thread = factory.newThread(new NullRunnable());
		Assert.assertEquals(NAME + "-1", thread.getName());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testNewThreadNotNamed()
	{
		NamedThreadFactory factory = new NamedThreadFactory();
		factory.newThread(new NullRunnable());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNameFail()
	{
		NamedThreadFactory factory = new NamedThreadFactory();
		factory.getName();
	}
	
	@Test
	public void testSetName()
	{
		NamedThreadFactory factory = new NamedThreadFactory();
		factory.setName(NAME);
		Assert.assertEquals(NAME, factory.getName());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testSetNameFailReset()
	{
		NamedThreadFactory factory = new NamedThreadFactory();
		factory.setName(NAME);
		factory.setName(NAME);
	}

	@Test
	public void testGetThreadGroup()
	{
		NamedThreadFactory factory = new NamedThreadFactory();
		factory.setName(NAME);
		factory.getThreadGroup();
	}

	@Test(expected = IllegalStateException.class)
	public void testGetThreadGroupNotNamed()
	{
		NamedThreadFactory factory = new NamedThreadFactory();
		factory.getThreadGroup();
	}

	@Test
	public void testTwoThreadsSameGroup()
	{
		NamedThreadFactory factory = new NamedThreadFactory();
		factory.setName(NAME);
		Thread thread1 = factory.newThread(new NullRunnable());
		Assert.assertEquals(NAME + "-1", thread1.getName());
		Thread thread2 = factory.newThread(new NullRunnable());
		Assert.assertEquals(NAME + "-2", thread2.getName());
		Assert.assertEquals(thread1.getThreadGroup(), thread2.getThreadGroup());
	}
	
	@Test
	public void testTwoFactoriesSameName()
	{
		NamedThreadFactory factory1 = new NamedThreadFactory();
		factory1.setName(NAME);
		Thread thread1 = factory1.newThread(new NullRunnable());
		Assert.assertEquals(NAME + "-1", thread1.getName());
		
		NamedThreadFactory factory2 = new NamedThreadFactory();
		factory2.setName(NAME);
		Thread thread2 = factory2.newThread(new NullRunnable());
		Assert.assertEquals(NAME + "-1", thread2.getName());
		
		Assert.assertNotSame(thread1.getThreadGroup(), thread2.getThreadGroup());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSetUncaughtExceptionHandler()
	{
		NamedThreadFactory factory = new NamedThreadFactory();
		UncaughtExceptionHandler handler = new UncaughtExceptionHandler();
		factory.setUncaughtExceptionHandler(handler);
		AtomicReference<Thread.UncaughtExceptionHandler> ref = (AtomicReference<Thread.UncaughtExceptionHandler>)
				ReflectionTestUtils.getField(factory, "uncaughtExceptionHandler");
		Assert.assertSame(handler, ref.get());
	}
}
