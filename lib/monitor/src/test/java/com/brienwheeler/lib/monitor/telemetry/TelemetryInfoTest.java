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
package com.brienwheeler.lib.monitor.telemetry;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import com.brienwheeler.lib.util.ValidationException;

public class TelemetryInfoTest
{
	private static final String NAME = "name";
	private static final String ATTR = "attr";
	private static final String VALUE = "value";
	private static final String VERSION_NAME = "versionName";

	private static final Log log = LogFactory.getLog(TelemetryInfoTest.class);
	
	@Test
	public void testConstruct1()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME);
		Assert.assertEquals(NAME, telemetryInfo.getName());
		Assert.assertNull(telemetryInfo.getLog());
	}

	@Test(expected = ValidationException.class)
	public void testConstruct1Null()
	{
		new TelemetryInfo(null);
	}

	@Test
	public void testConstruct2()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		Assert.assertEquals(NAME, telemetryInfo.getName());
		Assert.assertEquals(log, telemetryInfo.getLog());
	}

	@Test(expected = ValidationException.class)
	public void testConstruct2Null()
	{
		new TelemetryInfo(null, log);
	}

	@Test
	public void testConstruct3()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, null, 0L);
		Assert.assertEquals(NAME, telemetryInfo.getName());
		Assert.assertNull(telemetryInfo.getLog());
		Assert.assertEquals(0L, telemetryInfo.getCreatedAt());
	}

	@Test(expected = ValidationException.class)
	public void testConstruct3Null()
	{
		new TelemetryInfo(null, log, 0L);
	}

	@Test
	public void testPublish()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.publish();
		telemetryInfo.checkPublished();
	}
	
	@Test
	public void testCheckPublished()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		try {
			telemetryInfo.checkPublished();
			Assert.fail();
		}
		catch (ValidationException e)
		{
			// expected
		}
		telemetryInfo.publish();
		telemetryInfo.checkPublished();
	}
	
	@Test
	public void testRepublish()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.publish();
		telemetryInfo.checkPublished();
		telemetryInfo.publish();
		telemetryInfo.checkPublished();
	}

	@Test
	public void testSetAndGet()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.set(ATTR, VALUE);
		Assert.assertEquals(VALUE, telemetryInfo.get(ATTR));
	}
	
	@Test
	public void testGetAttributeNames()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.set(ATTR, VALUE);
		Collection<String> attrNames = telemetryInfo.getAttributeNames();
		Assert.assertEquals(3, attrNames.size());
		Assert.assertTrue(CollectionUtils.containsInstance(attrNames, TelemetryInfo.ATTR_NAME));
		Assert.assertTrue(CollectionUtils.containsInstance(attrNames, TelemetryInfo.ATTR_CREATED_AT));
		Assert.assertTrue(CollectionUtils.containsInstance(attrNames, ATTR));
	}
	
	@Test(expected = ValidationException.class)
	public void testSetPublished()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.publish();
		telemetryInfo.set(ATTR, VALUE);
	}
	
	@Test(expected = ValidationException.class)
	public void testSetNull1()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.set(null, VALUE);
	}

	@Test(expected = ValidationException.class)
	public void testSetNull2()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.set(ATTR, null);
	}

	@Test(expected = ValidationException.class)
	public void testSetName()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.set(TelemetryInfo.ATTR_NAME, VALUE);
	}

	@Test(expected = ValidationException.class)
	public void testGetNull()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.get(null);
	}
	
	@Test
	public void testClear()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.set(ATTR, VALUE);
		Assert.assertEquals(VALUE, telemetryInfo.get(ATTR));
		telemetryInfo.clear(ATTR);
		Assert.assertNull(telemetryInfo.get(ATTR));
	}
	
	@Test(expected = ValidationException.class)
	public void testClearPublished()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.publish();
		telemetryInfo.clear(ATTR);
	}

	
	@Test(expected = ValidationException.class)
	public void testClearNull()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.clear(null);
	}

	@Test(expected = ValidationException.class)
	public void testClearName()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.clear(TelemetryInfo.ATTR_NAME);
	}
	
	@Test
	public void testMarkDelta() throws InterruptedException
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		Thread.sleep(10L);
		telemetryInfo.markDelta(ATTR);
		long delta = ((Long) telemetryInfo.get(ATTR)).longValue();
		if (Math.abs(delta - 10L) > 2L)
			log.warn("delta not between 8ms and 12ms: " + delta + "ms");
		Assert.assertTrue(Math.abs(delta - 10L) <= 2L);
	}
	
	@Test
	public void testSetAndGetProcessedVersion()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		Assert.assertNull(telemetryInfo.getProcessedVersion(VERSION_NAME));
		Object processedVersion = new Object();
		telemetryInfo.setProcessedVersion(VERSION_NAME, processedVersion);
		Assert.assertSame(processedVersion, telemetryInfo.getProcessedVersion(VERSION_NAME));		
	}
	
	@Test
	public void testToString()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(NAME, log);
		telemetryInfo.set(ATTR, VALUE);
		String strVal = telemetryInfo.toString();
		Assert.assertTrue(strVal.contains(TelemetryInfo.ATTR_NAME + "=" + NAME));
		Assert.assertTrue(strVal.contains(TelemetryInfo.ATTR_CREATED_AT + "=" + telemetryInfo.getCreatedAt()));
		Assert.assertTrue(strVal.contains(ATTR + "=" + VALUE));
	}
}
