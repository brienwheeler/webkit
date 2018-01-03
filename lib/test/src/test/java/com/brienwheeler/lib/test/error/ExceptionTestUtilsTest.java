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
package com.brienwheeler.lib.test.error;

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.test.UtilsTestBase;


public class ExceptionTestUtilsTest extends UtilsTestBase<ExceptionTestUtils>
{
	@Override
	protected Class<ExceptionTestUtils> getUtilClass() 
	{
		return ExceptionTestUtils.class;
	}

	@Test
	public void testTestExceptionConstructors()
	{
		ExceptionTestUtils.testExceptionConstructors(Exception.class);
	}

	@Test
	public void testTestExceptionConstructorsNSME()
	{
		ExceptionTestUtils.testExceptionConstructors(TestExceptionNSME.class);
	}

	@Test
	public void testTestExceptionConstructorsITE()
	{
		try {
			ExceptionTestUtils.testExceptionConstructors(TestExceptionITE.class);
			Assert.fail();
		}
		catch (RuntimeException e)
		{
			Assert.assertEquals(InvocationTargetException.class, e.getCause().getClass());
		}
	}
}
