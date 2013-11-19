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
package com.brienwheeler.svc.attrs.impl;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.brienwheeler.lib.db.dao.ISimpleEntityAttributeDao;
import com.brienwheeler.lib.db.dao.ISimpleEntityDao;
import com.brienwheeler.lib.db.domain.SimpleEntity;
import com.brienwheeler.lib.db.domain.SimpleEntityAttribute;
import com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer;
import com.brienwheeler.lib.svc.impl.ServiceBaseTestUtil;

@ContextConfiguration({
    "classpath:com/brienwheeler/lib/spring/beans/contextBeanDumper.xml",
    "classpath:com/brienwheeler/svc/attrs/PersistentAttributeServiceBase-testContext.xml" })
public class PersistentAttributeServiceBaseTest extends AbstractJUnit4SpringContextTests
{
	protected ISimpleEntityDao simpleEntityDao;
	protected ISimpleEntityAttributeDao simpleEntityAttributeDao;
	protected ISimpleEntityAttributeService simpleEntityAttributeService;
	
    @BeforeClass
    public static void oneTimeSetUp()
    {
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/lib/db/test.properties");
    }

    @Before
    public void onSetUp()
    {
        simpleEntityDao = applicationContext.getBean("com.brienwheeler.lib.db.dao.simpleEntityDao",
        		ISimpleEntityDao.class);
        simpleEntityAttributeDao = applicationContext.getBean("com.brienwheeler.lib.db.dao.simpleEntityAttributeDao",
        		ISimpleEntityAttributeDao.class);
        simpleEntityAttributeService = applicationContext.getBean("com.brienwheeler.svc.attrs.simpleEntityAttributeService",
        		ISimpleEntityAttributeService.class);
    }

    @Test
	public void testGetAttributes()
	{
		SimpleEntity entity = new SimpleEntity();
		simpleEntityDao.save(entity);
		
		SimpleEntityAttribute attribute1 = new SimpleEntityAttribute(entity, "name1", "value1");
		simpleEntityAttributeDao.save(attribute1);
		SimpleEntityAttribute attribute2 = new SimpleEntityAttribute(entity, "name2", "value2");
		simpleEntityAttributeDao.save(attribute2);
		
		ServiceBaseTestUtil.clearWorkRecords(simpleEntityAttributeService);
		
		Map<String,String> attributeMap = simpleEntityAttributeService.getAttributes(entity.getDbId());
		Assert.assertEquals(2, attributeMap.size());

		ServiceBaseTestUtil.verifyWorkRecord(simpleEntityAttributeService, "getAttribute",  1,  0);
	}

    @Test
	public void testGetAttribute()
	{
		SimpleEntity entity = new SimpleEntity();
		simpleEntityDao.save(entity);
		
		SimpleEntityAttribute attribute1 = new SimpleEntityAttribute(entity, "name1", "value1");
		simpleEntityAttributeDao.save(attribute1);
		
		ServiceBaseTestUtil.clearWorkRecords(simpleEntityAttributeService);

		String value = simpleEntityAttributeService.getAttribute(entity.getDbId(), "name2");
		Assert.assertNull(value);

		value = simpleEntityAttributeService.getAttribute(entity.getDbId(), "name1");
		Assert.assertEquals("value1", value);

		ServiceBaseTestUtil.verifyWorkRecord(simpleEntityAttributeService, "getAttribute",  2,  0);
}

    @Test
	public void testSetAttributeModify()
	{
		SimpleEntity entity = new SimpleEntity();
		simpleEntityDao.save(entity);
		
		SimpleEntityAttribute attribute1 = new SimpleEntityAttribute(entity, "name1", "value1");
		simpleEntityAttributeDao.save(attribute1);
		
		String value = simpleEntityAttributeService.getAttribute(entity.getDbId(), "name1");
		Assert.assertEquals("value1", value);

		ServiceBaseTestUtil.clearWorkRecords(simpleEntityAttributeService);

		simpleEntityAttributeService.setAttribute(entity, "name1", "value2");

		ServiceBaseTestUtil.verifyWorkRecord(simpleEntityAttributeService, "setAttribute",  1,  0);

		value = simpleEntityAttributeService.getAttribute(entity.getDbId(), "name1");
		Assert.assertEquals("value2", value);
	}

    @Test
	public void testSetAttributeCreate()
	{
		SimpleEntity entity = new SimpleEntity();
		simpleEntityDao.save(entity);
		
		String value = simpleEntityAttributeService.getAttribute(entity.getDbId(), "name1");
		Assert.assertNull(value);
		
		ServiceBaseTestUtil.clearWorkRecords(simpleEntityAttributeService);

		simpleEntityAttributeService.setAttribute(entity, "name1", "value1");
		
		ServiceBaseTestUtil.verifyWorkRecord(simpleEntityAttributeService, "setAttribute",  1,  0);

		value = simpleEntityAttributeService.getAttribute(entity.getDbId(), "name1");
		Assert.assertEquals("value1", value);
	}
}
