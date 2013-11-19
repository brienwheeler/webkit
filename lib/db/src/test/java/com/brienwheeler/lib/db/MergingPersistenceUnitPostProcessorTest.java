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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.test.util.ReflectionTestUtils;

import com.brienwheeler.lib.util.ValidationException;

public class MergingPersistenceUnitPostProcessorTest
{
	private static final String TEST_PU_NAME = "TestPersistenceUnit";
	private static final String TEST_PROPERTY_NAME = "propertyName";
	private static final String TEST_PROPERTY_VALUE = "propertyValue";
	
	private MergingPersistenceUnitPostProcessor persistenceUnitPostProcessor;
	
	@Before
	public void onSetUp()
	{
		persistenceUnitPostProcessor = new MergingPersistenceUnitPostProcessor();		
	}
	
    @Test
    public void testSetProperties()
    {
    	Properties properties = new Properties();
    	persistenceUnitPostProcessor.setProperties(properties);
    	Assert.assertSame(properties, ReflectionTestUtils.getField(persistenceUnitPostProcessor, "properties"));
    }
    
    @Test(expected = ValidationException.class)
    public void testSetPropertiesNull()
    {
    	persistenceUnitPostProcessor.setProperties(null);
    }
    
    @Test
    public void testPostProcess()
    {
    	MutablePersistenceUnitInfo info1 = new MutablePersistenceUnitInfo();
    	info1.setPersistenceUnitName(TEST_PU_NAME);
    	List<String> classes1 = new ArrayList<String>();
    	classes1.add("com.brienwheeler.lib.db.domain.SimpleEntity");
    	ReflectionTestUtils.setField(info1, "managedClassNames", classes1);
    	
    	MutablePersistenceUnitInfo info2 = new MutablePersistenceUnitInfo();
    	info2.setPersistenceUnitName(TEST_PU_NAME);
    	List<String> classes2 = new ArrayList<String>();
    	classes2.add("com.brienwheeler.lib.db.domain.SimpleEntity2");
    	ReflectionTestUtils.setField(info2, "managedClassNames", classes2);
    	
    	persistenceUnitPostProcessor.postProcessPersistenceUnitInfo(info1);
    	persistenceUnitPostProcessor.postProcessPersistenceUnitInfo(info2);
    	
    	Assert.assertEquals(2, info2.getManagedClassNames().size());
    	Assert.assertEquals(0, info1.getProperties().size());
    	Assert.assertEquals(0, info2.getProperties().size());
    }

    @Test
    public void testPostProcessWithProperties()
    {
    	Properties properties = new Properties();
    	properties.setProperty(TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE);
    	persistenceUnitPostProcessor.setProperties(properties);

    	MutablePersistenceUnitInfo info1 = new MutablePersistenceUnitInfo();
    	info1.setPersistenceUnitName(TEST_PU_NAME);
    	List<String> classes1 = new ArrayList<String>();
    	classes1.add("com.brienwheeler.lib.db.domain.SimpleEntity");
    	ReflectionTestUtils.setField(info1, "managedClassNames", classes1);
    	
    	MutablePersistenceUnitInfo info2 = new MutablePersistenceUnitInfo();
    	info2.setPersistenceUnitName(TEST_PU_NAME);
    	List<String> classes2 = new ArrayList<String>();
    	classes2.add("com.brienwheeler.lib.db.domain.SimpleEntity2");
    	ReflectionTestUtils.setField(info2, "managedClassNames", classes2);
    	
    	persistenceUnitPostProcessor.postProcessPersistenceUnitInfo(info1);
    	persistenceUnitPostProcessor.postProcessPersistenceUnitInfo(info2);
    	
    	Assert.assertEquals(2, info2.getManagedClassNames().size());
    	Assert.assertSame(properties, info1.getProperties());
    	Assert.assertSame(properties, info2.getProperties());
    }
}
