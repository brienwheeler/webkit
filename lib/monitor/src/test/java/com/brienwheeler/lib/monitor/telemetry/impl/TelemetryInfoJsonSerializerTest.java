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

import junit.framework.Assert;

import org.junit.Test;

import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;

public class TelemetryInfoJsonSerializerTest
{
	private static final String NAME = "name";
	
	@Test
	public void testAlreadyDone()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME);
		Object processedVersion = new Object();
		telemetryInfo.setProcessedVersion(TelemetryInfoJsonSerializer.VERSION_NAME, processedVersion);
		telemetryInfo.publish();
		TelemetryInfoJsonSerializer telemetryInfoJsonSerializer = new TelemetryInfoJsonSerializer();
		telemetryInfoJsonSerializer.process(telemetryInfo);
		Assert.assertEquals(processedVersion, telemetryInfo.getProcessedVersion(TelemetryInfoJsonSerializer.VERSION_NAME));
	}

	@Test
	public void testProcess()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME);
		telemetryInfo.set("Boolean", true);
		telemetryInfo.set("Double", 1.0d);
		telemetryInfo.set("Float", 1.0f);
		telemetryInfo.set("Integer", 1);
		telemetryInfo.set("Long", 1L);
		telemetryInfo.set("Object", new Object());
		telemetryInfo.publish();
		Assert.assertNull(telemetryInfo.getProcessedVersion(TelemetryInfoJsonSerializer.VERSION_NAME));
		TelemetryInfoJsonSerializer telemetryInfoJsonSerializer = new TelemetryInfoJsonSerializer();
		telemetryInfoJsonSerializer.process(telemetryInfo);
		Assert.assertNotNull(telemetryInfo.getProcessedVersion(TelemetryInfoJsonSerializer.VERSION_NAME));
	}
}
