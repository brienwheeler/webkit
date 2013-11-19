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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.monitor.telemetry.ITelemetryInfoProcessor;
import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;

public class TelemetryInfoProcessorMuxTest 
{
	@Test
	public void testsetProcessorsNull()
	{
		TelemetryInfoProcessorMux processor = new TelemetryInfoProcessorMux();
		processor.setProcessors(null);
		Iterator<ITelemetryInfoProcessor> it = processor.iterator();
		Assert.assertFalse(it.hasNext());
	}
	
	@Test
	public void testProcess()
	{
		TelemetryInfoProcessorMux processor = new TelemetryInfoProcessorMux();

		TelemetryRecordingProcessor subProcessor1 = new TelemetryRecordingProcessor();
		TelemetryRecordingProcessor subProcessor2 = new TelemetryRecordingProcessor();
		
		List<ITelemetryInfoProcessor> processors = new ArrayList<ITelemetryInfoProcessor>();
		processors.add(subProcessor1);
		processors.add(subProcessor2);
		processor.setProcessors(processors);

		processor.startProcessors();
		
		TelemetryInfo info = new TelemetryInfo("InfoName");
		info.publish();
		processor.process(info);

		processor.stopProcessors(1000L);

		Assert.assertEquals(1, subProcessor1.getCount());
		Assert.assertEquals(1, subProcessor1.getCount());
	}

	@Test
	public void testCallProcessors()
	{
		TelemetryInfoProcessorMux processor = new TelemetryInfoProcessorMux();

		TelemetryRecordingProcessor subProcessor1 = new TelemetryRecordingProcessor();
		TelemetryRecordingProcessor subProcessor2 = new TelemetryRecordingProcessor();
		
		List<ITelemetryInfoProcessor> processors = new ArrayList<ITelemetryInfoProcessor>();
		processors.add(subProcessor1);
		processors.add(subProcessor2);
		processor.setProcessors(processors);

		processor.startProcessors();
		
		TelemetryInfo info = new TelemetryInfo("InfoName");
		processor.callProcessors(info);

		processor.stopProcessors(1000L);

		Assert.assertEquals(1, subProcessor1.getCount());
		Assert.assertEquals(1, subProcessor1.getCount());
	}
}
