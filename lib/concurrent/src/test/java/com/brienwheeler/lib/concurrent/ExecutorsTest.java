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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import com.brienwheeler.lib.test.UtilsTestBase;

public class ExecutorsTest extends UtilsTestBase<Executors>
{
	private static final String THREAD_FACTORY_NAME = "TestThreadFactory";
	
	@Override
	protected Class<Executors> getUtilClass()
	{
		return Executors.class;
	}

	@Test
	public void testNewSingleThreadExecutorShutdownClean() throws InterruptedException
	{
		NamedThreadFactory threadFactory = new NamedThreadFactory(THREAD_FACTORY_NAME);
		ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
		Assert.assertFalse(executor.isShutdown());
		Assert.assertFalse(executor.isTerminated());
		
		executor.execute(new NullRunnable());

		executor.shutdown();
		Assert.assertTrue(executor.isShutdown());
		executor.awaitTermination(10, TimeUnit.MILLISECONDS);
		Assert.assertTrue(executor.isTerminated());
	}

	@Test
	public void testNewSingleThreadExecutorShutdownNow() throws InterruptedException
	{
		NamedThreadFactory threadFactory = new NamedThreadFactory(THREAD_FACTORY_NAME);
		ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
		
		executor.submit(new SleepRunnable(10L));
		Future<?> notExecutedRunnable = executor.submit(new NullRunnable());
		Future<?> notExecutedCallable = executor.submit(new NullCallable());
		Future<Integer> notEexecutedRunnable2 = executor.submit(new NullRunnable(), 1);

		List<Runnable> notExecuted = executor.shutdownNow();
		Assert.assertTrue(executor.isShutdown());
		Assert.assertEquals(3, notExecuted.size());
		Assert.assertTrue(CollectionUtils.containsInstance(notExecuted, notExecutedRunnable));
		Assert.assertTrue(CollectionUtils.containsInstance(notExecuted, notExecutedCallable));
		Assert.assertTrue(CollectionUtils.containsInstance(notExecuted, notEexecutedRunnable2));
		
		executor.awaitTermination(10, TimeUnit.MILLISECONDS);
		Assert.assertTrue(executor.isTerminated());
	}

	@Test
	public void testNewSingleThreadExecutorInvokeAll() throws InterruptedException, ExecutionException
	{
		NamedThreadFactory threadFactory = new NamedThreadFactory(THREAD_FACTORY_NAME);
		ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);

		IntCallable one = new IntCallable(1);
		IntCallable two = new IntCallable(2);
		ArrayList<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
		tasks.add(one);
		tasks.add(two);
		
		List<Future<Integer>> results = executor.invokeAll(tasks);
		Assert.assertEquals(2, results.size());
		Iterator<Future<Integer>> it = results.iterator();
		Future<Integer> oneResult = it.next();
		Future<Integer> twoResult = it.next();
		Assert.assertTrue(oneResult.isDone());
		Assert.assertEquals(1, oneResult.get().intValue());
		Assert.assertEquals(2, twoResult.get().intValue());
		
		results = executor.invokeAll(tasks, 10, TimeUnit.MILLISECONDS);
		Assert.assertEquals(2, results.size());
		it = results.iterator();
		oneResult = it.next();
		twoResult = it.next();
		Assert.assertTrue(oneResult.isDone());
		Assert.assertEquals(1, oneResult.get().intValue());
		Assert.assertEquals(2, twoResult.get().intValue());

		executor.shutdown();
	}

	@Test
	public void testNewSingleThreadExecutorInvokeAny() throws InterruptedException, ExecutionException,
			TimeoutException
	{
		NamedThreadFactory threadFactory = new NamedThreadFactory(THREAD_FACTORY_NAME);
		ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);

		IntCallable one = new IntCallable(1);
		IntCallable two = new IntCallable(2);
		ArrayList<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
		tasks.add(one);
		tasks.add(two);
		
		Integer result = executor.invokeAny(tasks);
		Assert.assertTrue(result == 1 || result == 2);
		
		result = executor.invokeAny(tasks, 10, TimeUnit.MILLISECONDS);
		Assert.assertTrue(result == 1 || result == 2);

		executor.shutdown();
	}

	@Test
	public void testNewSingleThreadScheduledExecutor()
	{
		NamedThreadFactory threadFactory = new NamedThreadFactory(THREAD_FACTORY_NAME);
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
		
		ScheduledFuture<?> future1 = executor.schedule(new NullRunnable(), 10, TimeUnit.MILLISECONDS);
		ScheduledFuture<Integer> future2 = executor.schedule(new IntCallable(1), 10, TimeUnit.MILLISECONDS);
		ScheduledFuture<?> future3 = executor.scheduleAtFixedRate(new NullRunnable(), 10, 10, TimeUnit.MILLISECONDS);
		ScheduledFuture<?> future4 = executor.scheduleWithFixedDelay(new NullRunnable(), 10, 10, TimeUnit.MILLISECONDS);
	
		List<Runnable> notRun = executor.shutdownNow();
		Assert.assertTrue(executor.isShutdown());
		Assert.assertEquals(4, notRun.size());
		Assert.assertTrue(CollectionUtils.containsInstance(notRun, future1));
		Assert.assertTrue(CollectionUtils.containsInstance(notRun, future2));
		Assert.assertTrue(CollectionUtils.containsInstance(notRun, future3));
		Assert.assertTrue(CollectionUtils.containsInstance(notRun, future4));
	}
	
	@Test
	public void testFinalize() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
	{
		NamedThreadFactory threadFactory = new NamedThreadFactory(THREAD_FACTORY_NAME);
		ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
		Assert.assertFalse(executor.isShutdown());
		Method finalize = executor.getClass().getDeclaredMethod("finalize");
		finalize.setAccessible(true);
		finalize.invoke(executor);
		Assert.assertTrue(executor.isShutdown());
	}
	
}
