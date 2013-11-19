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

import org.springframework.beans.factory.annotation.Required;

import com.brienwheeler.lib.monitor.telemetry.ITelemetryInfoProcessor;
import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;
import com.brienwheeler.svc.monitor.telemetry.IStoppableTelemetryInfoProcessor;

public abstract class TelemetryInfoProcessorChainBase implements IStoppableTelemetryInfoProcessor
{
	private ITelemetryInfoProcessor nextProcessor;

	protected abstract boolean onProcess(TelemetryInfo telemetryInfo);

	@Required
	public void setNextProcessor(ITelemetryInfoProcessor nextProcessor)
	{
		this.nextProcessor = nextProcessor;
	}

	@Override
	public void process(TelemetryInfo telemetryInfo)
	{
		if (onProcess(telemetryInfo))
			nextProcessor.process(telemetryInfo);
	}
	
	protected void onStart()
	{
	}
	
	public final void start()
	{
		onStart();
		TelemetryProcessorUtils.tryToStart(nextProcessor);
	}
	
	protected void onStop()
	{
	}
	
	public final void stop(long stopGracePeriod)
	{
		TelemetryProcessorUtils.tryToStop(nextProcessor, stopGracePeriod);
		onStop();
	}
}
