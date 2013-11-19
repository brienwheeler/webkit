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

import com.brienwheeler.lib.monitor.telemetry.ITelemetryInfoProcessor;
import com.brienwheeler.lib.svc.IStartableService;
import com.brienwheeler.lib.svc.IStoppableService;
import com.brienwheeler.svc.monitor.telemetry.IStartableTelemetryInfoProcessor;
import com.brienwheeler.svc.monitor.telemetry.IStoppableTelemetryInfoProcessor;

public class TelemetryProcessorUtils
{
	private TelemetryProcessorUtils() {}
	
	public static void tryToStart(ITelemetryInfoProcessor processor)
	{
		if (processor instanceof IStartableService)
			((IStartableService) processor).start();
		else if (processor instanceof IStartableTelemetryInfoProcessor)
			((IStartableTelemetryInfoProcessor) processor).start();
	}

	public static void tryToStop(ITelemetryInfoProcessor processor, long stopGracePeriod)
	{
		if (processor instanceof IStoppableService)
			((IStoppableService) processor).stop(stopGracePeriod);
		else if (processor instanceof IStoppableTelemetryInfoProcessor)
			((IStoppableTelemetryInfoProcessor) processor).stop(stopGracePeriod);
	}
}
