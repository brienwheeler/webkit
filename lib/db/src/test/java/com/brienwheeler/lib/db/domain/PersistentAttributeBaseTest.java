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
package com.brienwheeler.lib.db.domain;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.db.AbstractLibDbTest;
import com.brienwheeler.lib.util.ValidationException;

public class PersistentAttributeBaseTest extends AbstractLibDbTest
{
	@Test(expected=ValidationException.class)
	public void testConstructUnpersistedOwner()
	{
		SimpleEntity entity = new SimpleEntity();
		new SimpleEntityAttribute(entity, "name", "value");
	}

	@Test
	public void testConstruct()
	{
		SimpleEntity entity = new SimpleEntity();
		simpleEntityDao.save(entity);
		new SimpleEntityAttribute(entity, "name", "value");
	}

	@Test
	public void testGetters()
	{
		SimpleEntity entity = new SimpleEntity();
		simpleEntityDao.save(entity);
		SimpleEntityAttribute attribute = new SimpleEntityAttribute(entity, "name", "value");
		simpleEntityAttributeDao.save(attribute);
		Assert.assertEquals(entity, attribute.getOwner());
		Assert.assertEquals("name", attribute.getName());
		Assert.assertEquals("value", attribute.getValue());
	}

	@Test
	public void testSetters()
	{
		SimpleEntity entity = new SimpleEntity();
		simpleEntityDao.save(entity);
		SimpleEntityAttribute attribute = new SimpleEntityAttribute(entity, "name", "value");
		Assert.assertEquals("value", attribute.getValue());
		attribute.setValue("value2");
		Assert.assertEquals("value2", attribute.getValue());
	}
}
