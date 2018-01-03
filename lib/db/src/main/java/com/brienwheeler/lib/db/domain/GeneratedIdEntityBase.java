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

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.proxy.HibernateProxyHelper;

/**
 * A common base class for all database entity objects.  It includes a generated long
 * id value (which will be 0 for all unpersisted objects), a useful toString() method,
 * and hashCode() and equals() methods for good behavior with respect to Collections.
 *  
 * @author Brien Wheeler
 */
@MappedSuperclass
public abstract class GeneratedIdEntityBase<EntityType extends GeneratedIdEntityBase<EntityType>>
{
    @GeneratedValue
    @Id
    private long id;

    /**
     * @return the Entity's database id, or 0 if not yet persisted
     */
    public long getId()
    {
        return id;
    }

    @SuppressWarnings("unchecked")
	public DbId<EntityType> getDbId()
    {
    	return new DbId<EntityType>((Class<EntityType>) getClass(), id);
    }
    
    /**
     * @return "[ClassName:unpersisted]" or "[ClassName:<id>]" 
     */
    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(getClass().getSimpleName());
        buffer.append(":");
        buffer.append((id != 0) ? Long.toString(id) : "unpersisted");
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * @return if persisted, the integer value of the database id, otherwise the
     * System.identityHashCode of the object
     */
    @Override
    public int hashCode()
    {
        return (id != 0) ? (int) id : System.identityHashCode(this); 
    }

    /**
     * @return true if obj is the same JVM object as this, or obj and this are
     * both persisted database objects of the same class with the same id.
     */
    @Override
    public boolean equals(Object obj)
    {
        return (this == obj) || (obj != null) &&
        		HibernateProxyHelper.getClassWithoutInitializingProxy(this).equals(HibernateProxyHelper.getClassWithoutInitializingProxy(obj)) &&
                (getId() != 0) && (getId() == ((GeneratedIdEntityBase<?>) obj).getId()); 
    }
}
