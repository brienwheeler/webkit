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

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import com.brienwheeler.lib.io.ReconnectingSocket;
import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;
import com.brienwheeler.lib.util.ValidationUtils;

public class GraphiteTelemetryInfoProcessor extends StoppableTelemetryInfoProcessorBase
{
	private static final Log log = LogFactory.getLog(GraphiteTelemetryInfoProcessor.class);
	
	private String hostname = "";
	private int port = 2003;
	private final AtomicReference<ReconnectingSocket> reconnectingSocket = new AtomicReference<ReconnectingSocket>();
	
	@Required
	public void setHostname(String hostname)
	{
		// allowed to be empty string to disable graphite integration
		ValidationUtils.assertNotNull(hostname, "hostname cannot be null");
		this.hostname = hostname.trim();
	}

	@Required
	public void setPort(int port)
	{
		ValidationUtils.assertTrue(port > 0, "port must be greater than 0");
		ValidationUtils.assertTrue(port < 65536, "port must be less than 65536");
		this.port = port;
	}

	@Override
	public void onStart()
	{
		if (!hostname.isEmpty()) {
			ReconnectingSocket reconnectingSocket = new ReconnectingSocket(hostname, port, log);
			reconnectingSocket.start();
			this.reconnectingSocket.set(reconnectingSocket);
		}
	}

	@Override
	public void onStop(long stopGracePeriod)
	{
		ReconnectingSocket socket = reconnectingSocket.getAndSet(null);
		if (socket != null)
			socket.stop();
	}
	
	@Override
	public void process(TelemetryInfo telemetryInfo)
	{
		ReconnectingSocket socket = reconnectingSocket.get();
		if (socket == null || !socket.isConnected())
			return;

		String name = telemetryInfo.getName();
		long createdAt = telemetryInfo.getCreatedAt() / 1000; // Graphite uses UNIX epoch

		StringBuffer data = new StringBuffer(256);
		for (String attrName : telemetryInfo.getAttributeNames()) {
			if (attrName.equals(TelemetryInfo.ATTR_NAME) || attrName.equals(TelemetryInfo.ATTR_CREATED_AT))
				continue;
			
			Object attrValue = telemetryInfo.get(attrName);
			if (attrValue instanceof Double || attrValue instanceof Float || 
					attrValue instanceof Integer || attrValue instanceof Long) {
				data.setLength(0);
				data.append(name).append(".").append(attrName);
				data.append(" ").append(attrValue.toString());
				data.append(" ").append(createdAt).append("\n");
				String dataStr = data.toString();
				socket.write(dataStr);
				log.debug(dataStr);
			}
		}
	}

}