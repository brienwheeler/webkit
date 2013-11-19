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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;

public class FilteringTelemetryInfoProcessorTest
{
	private static final String INFO_NAME = "InfoName";
	private static final String NOT_A_MATCH = "NotAMatch";
	
	private FilteringTelemetryInfoProcessor processor;
	private TelemetryRecordingProcessor sink;
	
	@Before
	public void onSetUp()
	{
		processor = new FilteringTelemetryInfoProcessor();
		sink = new TelemetryRecordingProcessor();
		processor.setNextProcessor(sink);
		processor.start();
	}
	
	@After
	public void onTearDown()
	{
		processor.stop(1000L);
	}
	
	private void doProcess()
	{
		TelemetryInfo telemetryInfo = new TelemetryInfo(INFO_NAME);
		telemetryInfo.publish();
		processor.process(telemetryInfo);
	}
	
	@Test
	public void testOnProcessNull()
	{
		processor.setFilterRecords(null);
		doProcess();
		Assert.assertEquals(1, sink.getCount());
	}
	
	@Test
	public void testOnProcessEmpty()
	{
		processor.setFilterRecords("  ");
		doProcess();
		Assert.assertEquals(1, sink.getCount());
	}
	
	@Test
	public void testOnProcessInclude()
	{
		processor.setFilterRecords("INCLUDE" + FilteringTelemetryInfoProcessor.FIELD_SEPARATOR + INFO_NAME);
		doProcess();
		Assert.assertEquals(1, sink.getCount());
	}
	
	@Test
	public void testOnProcessExclude()
	{
		processor.setFilterRecords("EXCLUDE" + FilteringTelemetryInfoProcessor.FIELD_SEPARATOR + INFO_NAME);
		doProcess();
		Assert.assertEquals(0, sink.getCount());
	}

	@Test
	public void testOnProcessNoMatch()
	{
		processor.setFilterRecords("EXCLUDE" + FilteringTelemetryInfoProcessor.FIELD_SEPARATOR + NOT_A_MATCH);
		doProcess();
		Assert.assertEquals(1, sink.getCount());
	}

	@Test
	public void testOnProcessBadFilterSpec()
	{
		processor.setFilterRecords("EXCLUDE" + FilteringTelemetryInfoProcessor.FIELD_SEPARATOR + INFO_NAME +
				FilteringTelemetryInfoProcessor.FIELD_SEPARATOR + "NOTSUPPOSEDTOBEHERE");
		doProcess();
		Assert.assertEquals(1, sink.getCount());
	}

	@Test
	public void testOnProcessBadAction()
	{
		processor.setFilterRecords("BADACTION" + FilteringTelemetryInfoProcessor.FIELD_SEPARATOR + INFO_NAME);
		doProcess();
		Assert.assertEquals(1, sink.getCount());
	}

	@Test
	public void testOnProcessBadPattern()
	{
		processor.setFilterRecords("EXCLUDE" + FilteringTelemetryInfoProcessor.FIELD_SEPARATOR + "[a-z");
		doProcess();
		Assert.assertEquals(1, sink.getCount());
	}
}
