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
package com.brienwheeler.svc.monitor.work.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;
import com.brienwheeler.lib.svc.impl.mocks.AnnotatedSpringStoppableService;
import com.brienwheeler.svc.monitor.telemetry.impl.TelemetryPublishService;
import com.brienwheeler.svc.monitor.telemetry.impl.TelemetryRecordingProcessor;
import com.brienwheeler.svc.monitor.telemetry.impl.TelemetryServiceBaseTestUtils;

@ContextConfiguration({
    "classpath:com/brienwheeler/lib/spring/beans/contextBeanDumper.xml",
    "classpath:com/brienwheeler/svc/monitor/work/WorkPublishService-test1Context.xml" })
public class WorkPublishServiceTest extends AbstractJUnit4SpringContextTests
{
	private static AnnotatedSpringStoppableService workService;
	private static NotifyingWorkPublishService publishService;
	private static TelemetryRecordingProcessor recordingProcessor;
	
	private static long NAME_OK_1 = 10L;
	private static long NAME_OK_2 = 20L;
	private static long NAME_OK_3 = 30L;
	private static long NAME_OK_4 = 40L;
	private static long NAME_ERR_1 = 50L;
	private static long NAME_ERR_2 = 60L;
	private static long NAME_ERR_3 = 70L;
	private static long NONAME_OK_1 = 80L;
	private static long NONAME_OK_2 = 90L;
	
	// instead of @BeforeClass this is called for lazy initialization from a @Before so that
	// applicationContext is created and started
	private static void initialize(ApplicationContext applicationContext)
	{
		if (workService != null)
			return;
		
		workService = applicationContext.getBean(AnnotatedSpringStoppableService.class);
		publishService = applicationContext.getBean(NotifyingWorkPublishService.class);

		TelemetryPublishService telemetryPublishService = applicationContext.getBean(TelemetryPublishService.class);
		recordingProcessor = TelemetryServiceBaseTestUtils.findProcessor(telemetryPublishService, TelemetryRecordingProcessor.class);
	}

	@Before
	public void onSetup()
	{
		initialize(applicationContext);
	}
	
	@Test
	public void test() throws Exception
	{
		// wait for background thread to wake up and process 0 monitors
		publishService.getBarrier().await();
		
		// now load up some work data
		
		// first two successful named work
		workService.testMethodWorkName(NAME_OK_1);
		workService.testMethodWorkName(NAME_OK_2);
		workService.testMethodWorkName(NAME_OK_3);
		workService.testMethodWorkName(NAME_OK_4);

		// now three error named work
		try {
			workService.testMethodWorkNameRuntimeException(NAME_ERR_1);
			Assert.fail();
		}
		catch (RuntimeException e) {
			// expected
		}
		try {
			workService.testMethodWorkNameRuntimeException(NAME_ERR_2);
			Assert.fail();
		}
		catch (RuntimeException e) {
			// expected
		}
		try {
			workService.testMethodWorkNameRuntimeException(NAME_ERR_3);
			Assert.fail();
		}
		catch (RuntimeException e) {
			// expected
		}

		// finally two successful unnamed work
		workService.testMethodWorkNoName(NONAME_OK_1);
		workService.testMethodWorkNoName2(NONAME_OK_2);
		
		// wait for background thread to process monitors from service calls above
		publishService.getBarrier().await();
		
		TelemetryInfo recordedTelemetry[] = recordingProcessor.getRecordedTelemetry();
		Assert.assertEquals(2, recordedTelemetry.length);
		
		// make sure WORK_NAME is first and NO_NAME is second
		if (recordedTelemetry[1].getName().endsWith(AnnotatedSpringStoppableService.WORK_NAME)) {
			TelemetryInfo tmp = recordedTelemetry[0];
			recordedTelemetry[0] = recordedTelemetry[1];
			recordedTelemetry[1] = tmp;
		}
		
		// check successful named work
		Assert.assertEquals(4, recordedTelemetry[0].get(WorkRecordCollectionTelemetryPublisher.OK_COUNT));
		Assert.assertTrue(Math.abs(NAME_OK_1 + NAME_OK_2 + NAME_OK_3 + NAME_OK_4 -
				(Long) recordedTelemetry[0].get(WorkRecordCollectionTelemetryPublisher.OK_DURATION)) < 10);
		Assert.assertEquals(((float) NAME_OK_1 + NAME_OK_2 + NAME_OK_3 + NAME_OK_4) / 4,
				((Float) recordedTelemetry[0].get(WorkRecordCollectionTelemetryPublisher.OK_AVG_DURATION)).floatValue(),
				2.0f);

		// check unsuccessful named work
		Assert.assertEquals(3, recordedTelemetry[0].get(WorkRecordCollectionTelemetryPublisher.ERROR_COUNT));
		Assert.assertTrue(Math.abs(NAME_ERR_1 + NAME_ERR_2 + NAME_ERR_3 -
				(Long) recordedTelemetry[0].get(WorkRecordCollectionTelemetryPublisher.ERROR_DURATION)) < 10);
		Assert.assertEquals(((float) NAME_ERR_1 + NAME_ERR_2 + NAME_ERR_3) / 3,
				((Float) recordedTelemetry[0].get(WorkRecordCollectionTelemetryPublisher.ERROR_AVG_DURATION)).floatValue(),
				2.0f);

		// check successful unnamed work
		Assert.assertEquals(2, recordedTelemetry[1].get(WorkRecordCollectionTelemetryPublisher.OK_COUNT));
		Assert.assertTrue(Math.abs(NONAME_OK_1 + NONAME_OK_2 -
				(Long) recordedTelemetry[1].get(WorkRecordCollectionTelemetryPublisher.OK_DURATION)) < 10);
		Assert.assertEquals(((float) NONAME_OK_1 + NONAME_OK_2) / 2,
				((Float) recordedTelemetry[1].get(WorkRecordCollectionTelemetryPublisher.OK_AVG_DURATION)).floatValue(),
				2.0f);

		// check unsuccessful unnamed work
		Assert.assertEquals(0, recordedTelemetry[1].get(WorkRecordCollectionTelemetryPublisher.ERROR_COUNT));
		Assert.assertTrue(Math.abs((Long) recordedTelemetry[1].get(WorkRecordCollectionTelemetryPublisher.ERROR_DURATION)) < 1);
		Assert.assertEquals(0f,
				((Float) recordedTelemetry[1].get(WorkRecordCollectionTelemetryPublisher.ERROR_AVG_DURATION)).floatValue(),
				2.0f);
	}
}
