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
package com.brienwheeler.lib.db;

import com.brienwheeler.lib.db.domain.DbId;
import com.brienwheeler.lib.db.domain.GeneratedIdEntityBase;
import com.brienwheeler.lib.util.ValidationUtils;

public class DbValidationUtils
{
	private DbValidationUtils() {}
	
	public static <T extends GeneratedIdEntityBase<T>> void assertPersisted(T entity)
	{
		ValidationUtils.assertNotNull(entity, "entity cannot be null");
		ValidationUtils.assertTrue(entity.getId() != 0, "entity cannot be unpersisted");
	}
	
	public static <T extends GeneratedIdEntityBase<T>> void assertPersisted(DbId<T> dbId)
	{
		ValidationUtils.assertNotNull(dbId, "entity cannot be null");
		ValidationUtils.assertTrue(dbId.getId() != 0, "entity cannot be unpersisted");
	}
}
