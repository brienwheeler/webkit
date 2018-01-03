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
package com.brienwheeler.lib.db.domain;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.brienwheeler.lib.db.AbstractLibDbTest;

public class GeneratedIdEntityBaseTest extends AbstractLibDbTest
{
    @Test
    public void testGetIdUnpersisted()
    {
    	SimpleEntity entity = new SimpleEntity();
    	Assert.assertEquals(0, entity.getId());
    }

    @Test
    public void testGetDbIdUnpersisted()
    {
    	SimpleEntity entity = new SimpleEntity();
    	DbId<SimpleEntity> dbId = entity.getDbId();
    	Assert.assertEquals(0, dbId.getId());
    }

    @Test
    public void testToStringUnpersisted()
    {
    	SimpleEntity entity = new SimpleEntity();
    	Assert.assertTrue(entity.toString().contains(entity.getClass().getSimpleName()));
    	Assert.assertTrue(entity.toString().contains("unpersisted"));
    }

    @Test
    public void testHashCodeUnpersisted()
    {
    	SimpleEntity entity = new SimpleEntity();
    	Assert.assertEquals(System.identityHashCode(entity), entity.hashCode());
    }
    
    @Test
    public void testEqualsUnpersisted()
    {
    	SimpleEntity entityA = new SimpleEntity();
    	SimpleEntity entityB = new SimpleEntity();
    	SimpleEntity2 entity2 = new SimpleEntity2();
    	
    	Assert.assertTrue(entityA.equals(entityA));
    	Assert.assertFalse(entityA.equals(null));
    	Assert.assertFalse(entityA.equals(entityB));
    	Assert.assertFalse(entityA.equals(entity2));
    }

    @Test
    public void testGetIdPersisted() throws IOException
    {
    	SimpleEntity entity = new SimpleEntity();
    	simpleEntityDao.save(entity);
    	Assert.assertTrue(entity.getId() != 0);
    }

    @Test
    public void testGetDbIdPersisted() throws IOException
    {
    	SimpleEntity entity = new SimpleEntity();
    	simpleEntityDao.save(entity);
    	DbId<SimpleEntity> dbId = entity.getDbId();
    	Assert.assertTrue(dbId.getId() != 0);
    }

    @Test
    public void testToStringPersisted()
    {
    	SimpleEntity entity = new SimpleEntity();
    	simpleEntityDao.save(entity);
    	Assert.assertTrue(entity.toString().contains(entity.getClass().getSimpleName()));
    	Assert.assertTrue(entity.toString().contains(Long.toString(entity.getId())));
    }

    @Test
    public void testHashCodePersisted()
    {
    	SimpleEntity entity = new SimpleEntity();
    	simpleEntityDao.save(entity);
    	Assert.assertEquals((int) entity.getId(), entity.hashCode());
    }
    
    @Test
    public void testEqualsPersisted()
    {
    	SimpleEntity entityA = new SimpleEntity();
    	simpleEntityDao.save(entityA);
    	SimpleEntity entityB = new SimpleEntity();
    	simpleEntityDao.save(entityB);
    	SimpleEntity2 entity2 = new SimpleEntity2();
    	simpleEntity2Dao.save(entity2);
    	
    	SimpleEntity entityACopy = new SimpleEntity();
    	ReflectionTestUtils.setField(entityACopy, "id", entityA.getId());

    	Assert.assertNotSame(entityA, entityACopy);
    	Assert.assertTrue(entityA.equals(entityA));
    	Assert.assertTrue(entityA.equals(entityACopy));
    	Assert.assertFalse(entityA.equals(null));
    	Assert.assertFalse(entityA.equals(entityB));
    	Assert.assertFalse(entityA.equals(entity2));
    }

}
