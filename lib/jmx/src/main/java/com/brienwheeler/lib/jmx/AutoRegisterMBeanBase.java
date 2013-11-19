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
package com.brienwheeler.lib.jmx;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;

/**
 * Extend this base class and your bean will be JMX-exposed regardless of the Spring context configuration
 * (presence or absence of AnnotationMBeanExporter) or your MBean construction scheme (interface or annotation).
 *
 * Class to auto-detect whether there is a Spring AnnotationMBeanExporter in the context
 * and, if not, to directly to register the managed bean using MBeanRegistrationSupport.registerMBean
 * Regardless, the target managed bean (subclass) may implement traditional *MBean interface or may
 * use Spring ManagedResource/ManagedAttribute/ManagedOperation annotations
 * 
 * @author bwheeler
 */
public abstract class AutoRegisterMBeanBase implements ApplicationContextAware, InitializingBean
{
	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception
	{
		Map<String, AnnotationMBeanExporter> autoExport = applicationContext.getBeansOfType(AnnotationMBeanExporter.class);
		if (!autoExport.isEmpty())
			return; // will be auto-exported

		MBeanRegistrationSupport.registerMBean(this);
	}
	
}
