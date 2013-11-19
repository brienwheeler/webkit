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
package com.brienwheeler.lib.db;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.brienwheeler.lib.db.domain.SimpleEntity;
import com.brienwheeler.lib.test.UtilsTestBase;
import com.brienwheeler.lib.util.ValidationException;

public class DbValidationUtilsTest extends UtilsTestBase<DbValidationUtils>
{
	@Override
	protected Class<DbValidationUtils> getUtilClass()
	{
		return DbValidationUtils.class;
	}

	@Test(expected=ValidationException.class)
	public void testAssertPersistedFail()
	{
		SimpleEntity entity = new SimpleEntity();
		DbValidationUtils.assertPersisted(entity);
	}

	@Test
	public void testAssertPersistedOk()
	{
		SimpleEntity entity = new SimpleEntity();
		ReflectionTestUtils.setField(entity, "id", 1L);
		DbValidationUtils.assertPersisted(entity);
	}
	
}
