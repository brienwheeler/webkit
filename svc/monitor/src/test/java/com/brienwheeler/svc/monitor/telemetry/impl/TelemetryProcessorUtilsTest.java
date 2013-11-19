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

import junit.framework.Assert;

import org.junit.Test;

import com.brienwheeler.lib.monitor.telemetry.ITelemetryInfoProcessor;
import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;
import com.brienwheeler.lib.svc.IStoppableService;
import com.brienwheeler.lib.svc.ServiceState;
import com.brienwheeler.lib.test.UtilsTestBase;
import com.brienwheeler.svc.monitor.telemetry.IStoppableTelemetryInfoProcessor;

public class TelemetryProcessorUtilsTest extends UtilsTestBase<TelemetryProcessorUtilsTest>
{
	@Override
	protected Class<TelemetryProcessorUtilsTest> getUtilClass()
	{
		return TelemetryProcessorUtilsTest.class;
	}

	@Test
	public void testStartStopService()
	{
		ProcessorService processor = new ProcessorService();
		Assert.assertEquals(ServiceState.STARTING, processor.getState());
		TelemetryProcessorUtils.tryToStart(processor);
		Assert.assertEquals(ServiceState.RUNNING, processor.getState());
		TelemetryProcessorUtils.tryToStop(processor, 1000L);
		Assert.assertEquals(ServiceState.STOPPED, processor.getState());
	}

	@Test
	public void testStartStopProcessor()
	{
		StoppableProcessor processor = new StoppableProcessor();
		Assert.assertEquals(ServiceState.STARTING, processor.getState());
		TelemetryProcessorUtils.tryToStart(processor);
		Assert.assertEquals(ServiceState.RUNNING, processor.getState());
		TelemetryProcessorUtils.tryToStop(processor, 1000L);
		Assert.assertEquals(ServiceState.STOPPED, processor.getState());
	}

	@Test
	public void testStartStopGenericProcessor()
	{
		GenericProcessor processor = new GenericProcessor();
		Assert.assertEquals(ServiceState.STARTING, processor.getState());
		TelemetryProcessorUtils.tryToStart(processor);
		Assert.assertEquals(ServiceState.STARTING, processor.getState());
		TelemetryProcessorUtils.tryToStop(processor, 1000L);
		Assert.assertEquals(ServiceState.STARTING, processor.getState());
	}

	static class ProcessorService implements IStoppableService, ITelemetryInfoProcessor
	{
		private boolean started = false;
		private boolean stopped = false;
		
		@Override
		public void start()
		{
			started = true;
		}
		
		@Override
		public ServiceState getState()
		{
			if (stopped)
				return ServiceState.STOPPED;
			if (started)
				return ServiceState.RUNNING;
			return ServiceState.STARTING;
		}
		
		@Override
		public void stop(long gracePeriod)
		{
			stopped = true;
		}
		
		@Override
		public void stopImmediate()
		{
			stop(0L);
		}

		@Override
		public void process(TelemetryInfo telemetryInfo)
		{
		}
	}

	static class StoppableProcessor implements IStoppableTelemetryInfoProcessor
	{
		private boolean started = false;
		private boolean stopped = false;
		
		@Override
		public void start()
		{
			started = true;
		}

		@Override
		public void stop(long stopGracePeriod)
		{
			stopped = true;
		}

		public ServiceState getState()
		{
			if (stopped)
				return ServiceState.STOPPED;
			if (started)
				return ServiceState.RUNNING;
			return ServiceState.STARTING;
		}
		
		@Override
		public void process(TelemetryInfo telemetryInfo)
		{
		}
	}
	
	static class GenericProcessor implements ITelemetryInfoProcessor
	{
		private boolean started = false;
		private boolean stopped = false;
		
		public ServiceState getState()
		{
			if (stopped)
				return ServiceState.STOPPED;
			if (started)
				return ServiceState.RUNNING;
			return ServiceState.STARTING;
		}
		
		@Override
		public void process(TelemetryInfo telemetryInfo)
		{
		}
	}

}

