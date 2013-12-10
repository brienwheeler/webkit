package com.brienwheeler.lib.util;

import java.util.concurrent.CopyOnWriteArrayList;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class ListenerSetTest
{
	private static final Log log = LogFactory.getLog(ListenerSetTest.class);

	@SuppressWarnings("unchecked")
	@Test
	public void testManageListeners()
	{
		ListenerSet<TestListenerInterface> listeners = new ListenerSet<TestListenerInterface>(log);
		CopyOnWriteArrayList<TestListenerInterface> internalList = (CopyOnWriteArrayList<TestListenerInterface>) ReflectionTestUtils.getField(listeners, "listeners");

		Assert.assertEquals(0, internalList.size());

		TestListenerInterface testListener1 = new TestListener();
		listeners.addListener(testListener1);
		Assert.assertEquals(1, internalList.size());
		
		TestListenerInterface testListener2 = new TestListener();
		listeners.addListener(testListener2);
		Assert.assertEquals(2, internalList.size());

		listeners.removeListener(testListener1);
		Assert.assertEquals(1, internalList.size());
		listeners.removeListener(testListener2);
		Assert.assertEquals(0, internalList.size());
		
		listeners.setListeners(CollectionUtils.makeList(testListener1, testListener2));
		Assert.assertEquals(2, internalList.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAddSameListenerTwice()
	{
		ListenerSet<TestListenerInterface> listeners = new ListenerSet<TestListenerInterface>(log);
		CopyOnWriteArrayList<TestListenerInterface> internalList = (CopyOnWriteArrayList<TestListenerInterface>) ReflectionTestUtils.getField(listeners, "listeners");
		
		Assert.assertEquals(0, internalList.size());

		TestListenerInterface testListener1 = new TestListener();
		listeners.addListener(testListener1);
		Assert.assertEquals(1, internalList.size());

		listeners.addListener(testListener1);
		Assert.assertEquals(1, internalList.size());
	}
	
	@Test
	public void testCallEmptySet()
	{
		ListenerSet<TestListenerInterface> listeners = new ListenerSet<TestListenerInterface>(log);
		listeners.callListenerMethod("methodVoid");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCallUnknownMethod()
	{
		ListenerSet<TestListenerInterface> listeners = new ListenerSet<TestListenerInterface>(log);
		TestListener listener = new TestListener();
		listeners.setListeners(CollectionUtils.makeList((TestListenerInterface) listener));

		listeners.callListenerMethod("noSuchMethod");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCallUnknownMethodArgCount()
	{
		ListenerSet<TestListenerInterface> listeners = new ListenerSet<TestListenerInterface>(log);
		TestListener listener = new TestListener();
		listeners.setListeners(CollectionUtils.makeList((TestListenerInterface) listener));

		listeners.callListenerMethod("methodVoid", 1);
	}

	@Test
	public void testCallUnknownMethodPrimitiveMismatch()
	{
		ListenerSet<TestListenerInterface> listeners = new ListenerSet<TestListenerInterface>(log);
		TestListener listener = new TestListener();
		listeners.setListeners(CollectionUtils.makeList((TestListenerInterface) listener));

		try {
			listeners.callListenerMethod("methodPrimitives", new Object(), 0.0D, 0.0F, 1, 1L);
			Assert.fail();
		}
		catch (IllegalArgumentException e) {
			// expected
		}
		try {
			listeners.callListenerMethod("methodPrimitives", true, new Object(), 0.0F, 1, 1L);
			Assert.fail();
		}
		catch (IllegalArgumentException e) {
			// expected
		}
		try {
			listeners.callListenerMethod("methodPrimitives", true, 0.0D, new Object(), 1, 1L);
			Assert.fail();
		}
		catch (IllegalArgumentException e) {
			// expected
		}
		try {
			listeners.callListenerMethod("methodPrimitives", true, 0.0D, 0.0F, new Object(), 1L);
			Assert.fail();
		}
		catch (IllegalArgumentException e) {
			// expected
		}
		try {
			listeners.callListenerMethod("methodPrimitives", true, 0.0D, 0.0F, 1, new Object());
			Assert.fail();
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCallUnknownMethodCached()
	{
		ListenerSet<TestListenerInterface> listeners = new ListenerSet<TestListenerInterface>(log);
		TestListener listener = new TestListener();
		listeners.setListeners(CollectionUtils.makeList((TestListenerInterface) listener));

		try {
			listeners.callListenerMethod("noSuchMethod");
			Assert.fail();
		}
		catch (IllegalArgumentException e) {
			// expected
		}

		listeners.callListenerMethod("noSuchMethod");
	}

	@Test
	public void testCallMethods()
	{
		ListenerSet<TestListenerInterface> listeners = new ListenerSet<TestListenerInterface>(log);
		
		// use multiple listeners
		TestListener listener1 = new TestListener();
		TestListener listener2 = new TestListener();
		listeners.setListeners(CollectionUtils.makeList((TestListenerInterface) listener1,
				listener2));
		
		listeners.callListenerMethod("methodVoid");
		Assert.assertEquals(1, listener1.voidCalls);
		Assert.assertEquals(1, listener2.voidCalls);

		listeners.callListenerMethod("methodInt", 20);
		Assert.assertEquals(1, listener1.intCalls);
		Assert.assertEquals(20, listener1.intValue);
		Assert.assertEquals(1, listener2.intCalls);
		Assert.assertEquals(20, listener2.intValue);
	
		listeners.callListenerMethod("methodObject", new TestArgumentObject());
		Assert.assertEquals(1, listener1.objectCalls);
		Assert.assertEquals(1, listener2.objectCalls);
		
		listeners.callListenerMethod("methodPrimitives", true, 0.0D, 0.0F, 1, 1L);
		Assert.assertEquals(1, listener1.primitivesCalls);
		Assert.assertEquals(1, listener2.primitivesCalls);
	}
	
	@Test
	public void testCallCachedMethod()
	{
		ListenerSet<TestListenerInterface> listeners = new ListenerSet<TestListenerInterface>(log);
		TestListener listener = new TestListener();
		listeners.setListeners(CollectionUtils.makeList((TestListenerInterface) listener));
		
		listeners.callListenerMethod("methodVoid");
		Assert.assertEquals(1, listener.voidCalls);

		listeners.callListenerMethod("methodVoid");
		Assert.assertEquals(2, listener.voidCalls);
	}
	
	private static abstract class TestArgumentObjectBase
	{
	}
	
	private static class TestArgumentObject extends TestArgumentObjectBase
	{
	}

	private static interface TestListenerInterface
	{
		void methodVoid();
		
		void methodInt(int arg0);
		
		void methodObject(TestArgumentObjectBase arg0);
		
		void methodPrimitives(boolean b, double d, float f, int i, long l);
	}
	
	private static class TestListener implements TestListenerInterface
	{
		public int voidCalls = 0;
		public int intCalls = 0;
		public int objectCalls = 0;
		public int primitivesCalls = 0;
		
		public int intValue = 0;
		
		@Override
		public void methodVoid()
		{
			voidCalls++;
		}

		@Override
		public void methodInt(int arg0)
		{
			intCalls++;
			intValue = arg0;
		}

		@Override
		public void methodObject(TestArgumentObjectBase arg0)
		{
			objectCalls++;
		}
		
		@Override
		public void methodPrimitives(boolean b, double d, float f, int i, long l)
		{
			primitivesCalls++;
		}
	}
}
