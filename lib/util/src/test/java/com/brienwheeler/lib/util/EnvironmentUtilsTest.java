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
package com.brienwheeler.lib.util;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.test.UtilsTestBase;

public class EnvironmentUtilsTest extends UtilsTestBase<EnvironmentUtils>
{
	private static final String TEST_PROP = "com.brienwheeler.lib.util.EnvironmentUtilsTest.testProp";
	
	@Override
	protected Class<EnvironmentUtils> getUtilClass()
	{
		return EnvironmentUtils.class;
	}

	@Test
	public void testIsProduction()
	{
		System.setProperty(EnvironmentUtils.SYSTEM_PROPERTY, EnvironmentUtils.PRODUCTION_ENVIRONMENT);
		Assert.assertTrue(EnvironmentUtils.isProduction());
		System.setProperty(EnvironmentUtils.SYSTEM_PROPERTY, "NOT-" + EnvironmentUtils.PRODUCTION_ENVIRONMENT);
		Assert.assertFalse(EnvironmentUtils.isProduction());
	}

	@Test
	public void testGetEnvironment()
	{
		System.setProperty(EnvironmentUtils.SYSTEM_PROPERTY, " " + EnvironmentUtils.PRODUCTION_ENVIRONMENT + " ");
		Assert.assertEquals(EnvironmentUtils.PRODUCTION_ENVIRONMENT, EnvironmentUtils.getEnvironment());
		System.setProperty(EnvironmentUtils.SYSTEM_PROPERTY, " NOT-" + EnvironmentUtils.PRODUCTION_ENVIRONMENT + " ");
		Assert.assertEquals("NOT-" + EnvironmentUtils.PRODUCTION_ENVIRONMENT, EnvironmentUtils.getEnvironment());
		System.clearProperty(EnvironmentUtils.SYSTEM_PROPERTY);
		Assert.assertEquals(EnvironmentUtils.PRODUCTION_ENVIRONMENT, EnvironmentUtils.getEnvironment());
	}
	
	@Test
	public void testGetBooleanProperty()
	{
		System.clearProperty(TEST_PROP);
		Assert.assertFalse(EnvironmentUtils.getBooleanProperty(TEST_PROP));
		System.setProperty(TEST_PROP, "true");
		Assert.assertTrue(EnvironmentUtils.getBooleanProperty(TEST_PROP));
		System.setProperty(TEST_PROP, "XXX");
		Assert.assertFalse(EnvironmentUtils.getBooleanProperty(TEST_PROP));
	}
}
