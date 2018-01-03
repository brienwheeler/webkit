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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;
import com.brienwheeler.lib.monitor.telemetry.impl.TelemetryInfoProcessorBase;

public class TelemetryRecordingProcessor extends TelemetryInfoProcessorBase
{
	private static final Log log = LogFactory.getLog(TelemetryRecordingProcessor.class);
	
	private final List<TelemetryInfo> recordedTelemetry = new ArrayList<TelemetryInfo>();
	private AtomicInteger delay = new AtomicInteger(0);
	
	@Override
	public void onProcess(TelemetryInfo telemetryInfo)
	{
		int delay = this.delay.get();
		if (delay != 0) {
			try {
				Thread.sleep(delay);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.warn(e);
			}
		}
		
		synchronized (recordedTelemetry) {
			recordedTelemetry.add(telemetryInfo);
			log.debug("count incremented to " + recordedTelemetry.size());
		}
	}

	public int getCount()
	{
		synchronized (recordedTelemetry) {
			return recordedTelemetry.size();
		}
	}

	public void setDelay(int delay)
	{
		this.delay.set(delay);
	}
	
	public TelemetryInfo[] getRecordedTelemetry()
	{
		synchronized (recordedTelemetry) {
			return recordedTelemetry.toArray(new TelemetryInfo[recordedTelemetry.size()]);
			
		}
	}

    public void initialize() {
        synchronized (recordedTelemetry) {
            recordedTelemetry.clear();
            log.debug("initialize, count set to 0");
        }
    }
	
}
