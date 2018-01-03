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
package com.brienwheeler.lib.db.dao;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.db.AbstractLibDbTest;
import com.brienwheeler.lib.db.domain.SimpleEntity;
import com.brienwheeler.lib.db.domain.SimpleEntityAttribute;

public class PersistentAttributeDaoBaseTest extends AbstractLibDbTest
{
	@Test
	public void testFindByOwner()
	{
		SimpleEntity entity = new SimpleEntity();
		simpleEntityDao.save(entity);
		
		SimpleEntityAttribute attribute1 = new SimpleEntityAttribute(entity, "name", "value");
		simpleEntityAttributeDao.save(attribute1);
		SimpleEntityAttribute attribute2 = new SimpleEntityAttribute(entity, "name2", "value2");
		simpleEntityAttributeDao.save(attribute2);
		
		List<SimpleEntityAttribute> attributes = simpleEntityAttributeDao.findByOwner(entity.getId());
		Assert.assertEquals(2, attributes.size());
		TreeMap<String,SimpleEntityAttribute> attributeMap = new TreeMap<String,SimpleEntityAttribute>();
		for (SimpleEntityAttribute attribute : attributes)
		{
			attributeMap.put(attribute.getName(), attribute);
		}
		
		Iterator<String> keyIterator = attributeMap.keySet().iterator();
		
		String name = keyIterator.next();
		Assert.assertEquals("name", name);
		Assert.assertTrue(attributeMap.get(name) != attribute1);
		Assert.assertEquals(attribute1, attributeMap.get(name));

		name = keyIterator.next();
		Assert.assertEquals("name2", name);
		Assert.assertTrue(attributeMap.get(name) != attribute2);
		Assert.assertEquals(attribute2, attributeMap.get(name));
	}

	@Test
	public void testFindByOwnerAndName()
	{
		SimpleEntity entity = new SimpleEntity();
		simpleEntityDao.save(entity);
		
		Assert.assertNull(simpleEntityAttributeDao.findByOwnerAndName(entity.getId(), "name2"));

		SimpleEntityAttribute attribute1 = new SimpleEntityAttribute(entity, "name", "value");
		simpleEntityAttributeDao.save(attribute1);
		SimpleEntityAttribute attribute2 = new SimpleEntityAttribute(entity, "name2", "value2");
		simpleEntityAttributeDao.save(attribute2);
		
		SimpleEntityAttribute attribute = simpleEntityAttributeDao.findByOwnerAndName(entity.getId(), "name2");
		Assert.assertEquals(entity, attribute.getOwner());
		Assert.assertEquals("name2", attribute.getName());
		Assert.assertEquals("value2", attribute.getValue());
	}

	@Test
	public void testDeleteByOwner()
	{
		SimpleEntity entity = new SimpleEntity();
		simpleEntityDao.save(entity);
		
		SimpleEntityAttribute attribute1 = new SimpleEntityAttribute(entity, "name", "value");
		simpleEntityAttributeDao.save(attribute1);
		SimpleEntityAttribute attribute2 = new SimpleEntityAttribute(entity, "name2", "value2");
		simpleEntityAttributeDao.save(attribute2);
		
		Assert.assertEquals(2, simpleEntityAttributeDao.findByOwner(entity.getId()).size());
		simpleEntityAttributeDao.deleteByOwner(entity.getId());
		Assert.assertEquals(0, simpleEntityAttributeDao.findByOwner(entity.getId()).size());
	}
}
