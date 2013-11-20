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
package com.brienwheeler.svc.monitor.telemetry.impl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.brienwheeler.lib.io.ReconnectingSocket;
import com.brienwheeler.lib.io.TestListener;
import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;
import com.brienwheeler.lib.util.ValidationException;

public class GraphiteTelemetryInfoProcessorTest
{
	private static final Log log = LogFactory.getLog(GraphiteTelemetryInfoProcessorTest.class);
	
	private static final String INFO_NAME = "InfoName";
	private static final String ATTR_DOUBLE = "attrDouble";
	private static final String ATTR_FLOAT = "attrFloat";
	private static final String ATTR_INTEGER = "attrInteger";
	private static final String ATTR_LONG = "attrLong";
	private static final String ATTR_STRING = "attrString";
	
	@Test(expected = ValidationException.class)
	public void testSetHostnameNull()
	{
		GraphiteTelemetryInfoProcessor processor = new GraphiteTelemetryInfoProcessor();
		processor.setHostname(null);	
	}

	@Test(expected = ValidationException.class)
	public void testSetPortZero()
	{
		GraphiteTelemetryInfoProcessor processor = new GraphiteTelemetryInfoProcessor();
		processor.setPort(0);	
	}

	@Test(expected = ValidationException.class)
	public void testSetPortTooLarge()
	{
		GraphiteTelemetryInfoProcessor processor = new GraphiteTelemetryInfoProcessor();
		processor.setPort(65536);	
	}

	@Test
	public void testProcessNoHost()
	{
		GraphiteTelemetryInfoProcessor processor = new GraphiteTelemetryInfoProcessor();
		processor.start();

		// try to process with null internal socket
		TelemetryInfo info = new TelemetryInfo(INFO_NAME);
		info.publish();
		processor.process(info);

		processor.stop(1000L);
	}
	
	@Test
	public void testProcessNoServer() throws IOException
	{
		TestListener testListener = new TestListener();
		testListener.close();
		
		GraphiteTelemetryInfoProcessor processor = new GraphiteTelemetryInfoProcessor();
		processor.setHostname("localhost");
		processor.setPort(testListener.getListenPort());

		processor.start();

		// try to process with not connected internal socket
		TelemetryInfo info = new TelemetryInfo(INFO_NAME);
		info.publish();
		processor.process(info);

		processor.stop(1000L);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testProcess() throws IOException, InterruptedException
	{
		TestListener testListener = new TestListener();
		
		GraphiteTelemetryInfoProcessor processor = new GraphiteTelemetryInfoProcessor();
		processor.setHostname("localhost");
		processor.setPort(testListener.getListenPort());
		
		processor.start();
		testListener.accept(); // wait for connect
		
		AtomicReference<ReconnectingSocket> socket = (AtomicReference<ReconnectingSocket>)
				ReflectionTestUtils.getField(processor, "reconnectingSocket");
		while (!socket.get().isConnected())
			Thread.sleep(5L);
		
		try {
			TelemetryInfo info = new TelemetryInfo(INFO_NAME);
			
			info.set(ATTR_DOUBLE, 1.0d);
			info.set(ATTR_FLOAT, 1.0f);
			info.set(ATTR_INTEGER, 1);
			info.set(ATTR_LONG, 1L);
			info.set(ATTR_STRING, "1");
			
			info.publish();
			processor.process(info);
			
			log.info(testListener.readLine());
			log.info(testListener.readLine());
			log.info(testListener.readLine());
			log.info(testListener.readLine());
		}
		finally {
			processor.stop(1000L);
			testListener.close();
		}
	}
}
