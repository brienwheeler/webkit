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
package com.brienwheeler.lib.io;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.brienwheeler.lib.util.ValidationException;

public class ReconnectingSocketTest
{
	private static final Log log = LogFactory.getLog(ReconnectingSocketTest.class);
	
	@Test
	public void testConstruct1()
	{
		ReconnectingSocket socket = new ReconnectingSocket("localhost", 5000);
		try {
			Assert.assertNotNull(socket);
			Assert.assertFalse(socket.isConnected());
		}
		finally {
			socket.stop();
		}
	}
	
	@Test(expected = ValidationException.class)
	public void testConstruct1NullHost()
	{
		new ReconnectingSocket(null, 5000);
	}
	
	@Test(expected = ValidationException.class)
	public void testConstruct1PortLow()
	{
		new ReconnectingSocket("localhost", 0);
	}
	
	@Test(expected = ValidationException.class)
	public void testConstruct1PortHigh()
	{
		new ReconnectingSocket("localhost", 65536);
	}
	
	@Test
	public void testConstruct2()
	{
		ReconnectingSocket socket = new ReconnectingSocket("localhost", 5000, log);
		Assert.assertNotNull(socket);
		socket.stop();
	}
	
	@Test(expected = ValidationException.class)
	public void testConstruct2NullHost()
	{
		new ReconnectingSocket(null, 5000, log);
	}
	
	@Test(expected = ValidationException.class)
	public void testConstruct2PortLow()
	{
		new ReconnectingSocket("localhost", 0, log);
	}
	
	@Test(expected = ValidationException.class)
	public void testConstruct2PortHigh()
	{
		new ReconnectingSocket("localhost", 65536, log);
	}
	
	@Test(expected = ValidationException.class)
	public void testConstruct2NullLog()
	{
		new ReconnectingSocket("localhost", 5000, null);
	}

	@Test
	public void testSetReconnectPeriodicity()
	{
		ReconnectingSocket socket = new ReconnectingSocket("localhost", 5000);
		try {
			Assert.assertFalse(1000L == (Long) ReflectionTestUtils.getField(socket, "reconnectPeriodicity"));
			socket.setReconnectPeriodicity(1000L);
			Assert.assertTrue(1000L == (Long) ReflectionTestUtils.getField(socket, "reconnectPeriodicity"));
		}
		finally {
			socket.stop();
		}
	}

	@Test
	public void testConnectAndMultipleStop() throws Exception
	{
		TestListener listener = null;
		TestReconnectingSocket socket = null;
		try {
			listener = new TestListener();
			socket = new TestReconnectingSocket("localhost", listener.getListenPort());
			Assert.assertNull(getConnectThread(socket));
			Assert.assertFalse(socket.isConnected());

			socket.start();
			Assert.assertNotNull(getConnectThread(socket));
			Assert.assertFalse(socket.isConnected());
			
			socket.waitForEvent();
			Assert.assertNull(getConnectThread(socket));
			Assert.assertTrue(socket.isConnected());
			
			socket.stop();
			Assert.assertNull(getConnectThread(socket));
			Assert.assertFalse(socket.isConnected());
			
			socket.stop();
			Assert.assertNull(getConnectThread(socket));
			Assert.assertFalse(socket.isConnected());
		}
		finally {
			if (socket != null)
				socket.stop();
			if (listener != null)
				listener.close();
		}
	}
		
	@Test
	public void testKeepReconnecting() throws Exception
	{
		TestListener listener = null;
		TestReconnectingSocket socket = null;
		try {
			listener = new TestListener();
			listener.close();
			socket = new TestReconnectingSocket("localhost", listener.getListenPort());
			socket.setReconnectPeriodicity(50L);
			Assert.assertEquals(0, socket.getConnectFail());
			socket.start();
			
			socket.waitForEvent(); // should be fail
			Assert.assertEquals(1, socket.getConnectFail());		
			socket.start(); // this covers a line that refuses to start another thread when one already exists trying to connect
			
			socket.waitForEvent(); // should be fail
			Assert.assertEquals(2, socket.getConnectFail());
			
			listener.reopen();
			socket.waitForEvent(); // should be connect
			Assert.assertTrue(socket.isConnected());
		}
		finally {
			if (socket != null)
				socket.stop();
			if (listener != null)
				listener.close();
		}
	}
	
