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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.brienwheeler.lib.monitor.intervene.mocks.MockInterventionListener;
import com.brienwheeler.lib.monitor.intervene.mocks.MockInterventionListener2;
import com.brienwheeler.lib.monitor.telemetry.mocks.MockTelemetryPublishService;
import com.brienwheeler.lib.monitor.telemetry.mocks.MockTelemetryPublishService2;
import com.brienwheeler.lib.monitor.work.mocks.MockWorkPublishService;
import com.brienwheeler.lib.monitor.work.mocks.MockWorkPublishService2;
import com.brienwheeler.lib.svc.impl.mocks.NullSpringStartableService;

@ContextConfiguration({
    "classpath:com/brienwheeler/lib/spring/beans/contextBeanDumper.xml",
    "classpath:com/brienwheeler/lib/svc/SpringStartableServiceBase-testContext.xml" })
public class SpringStartableServiceBaseTest extends AbstractJUnit4SpringContextTests
{
	@Test
	public void testAutowired()
	{
		NullSpringStartableService autowiredService = applicationContext.getBean("com.brienwheeler.lib.svc.nullSpringStartableService-autowired",
				NullSpringStartableService.class);
		
		Assert.assertEquals(1, ServiceBaseTestUtil.getInterventionListeners(autowiredService).size());
		Assert.assertEquals(MockInterventionListener.class,
				ServiceBaseTestUtil.getInterventionListeners(autowiredService).iterator().next().getClass());
		
		Assert.assertEquals(1, ServiceBaseTestUtil.getTelemetryPublishers(autowiredService).size());
		Assert.assertEquals(MockTelemetryPublishService.class,
				ServiceBaseTestUtil.getTelemetryPublishers(autowiredService).iterator().next().getClass());
		
		Assert.assertEquals(1, ServiceBaseTestUtil.getWorkPublishers(autowiredService).size());
		Assert.assertEquals(MockWorkPublishService.class,
				ServiceBaseTestUtil.getWorkPublishers(autowiredService).iterator().next().getClass());
	}

	@Test
	public void testAutowiredDisabled()
	{
		NullSpringStartableService autowiredService = applicationContext.getBean("com.brienwheeler.lib.svc.nullSpringStartableService-autowireDisabled",
				NullSpringStartableService.class);
		Assert.assertEquals(0, ServiceBaseTestUtil.getInterventionListeners(autowiredService).size());
		Assert.assertEquals(0, ServiceBaseTestUtil.getTelemetryPublishers(autowiredService).size());
		Assert.assertEquals(0, ServiceBaseTestUtil.getWorkPublishers(autowiredService).size());
	}

	@Test
	public void testManuallyWired()
	{
		NullSpringStartableService autowiredService = applicationContext.getBean("com.brienwheeler.lib.svc.nullSpringStartableService-manuallyWired",
				NullSpringStartableService.class);
		
		Assert.assertEquals(1, ServiceBaseTestUtil.getInterventionListeners(autowiredService).size());
		Assert.assertEquals(MockInterventionListener2.class,
				ServiceBaseTestUtil.getInterventionListeners(autowiredService).iterator().next().getClass());
		
		Assert.assertEquals(1, ServiceBaseTestUtil.getTelemetryPublishers(autowiredService).size());
		Assert.assertEquals(MockTelemetryPublishService2.class,
				ServiceBaseTestUtil.getTelemetryPublishers(autowiredService).iterator().next().getClass());
		
		Assert.assertEquals(1, ServiceBaseTestUtil.getWorkPublishers(autowiredService).size());
		Assert.assertEquals(MockWorkPublishService2.class,
				ServiceBaseTestUtil.getWorkPublishers(autowiredService).iterator().next().getClass());
	}	
}
