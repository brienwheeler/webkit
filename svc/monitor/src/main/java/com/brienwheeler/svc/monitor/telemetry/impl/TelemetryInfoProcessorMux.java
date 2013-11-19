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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import com.brienwheeler.lib.monitor.telemetry.ITelemetryInfoProcessor;
import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;

public class TelemetryInfoProcessorMux implements ITelemetryInfoProcessor, Iterable<ITelemetryInfoProcessor>
{
	protected final Log log = LogFactory.getLog(getClass());
	
	protected final CopyOnWriteArrayList<ITelemetryInfoProcessor> processors =
			new CopyOnWriteArrayList<ITelemetryInfoProcessor>();
	
	@Required
	public synchronized void setProcessors(List<ITelemetryInfoProcessor> processors)
	{
		this.processors.clear();
		if (processors != null)
			this.processors.addAll(processors);
	}

	@Override
	public void process(TelemetryInfo telemetryInfo)
	{
		for (ITelemetryInfoProcessor processor : processors) {
			try {
				processor.process(telemetryInfo);
			}
			catch (Exception e) {
				log.error("error processing telemetryInfo: " + telemetryInfo, e);
			}
		}
	}

	public void startProcessors()
	{
		for (ITelemetryInfoProcessor processor : processors)
			TelemetryProcessorUtils.tryToStart(processor);
	}
	
	public void stopProcessors(long stopGracePeriod)
	{
		for (ITelemetryInfoProcessor processor : processors)
			TelemetryProcessorUtils.tryToStop(processor, stopGracePeriod);
	}
	
	public Iterator<ITelemetryInfoProcessor> iterator()
	{
		return processors.iterator();
	}
	
	protected void callProcessors(TelemetryInfo telemetryInfo)
	{
		telemetryInfo.publish();
		for (ITelemetryInfoProcessor processor : processors)
			processor.process(telemetryInfo);
		return;
	}

}
