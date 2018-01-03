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
package com.brienwheeler.lib.svc.impl;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.brienwheeler.lib.monitor.intervene.IInterventionListener;
import com.brienwheeler.lib.monitor.telemetry.ITelemetryPublishService;
import com.brienwheeler.lib.monitor.work.IWorkPublishService;
import com.brienwheeler.lib.spring.beans.AutowireUtils;

/*
 * Service start code that needs to be shared between SpringStartableServiceBase and
 * SpringStoppableServiceBase. 
 */
public class SpringStartableServiceHelper implements ApplicationContextAware, InitializingBean
{
	protected final Log log;
	protected final StartableServiceBase service;
	protected ApplicationContext applicationContext;
	protected boolean autowireInterventionListeners = true;
	protected boolean autowireTelemetryPublishers = true;
	protected boolean autowireWorkPublishers = true;
	
	public SpringStartableServiceHelper(StartableServiceBase service, Log log)
	{
		this.service = service;
		this.log = log;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception
	{
		if (service.interventionListeners.isEmpty() && autowireInterventionListeners) {
			service.setInterventionListeners(AutowireUtils.getAutowireBeans(applicationContext,
					IInterventionListener.class, log));
		}
		if (service.telemetryPublishers.isEmpty() && autowireTelemetryPublishers) {
			service.setTelemetryPublishers(AutowireUtils.getAutowireBeans(applicationContext,
					ITelemetryPublishService.class, log));
		}
		if (service.workPublishers.isEmpty() && autowireWorkPublishers) {
			service.setWorkPublishers(AutowireUtils.getAutowireBeans(applicationContext,
					IWorkPublishService.class, log));
		}

		service.start();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	public void setAutowireInterventionListeners(boolean autowireInterventionListeners)
	{
		this.autowireInterventionListeners = autowireInterventionListeners;
	}
	
	public void setAutowireTelemetryPublishers(boolean autowireTelemetryPublishers)
	{
		this.autowireTelemetryPublishers = autowireTelemetryPublishers;
	}

	public void setAutowireWorkPublishers(boolean autowireWorkPublishers)
	{
		this.autowireWorkPublishers = autowireWorkPublishers;
	}
	
}
