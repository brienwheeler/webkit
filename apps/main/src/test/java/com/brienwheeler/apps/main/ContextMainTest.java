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
package com.brienwheeler.apps.main;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import com.brienwheeler.lib.test.spring.beans.PropertiesTestUtils;

public class ContextMainTest
{
	private static final String C = "-c";
	private static final String M = "-m";
	private static final String N = "-n";
	private static final String P = "-p";
	private static final String BAD_OPT = "-X";
	
	@Test
	public void testMain()
	{
		ContextMain.main(new String[0]);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testBadOpt()
	{
		ContextMain main = new ContextMain(new String[] { BAD_OPT });
		main.run();
	}
	
	@Test
	public void testSuperArgProcessing() throws InterruptedException
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		
		ContextMain main = new ContextMain(new String[] { P, TestDataConstants.PROPS_FILE1 });
		main.setUseBaseOpts(true);
		main.run();

		Assert.assertEquals(TestDataConstants.PROPS_FILE1_VALUE, System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
	}
	
	@Test
	public void testShutdownCommand() throws InterruptedException
	{
		CountDownLatch latch = new CountDownLatch(1);
		TestContextMain main = new TestContextMain(new String[] { N, C, TestDataConstants.RMAP_CTX_CLASSPATH }, latch);
		TestRunner testRunner = new TestRunner(main);
		testRunner.start();
		
		latch.await();

		assertLaunchCount(main, 1);

		main.shutdown();
		testRunner.join();
	}
	
	@Test
	public void testShutdownInternal() throws InterruptedException
	{
		CountDownLatch latch = new CountDownLatch(1);
		TestContextMain main = new TestContextMain(new String[] { N, C, TestDataConstants.RMAP_CTX_CLASSPATH }, latch);
		TestRunner testRunner = new TestRunner(main);
		testRunner.start();
		
		latch.await();
		
		assertLaunchCount(main, 1);
		getLaunchedContext(main, 0).close();
		
		testRunner.join();
	}

	@Test
	public void testContextDefault() throws InterruptedException
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		System.clearProperty(TestDataConstants.RMAP_TEST_PROP);
		Assert.assertNull(System.getProperty(TestDataConstants.RMAP_TEST_PROP));
		
		ContextMain main = new ContextMain(new String[] { });
		main.run();
		
		Assert.assertEquals(TestDataConstants.RMAP_TEST_VAL, System.getProperty(TestDataConstants.RMAP_TEST_PROP));
	}
	
	@Test
	public void testContextDefaultNotGiven() throws InterruptedException
	{
		ContextMain main = new ContextMain(new String[] { M, TestDataConstants.RMAP2_LOCATION });
		main.run();
		
		assertLaunchCount(main, 0);
	}
	
	@Test
	public void testContextDirect() throws InterruptedException
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		System.clearProperty(TestDataConstants.RMAP_TEST_PROP);
		Assert.assertNull(System.getProperty(TestDataConstants.RMAP_TEST_PROP));
		
		CountDownLatch latch = new CountDownLatch(1);
		TestContextMain main = new TestContextMain(new String[] { C, TestDataConstants.RMAP_CTX_DIRECT }, latch);
		TestRunner testRunner = new TestRunner(main);
		testRunner.start();
		
		latch.await();

		Assert.assertEquals(TestDataConstants.RMAP_TEST_VAL, System.getProperty(TestDataConstants.RMAP_TEST_PROP));
		assertLaunchCount(main, 1);