	@Test
	public void testStopInReconnectWait() throws Exception
	{
		TestListener listener = null;
		TestReconnectingSocket socket = null;
		try {
			listener = new TestListener();
			listener.close();
			socket = new TestReconnectingSocket("localhost", listener.getListenPort());
			socket.setReconnectPeriodicity(50L);
			Assert.assertEquals(0, socket.getConnectFail());
			socket.start();
			
			socket.waitForEvent(); // should be fail
			Assert.assertEquals(1, socket.getConnectFail());	
			ReconnectingSocket.ConnectThread thread = getConnectThread(socket);
			Assert.assertNotNull(thread);
			Assert.assertTrue(thread.isAlive());
			socket.stop(); // this shuts down background thread
			Assert.assertFalse(thread.isAlive());
		}
		finally {
			if (socket != null)
				socket.stop();
			if (listener != null)
				listener.close();
		}
	}

	@Test
	public void testTryToWrite() throws Exception
	{
		TestListener listener = null;
		TestReconnectingSocket socket = null;
		try {
			listener = new TestListener();
			socket = new TestReconnectingSocket("localhost", listener.getListenPort());
			socket.setReconnectPeriodicity(50L);
			socket.start();
			
			socket.waitForEvent(); // should be connect
			listener.accept();
			socket.write("test\n");
			Assert.assertEquals("test", listener.readLine());
		}
		finally {
			if (socket != null)
				socket.stop();
			if (listener != null)
				listener.close();
		}
	}

	@Test
	public void testTryToWriteFail() throws Exception
	{
		TestListener listener = null;
		TestReconnectingSocket socket = null;
		try {
			listener = new TestListener();
			log.info("port " + listener.getListenPort());
			socket = new TestReconnectingSocket("localhost", listener.getListenPort());
			socket.setReconnectPeriodicity(50L);
			socket.start();
			
			socket.waitForEvent(); // should be connect
			listener.accept();
			Assert.assertEquals(0, socket.getDisconnects());
			
			log.info("closing client");
			listener.closeClient();
			log.info("writing");
			for (int i=0; i<500; i++) {
				socket.write("test\n");
				if (!socket.isConnected())
					break;
			}
			Assert.assertEquals(1, socket.getDisconnects());
			
			socket.write("test"); // silent discard when not connected
		
			socket.waitForEvent(); // should be connect
			listener.accept();
			socket.write("test\n");
			Assert.assertEquals("test", listener.readLine());
		}
		finally {
			if (socket != null)
				socket.stop();
			if (listener != null)
				listener.close();
		}
	}

	@SuppressWarnings("unchecked")
	private ReconnectingSocket.ConnectThread getConnectThread(ReconnectingSocket socket)
	{
		return ((AtomicReference<ReconnectingSocket.ConnectThread>) ReflectionTestUtils.getField(socket, "connectThread")).get();
	}
	
	private static class TestReconnectingSocket extends ReconnectingSocket
	{
		private int connectFail = 0;
		private int disconnects = 0;
		private final long timeout = 5000L;
		private final Object syncObj = new Object();
		
		public TestReconnectingSocket(String hostname, int port, Log log)
		{
			super(hostname, port, log);
		}

		public TestReconnectingSocket(String hostname, int port)
		{
			super(hostname, port);
		}
		
		public void waitForEvent() throws InterruptedException
		{
			synchronized (syncObj) {
				syncObj.wait(timeout);
			}
		}

		public int getConnectFail()
		{
			return connectFail;
		}
		
		public int getDisconnects()
		{
			return disconnects;
		}

		@Override
		protected void onConnected()
		{
			synchronized (syncObj) {
				syncObj.notifyAll();
			}
		}
		
		@Override
		protected void onDisconnected()
		{
			disconnects++;
		}
		
		@Override
		protected void onConnectFail()
		{
			connectFail++;
			synchronized (syncObj) {
				syncObj.notifyAll();
			}
		}		
	}
}
