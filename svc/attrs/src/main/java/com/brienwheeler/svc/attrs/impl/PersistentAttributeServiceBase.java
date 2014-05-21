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

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.brienwheeler.lib.monitor.work.MonitoredWork;
import com.brienwheeler.lib.svc.GracefulShutdown;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.brienwheeler.lib.db.DbValidationUtils;
import com.brienwheeler.lib.db.dao.IPersistentAttributeDao;
import com.brienwheeler.lib.db.domain.DbId;
import com.brienwheeler.lib.db.domain.GeneratedIdEntityBase;
import com.brienwheeler.lib.db.domain.PersistentAttributeBase;
import com.brienwheeler.lib.svc.IStoppableService;
import com.brienwheeler.lib.svc.impl.SpringStoppableServiceBase;
import com.brienwheeler.lib.util.ReflectionException;
import com.brienwheeler.lib.util.ValidationUtils;
import com.brienwheeler.svc.attrs.IPersistentAttributeService;

public abstract class PersistentAttributeServiceBase<
			AttrClass extends PersistentAttributeBase<AttrClass,OwnerClass>,
			OwnerClass extends GeneratedIdEntityBase<OwnerClass>
		>
		extends SpringStoppableServiceBase
		implements IPersistentAttributeService<AttrClass, OwnerClass>, IStoppableService
{
	protected IPersistentAttributeDao<AttrClass,OwnerClass> persistentAttributeDao;

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	@MonitoredWork("getAttribute")
    @GracefulShutdown
	public Map<String, String> getAttributes(DbId<OwnerClass> owner)
	{
		DbValidationUtils.assertPersisted(owner);
		
		List<AttrClass> attributes = persistentAttributeDao.findByOwner(owner.getId());
		Map<String,String> attributeMap = new HashMap<String,String>(attributes.size());
		for (AttrClass attribute : attributes)
		{
			attributeMap.put(attribute.getName(), attribute.getValue());
		}
		return Collections.unmodifiableMap(attributeMap);
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	@MonitoredWork("getAttribute")
    @GracefulShutdown
	public String getAttribute(DbId<OwnerClass> owner, String name)
	{
		DbValidationUtils.assertPersisted(owner);
		name = ValidationUtils.assertNotEmpty(name, "name cannot be empty");

		AttrClass attribute = persistentAttributeDao.findByOwnerAndName(owner.getId(), name);
		return attribute == null ? null : attribute.getValue();
	}

	@Override
	@Transactional
	@MonitoredWork
    @GracefulShutdown
	public AttrClass setAttribute(OwnerClass owner, String name, String value)
	{
		DbValidationUtils.assertPersisted(owner);
		name = ValidationUtils.assertNotEmpty(name, "name cannot be empty");
		value = ValidationUtils.assertNotEmpty(value, "value cannot be empty");
		
		AttrClass attribute = persistentAttributeDao.findByOwnerAndName(owner.getId(), name);
		if (attribute != null)
			attribute.setValue(value);
		else
			attribute = createAttribute(owner, name, value);
		return persistentAttributeDao.save(attribute);
	}

	@Required
	public void setPersistentAttributeDao(IPersistentAttributeDao<AttrClass, OwnerClass> persistentAttributeDao)
	{
		this.persistentAttributeDao = persistentAttributeDao;
	}

	@SuppressWarnings("unchecked")
	private AttrClass createAttribute(OwnerClass owner, String name, String value)
	{
		Class<AttrClass> clazz = persistentAttributeDao.getEntityClass();
		Constructor<?>[] constructors = clazz.getConstructors();
		for (Constructor<?> constructor : constructors)
		{
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes.length == 3 &&
					parameterTypes[0].equals(owner.getClass()) &&
					parameterTypes[1].equals(String.class) &&
					parameterTypes[2].equals(String.class))
			{
				try {
					return (AttrClass) constructor.newInstance(owner, name, value);
				}
				catch (Exception e) {
					throw new ReflectionException("error instantiating object of type " + clazz.getSimpleName(), e);
				}
			}
		}
		
		throw new ReflectionException(clazz.getSimpleName() + " does not have " + clazz.getSimpleName() +
				"(" + owner.getClass().getSimpleName() + ",String,String) constructor");
	}
}
