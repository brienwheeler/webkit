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
package com.brienwheeler.lib.monitor.telemetry.impl;

import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;

public class TelemetryInfoJsonLoggerTest
{
	private static final String NAME = "name";
	
	@Test
	public void testAlreadyFormatted()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME);
		telemetryInfo.setProcessedVersion(TelemetryInfoJsonSerializer.VERSION_NAME, new Object());
		telemetryInfo.publish();
		TelemetryInfoJsonLogger telemetryInfoJsonLogger = new TelemetryInfoJsonLogger();
		telemetryInfoJsonLogger.process(telemetryInfo);
	}

	@Test
	public void testFormatAndLogToDefault()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME);
		telemetryInfo.publish();
		TelemetryInfoJsonLogger telemetryInfoJsonLogger = new TelemetryInfoJsonLogger();
		telemetryInfoJsonLogger.process(telemetryInfo);
	}

	@Test
	public void testFormatAndLogToCustom()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, LogFactory.getLog(Object.class));
		telemetryInfo.publish();
		TelemetryInfoJsonLogger telemetryInfoJsonLogger = new TelemetryInfoJsonLogger();
		telemetryInfoJsonLogger.process(telemetryInfo);
	}
}
