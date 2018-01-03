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

import junit.framework.Assert;

import org.junit.Test;

import com.brienwheeler.lib.svc.ServiceStateException;
import com.brienwheeler.lib.svc.impl.mocks.AnnotatedSpringStoppableService;

public class GracefulShutdownAspectTest
{
	@Test(expected = ServiceStateException.class)
	public void testNotStarted()
	{
		AnnotatedSpringStoppableService service = new AnnotatedSpringStoppableService();
		service.testMethodGracefulShutdown();
	}

	@Test(expected = ServiceStateException.class)
	public void testInterrupted() throws InterruptedException
	{
		AnnotatedSpringStoppableService service = new AnnotatedSpringStoppableService();
		service.start();

		try {
			service.testMethodGracefulShutdownInterrupted();
			Assert.fail();
		}
		catch (ServiceStateException e)
		{
			// test but also clear interrupted status because on Linux
			// the test thread will remain in interrupted state, causing subsequent
			// tests to fail
			Assert.assertTrue(Thread.currentThread().interrupted());
			throw e;
		}
	}

	@Test(expected = RuntimeException.class)
	public void testRuntimeException()
	{
		AnnotatedSpringStoppableService service = new AnnotatedSpringStoppableService();
		service.start();
		
		service.testMethodGracefulShutdownRuntime();
	}

	@Test(expected = Error.class)
	public void testError()
	{
		AnnotatedSpringStoppableService service = new AnnotatedSpringStoppableService();
		service.start();
		
		service.testMethodGracefulShutdownError();
	}

	@Test(expected = RuntimeException.class)
	public void testThrowable() throws Throwable
	{
		AnnotatedSpringStoppableService service = new AnnotatedSpringStoppableService();
		service.start();
		
		service.testMethodGracefulShutdownThrowable();
	}
}
