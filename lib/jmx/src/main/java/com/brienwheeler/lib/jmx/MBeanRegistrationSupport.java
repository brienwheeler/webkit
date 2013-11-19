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

import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.RequiredModelMBean;

import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.assembler.MBeanInfoAssembler;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;
import org.springframework.jmx.export.naming.IdentityNamingStrategy;
import org.springframework.jmx.export.naming.ObjectNamingStrategy;
import org.springframework.jmx.support.JmxUtils;

/**
 * Utility class to leverage org.springframework.jmx.support.MBeanRegistrationSupport
 * to allow programmatic bean registration.
 */
public class MBeanRegistrationSupport extends org.springframework.jmx.support.MBeanRegistrationSupport
{
	/** Constant for the JMX <code>mr_type</code> "ObjectReference" */
	private static final String MR_TYPE_OBJECT_REFERENCE = "ObjectReference";

	public static void registerMBean(Object mbean)
	{
		try {
			ObjectNamingStrategy namingStrategy = new IdentityNamingStrategy();
			ObjectName objectName = namingStrategy.getObjectName(mbean, null);

			MBeanRegistrationSupport registrar = new MBeanRegistrationSupport();
			registrar.setServer(JmxUtils.locateMBeanServer());
			
			// if item qualifies as MBean, export it directly
			if (JmxUtils.isMBean(mbean.getClass())) {
				registrar.doRegister(mbean, objectName);
				return;
			}
			
			// assemble MBean info (from annotations by default)
			ModelMBean modelMBean = new RequiredModelMBean();
			modelMBean.setManagedResource(mbean, MR_TYPE_OBJECT_REFERENCE);
			MBeanInfoAssembler mBeanInfoAssembler = new MetadataMBeanInfoAssembler(new AnnotationJmxAttributeSource());
			modelMBean.setModelMBeanInfo(mBeanInfoAssembler.getMBeanInfo(mbean, objectName.getCanonicalName()));
			registrar.doRegister(modelMBean, objectName);
		}
		catch (Exception e) {
			throw new JmxRegisterException("error registering MBean", e);
		}
	}
}