		main.shutdown();
		testRunner.join();
	}
	
	@Test
	public void testContextIndirect() throws InterruptedException
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.RMAP_TEST_PROP));
		
		CountDownLatch latch = new CountDownLatch(1);
		TestContextMain main = new TestContextMain(new String[] { C, TestDataConstants.RMAP_CTX_INDIRECT }, latch);
		TestRunner testRunner = new TestRunner(main);
		testRunner.start();
		
		latch.await();

		Assert.assertEquals(TestDataConstants.RMAP_TEST_VAL, System.getProperty(TestDataConstants.RMAP_TEST_PROP));
		assertLaunchCount(main, 1);

		main.shutdown();
		testRunner.join();
	}

	@Test
	public void testContextReuse() throws InterruptedException
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.RMAP_TEST_PROP));
		
		CountDownLatch latch = new CountDownLatch(1);
		TestContextMain main = new TestContextMain(new String[] { C, TestDataConstants.RMAP_CTX_DIRECT, C, TestDataConstants.RMAP_CTX_INDIRECT }, latch);
		TestRunner testRunner = new TestRunner(main);
		testRunner.start();
		
		latch.await();

		Assert.assertEquals(TestDataConstants.RMAP_TEST_VAL, System.getProperty(TestDataConstants.RMAP_TEST_PROP));
		assertLaunchCount(main, 1);

		main.shutdown();
		testRunner.join();
	}

	@Test
	public void testContextList() throws InterruptedException
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.RMAP_TEST_PROP));
		
		CountDownLatch latch = new CountDownLatch(1);
		TestContextMain main = new TestContextMain(new String[] { C, TestDataConstants.RMAP_CTX_LIST }, latch);
		TestRunner testRunner = new TestRunner(main);
		testRunner.start();
		
		latch.await();

		Assert.assertEquals(TestDataConstants.RMAP_TEST_VAL, System.getProperty(TestDataConstants.RMAP_TEST_PROP));
		assertLaunchCount(main, 2);

		main.shutdown();
		testRunner.join();
	}

	@Test(expected = ResourceMapError.class)
	public void testContextCircular() throws InterruptedException
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.RMAP_TEST_PROP));
		
		ContextMain main = new ContextMain(new String[] { C, TestDataConstants.RMAP_CTX_CIRCULAR });
		main.run();
	}

	@Test(expected = ResourceMapError.class)
	public void testContextBadPropSpec() throws InterruptedException
	{
		ContextMain main = new ContextMain(new String[] { C, TestDataConstants.RMAP_CTX_BADPROPSPEC });
		main.run();
		assertLaunchCount(main, 0);
	}

	@Test
	public void testBadResourceMapBean() throws InterruptedException
	{
		ContextMain main = new ContextMain(new String[] { M, TestDataConstants.RMAP3_LOCATION });
		main.run();
		
		assertLaunchCount(main, 0);
	}

	@Test
	public void testPropsDirect() throws InterruptedException
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));

		ContextMain main = new ContextMain(new String[] { P, TestDataConstants.RMAP_PROP_DIRECT });
		main.run();
		
		Assert.assertEquals(TestDataConstants.PROPS_FILE1_VALUE, System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
	}

	@Test
	public void testPropsIndirect()
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));

		ContextMain main = new ContextMain(new String[] { P, TestDataConstants.RMAP_PROP_INDIRECT });
		main.run();
		
		Assert.assertEquals(TestDataConstants.PROPS_FILE1_VALUE, System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
	}

	@Test
	public void testPropsReuse()
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));

		ContextMain main = new ContextMain(new String[] { P, TestDataConstants.RMAP_PROP_DIRECT, P, TestDataConstants.RMAP_PROP_INDIRECT });
		main.run();
		
		Assert.assertEquals(TestDataConstants.PROPS_FILE1_VALUE, System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
	}

	@Test
	public void testPropsList()
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE2_PROP));

		ContextMain main = new ContextMain(new String[] { P, TestDataConstants.RMAP_PROP_LIST });
		main.run();
		
		Assert.assertEquals(TestDataConstants.PROPS_FILE1_VALUE, System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		Assert.assertEquals(TestDataConstants.PROPS_FILE2_VALUE, System.getProperty(TestDataConstants.PROPS_FILE2_PROP));
	}

	@Test(expected = ResourceMapError.class)
	public void testPropsCircular()
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE2_PROP));

		ContextMain main = new ContextMain(new String[] { P, TestDataConstants.RMAP_PROP_CIRCULAR });
		main.run();
	}

	@Test
	public void testContextWithProps() throws InterruptedException
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE2_PROP));

		CountDownLatch latch = new CountDownLatch(1);
		TestContextMain main = new TestContextMain(new String[] { C, TestDataConstants.RMAP_CTX_WITH_PROPS }, latch);
		TestRunner testRunner = new TestRunner(main);
		testRunner.start();
		
		latch.await();
		
		assertLaunchCount(main, 1);
		getLaunchedContext(main, 0).close();
		
		testRunner.join();
	
		Assert.assertEquals(TestDataConstants.PROPS_FILE1_VALUE, System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		Assert.assertEquals(TestDataConstants.PROPS_FILE2_VALUE, System.getProperty(TestDataConstants.PROPS_FILE2_PROP));
	}

	@Test
	public void testContextWithProps2() throws InterruptedException
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE2_PROP));

		CountDownLatch latch = new CountDownLatch(1);
		TestContextMain main = new TestContextMain(new String[] { C, TestDataConstants.RMAP_CTX_WITH_PROPS2 }, latch);
		TestRunner testRunner = new TestRunner(main);
		testRunner.start();
		
		latch.await();
		
		assertLaunchCount(main, 2);

		main.shutdown();
		testRunner.join();
	
		Assert.assertEquals(TestDataConstants.PROPS_FILE1_VALUE, System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		Assert.assertEquals(TestDataConstants.PROPS_FILE2_VALUE, System.getProperty(TestDataConstants.PROPS_FILE2_PROP));
	}

	@Test
	public void testContextWithProps3() throws InterruptedException
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE2_PROP));

		CountDownLatch latch = new CountDownLatch(1);
		TestContextMain main = new TestContextMain(new String[] { C, TestDataConstants.RMAP_CTX_WITH_PROPS3 }, latch);
		TestRunner testRunner = new TestRunner(main);
		testRunner.start();
		
		latch.await();
		
		assertLaunchCount(main, 2);

		main.shutdown();
		testRunner.join();
	
		Assert.assertEquals(TestDataConstants.PROPS_FILE1_VALUE, System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		Assert.assertEquals(TestDataConstants.PROPS_FILE2_VALUE, System.getProperty(TestDataConstants.PROPS_FILE2_PROP));
	}

	@Test
	public void testContextWithProps4() throws InterruptedException
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE2_PROP));

		CountDownLatch latch = new CountDownLatch(1);
		TestContextMain main = new TestContextMain(new String[] { C, TestDataConstants.RMAP_CTX_WITH_PROPS4 }, latch);
		TestRunner testRunner = new TestRunner(main);
		testRunner.start();
		
		latch.await();
		
		assertLaunchCount(main, 1);

		main.shutdown();
		testRunner.join();
	
		Assert.assertEquals(TestDataConstants.PROPS_FILE1_VALUE, System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		Assert.assertEquals(TestDataConstants.PROPS_FILE2_VALUE, System.getProperty(TestDataConstants.PROPS_FILE2_PROP));
	}

	@Test
	public void testContextWithNoPropsMap() throws InterruptedException
	{
		PropertiesTestUtils.clearAllTestSystemProperties();
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));

		CountDownLatch latch = new CountDownLatch(1);
		TestContextMain main = new TestContextMain(new String[] { M, TestDataConstants.RMAP2_LOCATION, C, TestDataConstants.RMAP_CTX_WITH_PROPS }, latch);
		TestRunner testRunner = new TestRunner(main);
		testRunner.start();
		
		latch.await();
		
		assertLaunchCount(main, 1);
		
		main.shutdown();
		testRunner.join();
	
		Assert.assertEquals(TestDataConstants.PROPS_FILE1_VALUE, System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
	}


	@SuppressWarnings("unchecked")
	private void assertLaunchCount(ContextMain main, int count)
	{
		List<AbstractApplicationContext> contextList =
				(List<AbstractApplicationContext>) ReflectionTestUtils.getField(main, "contextLaunchOrder");
		Assert.assertEquals(count, contextList.size());
	}
	
	@SuppressWarnings("unchecked")
	private AbstractApplicationContext getLaunchedContext(ContextMain main, int index)
	{
		List<AbstractApplicationContext> contextList =
				(List<AbstractApplicationContext>) ReflectionTestUtils.getField(main, "contextLaunchOrder");
		return contextList.get(index);
	}
	
	private static class TestRunner extends Thread
	{
		private final TestContextMain main;
		
		public TestRunner(TestContextMain main) {
			this.main = main;
		}

		@Override
		public void run()
		{
			main.run();
		}
		
	}
	
	private static class TestContextMain extends ContextMain
	{
		private final CountDownLatch latch;
		
		public TestContextMain(String[] args, CountDownLatch latch)
		{
			super(args);
			this.latch = latch;
		}

		@Override
		protected void onWaitingForShutdown()
		{
			// release test main thread
			latch.countDown();
		}
		
	}
}
