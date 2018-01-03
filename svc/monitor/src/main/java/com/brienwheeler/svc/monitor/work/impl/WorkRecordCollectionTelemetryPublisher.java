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
package com.brienwheeler.svc.monitor.work.impl;

import java.util.concurrent.CopyOnWriteArraySet;

import com.brienwheeler.lib.monitor.work.MonitoredWork;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.brienwheeler.lib.monitor.telemetry.ITelemetryPublishService;
import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;
import com.brienwheeler.lib.monitor.work.IWorkRecordCollectionProcessor;
import com.brienwheeler.lib.monitor.work.WorkRecord;
import com.brienwheeler.lib.monitor.work.WorkRecordCollection;
import com.brienwheeler.lib.spring.beans.AutowireUtils;

public class WorkRecordCollectionTelemetryPublisher
		implements IWorkRecordCollectionProcessor, ApplicationContextAware, InitializingBean
{
	private static final Log log = LogFactory.getLog(WorkRecordCollectionTelemetryPublisher.class);
	
	public static final String SEPARATOR = ".";
	public static final String OK_COUNT = "okCount";
	public static final String OK_DURATION = "okDuration";
	public static final String OK_AVG_DURATION = "okAvgDuration";
	public static final String ERROR_COUNT = "errorCount";
	public static final String ERROR_DURATION = "errorDuration";
	public static final String ERROR_AVG_DURATION = "errorAvgDuration";
	
	private ApplicationContext applicationContext;
	private boolean autowireTelemetryPublishers = true;
	private final CopyOnWriteArraySet<ITelemetryPublishService> telemetryPublishers =
			new CopyOnWriteArraySet<ITelemetryPublishService>();
	
	public void setApplicationContext(ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	public void setAutowireTelemetryPublishers(boolean autowireTelemetryPublishers)
	{
		this.autowireTelemetryPublishers = autowireTelemetryPublishers;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		if (telemetryPublishers.isEmpty() && autowireTelemetryPublishers) {
			telemetryPublishers.addAll(AutowireUtils.getAutowireBeans(applicationContext,
					ITelemetryPublishService.class, log));
		}
	}

	@Override
	public void process(long timestamp, WorkRecordCollection workRecordCollection)
	{
		String sourceName = workRecordCollection.getSourceName();
		
		for (String workRecordName : workRecordCollection.getWorkRecordNames()) {
			WorkRecord workRecord = workRecordCollection.getWorkRecord(workRecordName);
			String telemetryName = workRecordName.equals(MonitoredWork.NO_NAME) ?
					sourceName : sourceName + SEPARATOR + workRecordName;
			TelemetryInfo telemetryInfo = new TelemetryInfo(telemetryName, LogFactory.getLog(sourceName), timestamp);
			telemetryInfo.set(OK_COUNT, workRecord.getWorkOkCount());
			telemetryInfo.set(OK_DURATION, workRecord.getWorkOkDuration());
			telemetryInfo.set(OK_AVG_DURATION, workRecord.getWorkOkAvgDuration());
			telemetryInfo.set(ERROR_COUNT, workRecord.getWorkErrorCount());
			telemetryInfo.set(ERROR_DURATION, workRecord.getWorkErrorDuration());
			telemetryInfo.set(ERROR_AVG_DURATION, workRecord.getWorkErrorAvgDuration());
			
			for (ITelemetryPublishService telemetryPublisher : telemetryPublishers)
				telemetryPublisher.publish(telemetryInfo);
		}
	}
	
}
