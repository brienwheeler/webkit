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
package com.brienwheeler.lib.db.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.brienwheeler.lib.db.domain.GeneratedIdEntityBase;
import com.brienwheeler.lib.util.ValidationUtils;

@Repository
public abstract class NonDeletableDaoBase<EntityType extends GeneratedIdEntityBase<EntityType>>
		implements INonDeletableDaoBase<EntityType>
{
	public abstract Class<EntityType> getEntityClass();

	@PersistenceContext
	protected EntityManager entityManager;
	
	@Override
	@Transactional
	public EntityType save(EntityType entity)
	{
		ValidationUtils.assertNotNull(entity, "entity cannot be null");
		
		entityManager.persist(entity);
		return entity;
	}
	
	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	public EntityType findById(long id)
	{
		return entityManager.find(getEntityClass(), id);
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	@SuppressWarnings("unchecked")
	public List<EntityType> findAll()
	{
		return (List<EntityType>) entityManager.createQuery("from " + getEntityClass().getSimpleName()).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T getSingleResultOrNull(Query query)
	{
		try {
			return (T) query.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}
}
