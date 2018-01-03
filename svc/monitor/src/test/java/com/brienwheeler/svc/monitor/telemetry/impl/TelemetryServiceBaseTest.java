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
package com.brienwheeler.svc.monitor.telemetry.impl;

import java.util.ArrayList;
import java.util.List;

import com.brienwheeler.svc.monitor.telemetry.mocks.MockTelemetryService;
import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.monitor.telemetry.ITelemetryInfoProcessor;
import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;
import org.springframework.test.util.ReflectionTestUtils;

public class TelemetryServiceBaseTest
{
	@Test
	public void testSetProcessorsNull()
	{
		MockTelemetryService service = new MockTelemetryService();
		service.setProcessors(null);
        List<ITelemetryInfoProcessor> processors = (List<ITelemetryInfoProcessor>)
                ReflectionTestUtils.getField(service, "processors");
		Assert.assertFalse(processors.iterator().hasNext());
	}

	@Test
	public void testCallProcessors()
	{
        MockTelemetryService service = new MockTelemetryService();

		TelemetryRecordingProcessor subProcessor1 = new TelemetryRecordingProcessor();
		TelemetryRecordingProcessor subProcessor2 = new TelemetryRecordingProcessor();
		
		List<ITelemetryInfoProcessor> processors = new ArrayList<ITelemetryInfoProcessor>();
		processors.add(subProcessor1);
		processors.add(subProcessor2);
		service.setProcessors(processors);

		service.start();
		
		TelemetryInfo info = new TelemetryInfo("InfoName");
		service.callProcessors(info);

		service.stop(1000L);

		Assert.assertEquals(1, subProcessor1.getCount());
		Assert.assertEquals(1, subProcessor2.getCount());
	}
}
