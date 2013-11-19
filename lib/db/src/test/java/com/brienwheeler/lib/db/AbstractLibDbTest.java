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

import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.brienwheeler.lib.db.dao.ISimpleEntity2Dao;
import com.brienwheeler.lib.db.dao.ISimpleEntityAttributeDao;
import com.brienwheeler.lib.db.dao.ISimpleEntityDao;
import com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer;

@ContextConfiguration({
    "classpath:com/brienwheeler/lib/spring/beans/contextBeanDumper.xml",
    "classpath:com/brienwheeler/lib/db/AbstractLibDb-testContext.xml" })
public abstract class AbstractLibDbTest extends AbstractJUnit4SpringContextTests
{
	protected ISimpleEntityDao simpleEntityDao;
	protected ISimpleEntityAttributeDao simpleEntityAttributeDao;
	protected ISimpleEntity2Dao simpleEntity2Dao;
	
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
        simpleEntity2Dao = applicationContext.getBean("com.brienwheeler.lib.db.dao.simpleEntity2Dao",
        		ISimpleEntity2Dao.class);
    }
    
}
