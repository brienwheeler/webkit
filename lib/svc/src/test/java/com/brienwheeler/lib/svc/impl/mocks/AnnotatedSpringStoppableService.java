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
package com.brienwheeler.lib.svc.impl.mocks;

import com.brienwheeler.lib.monitor.work.MonitoredWork;
import com.brienwheeler.lib.svc.GracefulShutdown;
import com.brienwheeler.lib.svc.impl.SpringStoppableServiceBase;

public class AnnotatedSpringStoppableService extends SpringStoppableServiceBase 
{
    public static final String WORK_NAME = "testWork";

	@GracefulShutdown
	public void testMethodGracefulShutdown()
	{
	}

	@GracefulShutdown
	public void testMethodGracefulShutdownInterrupted() throws InterruptedException
	{
		throw new InterruptedException();
	}
	
	@GracefulShutdown
	public void testMethodGracefulShutdownRuntime()
	{
		throw new RuntimeException();
	}
	
	@GracefulShutdown
	public void testMethodGracefulShutdownError()
	{
		throw new Error();
	}
	
	@GracefulShutdown
	public void testMethodGracefulShutdownThrowable() throws Throwable
	{
		throw new Throwable();
	}

    @MonitoredWork(value=WORK_NAME)
    public void testMethodWorkName(long sleep)
    {
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                // unexpected, throw so test fails
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    @MonitoredWork(value=WORK_NAME)
    public void testMethodWorkNameRuntimeException(long sleep)
    {
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                // return without throwing so test fails
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new RuntimeException("test");
    }

    @MonitoredWork(value=WORK_NAME)
    public void testMethodWorkNameError(long sleep)
    {
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                // return without throwing so test fails
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new Error("test");
    }

    @MonitoredWork(value=MonitoredWork.NO_NAME)
    public void testMethodWorkNoName(long sleep)
    {
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                // unexpected, throw so test fails
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    @MonitoredWork(value=MonitoredWork.NO_NAME)
    public void testMethodWorkNoName2(long sleep)
    {
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                // unexpected, throw so test fails
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }
}
