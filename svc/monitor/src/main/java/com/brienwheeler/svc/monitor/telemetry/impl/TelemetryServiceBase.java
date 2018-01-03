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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Required;

import com.brienwheeler.lib.monitor.telemetry.ITelemetryInfoProcessor;
import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;
import com.brienwheeler.lib.svc.ServiceState;
import com.brienwheeler.lib.svc.impl.SpringStoppableServiceBase;

public abstract class TelemetryServiceBase extends SpringStoppableServiceBase
{
    protected final CopyOnWriteArrayList<ITelemetryInfoProcessor> processors =
            new CopyOnWriteArrayList<ITelemetryInfoProcessor>();

    public TelemetryServiceBase()
	{
		// not allowed for subclasses of this service
		super.setAutowireTelemetryPublishers(false);
	}

	@Override
	public final void setAutowireTelemetryPublishers(boolean autowireTelemetryPublishers)
	{
		// not allowed for subclasses of this service
		throw new IllegalArgumentException("not allowed to autowire telemetry publishers into " + getClass().getSimpleName());
	}

	@Required
	public void setProcessors(List<ITelemetryInfoProcessor> processors)
	{
		// prevent any state changes while doing this since this interacts
		// with onStart and onStop
		synchronized (state) {
			// only stop/start the processors if we're running
			if (getState() == ServiceState.RUNNING)
				stopProcessors();

            this.processors.clear();
            if (processors != null)
                this.processors.addAll(processors);

            // only stop/start the processors if we're running
            if (getState() == ServiceState.RUNNING)
                startProcessors();
		}
	}

    @Override
	protected void onStart() throws InterruptedException
	{
		super.onStart();
		
		// start any processors that are services
        startProcessors();
	}

	@Override
	protected void onStop() throws InterruptedException
	{
		// stop any processors that are services
        stopProcessors();

		super.onStop();
	}

    // do not make this an @GracefulShutdown because subclasses may want to call
    // it during shutdown processing.
    protected void callProcessors(TelemetryInfo telemetryInfo)
    {
        telemetryInfo.publish();
        for (ITelemetryInfoProcessor processor : processors)
            processor.process(telemetryInfo);
    }

    private void startProcessors()
    {
        for (ITelemetryInfoProcessor processor : processors)
            TelemetryProcessorUtils.tryToStart(processor);
    }

    private void stopProcessors()
    {
        for (ITelemetryInfoProcessor processor : processors)
            TelemetryProcessorUtils.tryToStop(processor, stopGracePeriod);
    }
}
