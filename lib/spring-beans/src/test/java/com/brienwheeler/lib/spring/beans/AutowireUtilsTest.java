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
package com.brienwheeler.lib.spring.beans;

import java.util.Collection;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.brienwheeler.lib.test.UtilsTestBase;
import com.brienwheeler.lib.util.ValidationException;

public class AutowireUtilsTest extends UtilsTestBase<AutowireUtils>
{
	private static final Log log = LogFactory.getLog(AutowireUtilsTest.class);
	
	@Override
	protected Class<AutowireUtils> getUtilClass()
	{
		return AutowireUtils.class;
	}

	@Test
	public void testGetAutowireBeans()
	{
		SmartClassPathXmlApplicationContext context = new SmartClassPathXmlApplicationContext(TestConstants.CTX_BEAN_DUMPER);
		Collection<ContextBeanDumper> beanDumpers = AutowireUtils.getAutowireBeans(context, ContextBeanDumper.class, log);
		Assert.assertEquals(1, beanDumpers.size());
		Collection<PropertyPlaceholderConfigurer> configurers = AutowireUtils.getAutowireBeans(context, PropertyPlaceholderConfigurer.class, log);
		Assert.assertEquals(0, configurers.size());
	}

	@Test(expected = ValidationException.class)
	public void testGetAutowireBeansNullContext()
	{
		@SuppressWarnings("unused")
		Collection<ContextBeanDumper> beanDumpers = AutowireUtils.getAutowireBeans(null, ContextBeanDumper.class, log);
	}

	@Test(expected = ValidationException.class)
	public void testGetAutowireBeansNullClass()
	{
		SmartClassPathXmlApplicationContext context = new SmartClassPathXmlApplicationContext(TestConstants.CTX_BEAN_DUMPER);
		@SuppressWarnings("unused")
		Collection<ContextBeanDumper> beanDumpers = AutowireUtils.getAutowireBeans(context, null, log);
	}

	@Test(expected = ValidationException.class)
	public void testGetAutowireBeansNullLog()
	{
		SmartClassPathXmlApplicationContext context = new SmartClassPathXmlApplicationContext(TestConstants.CTX_BEAN_DUMPER);
		@SuppressWarnings("unused")
		Collection<ContextBeanDumper> beanDumpers = AutowireUtils.getAutowireBeans(context, ContextBeanDumper.class, null);
	}
}
