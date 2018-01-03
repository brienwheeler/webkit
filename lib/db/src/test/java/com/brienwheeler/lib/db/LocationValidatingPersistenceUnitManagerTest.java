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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.brienwheeler.lib.util.ValidationException;

public class LocationValidatingPersistenceUnitManagerTest
{
	private LocationValidatingPersistenceUnitManager persistenceUnitManager;
	
	@Before
	public void onSetUp()
	{
		persistenceUnitManager = new LocationValidatingPersistenceUnitManager();
	}
	
	@Test(expected=ValidationException.class)
	public void testSetCommaSeparatedXmlLocationsNull()
	{
		persistenceUnitManager.setCommaSeparatedXmlLocations(null);
	}

	@Test
	public void testSetCommaSeparatedXmlLocationsEmpty()
	{
		persistenceUnitManager.setCommaSeparatedXmlLocations("");
		String[] locations = (String []) ReflectionTestUtils.getField(persistenceUnitManager, "persistenceXmlLocations");
		Assert.assertEquals(0, locations.length);
	}

	@Test
	public void testSetCommaSeparatedXmlLocations()
	{
		persistenceUnitManager.setCommaSeparatedXmlLocations("  classpath:com/brienwheeler/lib/db/persistence-test.xml  ,  classpath:com/brienwheeler/lib/db/bad-location.xml  ");
		String[] locations = (String []) ReflectionTestUtils.getField(persistenceUnitManager, "persistenceXmlLocations");
		Assert.assertEquals(1, locations.length);
		Assert.assertEquals("classpath:com/brienwheeler/lib/db/persistence-test.xml", locations[0]);
	}
	
	@Test
	public void testNullAndEmptyPersistenceLocations()
	{
		persistenceUnitManager.setPersistenceXmlLocations(new String[] { null, "" });
	}
}
