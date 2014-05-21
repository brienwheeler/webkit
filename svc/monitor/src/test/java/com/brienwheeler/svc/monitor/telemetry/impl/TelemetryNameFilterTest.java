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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TelemetryNameFilterTest
{
	private static final String INFO_NAME = "InfoName";
	private static final String NOT_A_MATCH = "NotAMatch";
	
	private TelemetryNameFilter filter;

	@Before
	public void onSetUp()
	{
        filter = new TelemetryNameFilter();
	}
	
	@Test
	public void testOnProcessNull()
	{
        filter.setFilterRecords(null);
		Assert.assertEquals(true, filter.process(INFO_NAME));
	}
	
	@Test
	public void testOnProcessEmpty()
	{
        filter.setFilterRecords("  ");
        Assert.assertEquals(true, filter.process(INFO_NAME));
	}
	
	@Test
	public void testOnProcessInclude()
	{
        filter.setFilterRecords("INCLUDE" + TelemetryNameFilter.FIELD_SEPARATOR + INFO_NAME);
        Assert.assertEquals(true, filter.process(INFO_NAME));
	}
	
	@Test
	public void testOnProcessExclude()
	{
        filter.setFilterRecords("EXCLUDE" + TelemetryNameFilter.FIELD_SEPARATOR + INFO_NAME);
        Assert.assertEquals(false, filter.process(INFO_NAME));
	}

	@Test
	public void testOnProcessNoMatch()
	{
        filter.setFilterRecords("EXCLUDE" + TelemetryNameFilter.FIELD_SEPARATOR + NOT_A_MATCH);
        Assert.assertEquals(true, filter.process(INFO_NAME));
	}

	@Test
	public void testOnProcessBadFilterSpec()
	{
        filter.setFilterRecords("EXCLUDE" + TelemetryNameFilter.FIELD_SEPARATOR + INFO_NAME +
                TelemetryNameFilter.FIELD_SEPARATOR + "NOTSUPPOSEDTOBEHERE");
        Assert.assertEquals(true, filter.process(INFO_NAME));
	}

	@Test
	public void testOnProcessBadAction()
	{
        filter.setFilterRecords("BADACTION" + TelemetryNameFilter.FIELD_SEPARATOR + INFO_NAME);
        Assert.assertEquals(true, filter.process(INFO_NAME));
	}

	@Test
	public void testOnProcessBadPattern()
	{
        filter.setFilterRecords("EXCLUDE" + TelemetryNameFilter.FIELD_SEPARATOR + "[a-z");
        Assert.assertEquals(true, filter.process(INFO_NAME));
	}
}
