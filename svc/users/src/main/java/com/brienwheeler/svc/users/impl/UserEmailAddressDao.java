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
package com.brienwheeler.svc.users.impl;

import java.util.List;

import javax.persistence.Query;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.brienwheeler.lib.db.dao.DeletableDaoBase;
import com.brienwheeler.lib.email.EmailAddress;
import com.brienwheeler.svc.users.domain.User;
import com.brienwheeler.svc.users.domain.UserEmailAddress;

public class UserEmailAddressDao extends DeletableDaoBase<UserEmailAddress>
		implements IUserEmailAddressDao
{
	@Override
	public Class<UserEmailAddress> getEntityClass()
	{
		return UserEmailAddress.class;
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	public UserEmailAddress findByEmailAddress(EmailAddress emailAddress)
	{
		Query query = entityManager.createQuery("from UserEmailAddress where emailAddress.address = :address");
		query.setParameter("address", emailAddress.getAddress());
		return getSingleResultOrNull(query);
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	@SuppressWarnings("unchecked")
	public List<UserEmailAddress> findByUser(User user)
	{
		Query query = entityManager.createQuery("from UserEmailAddress where user = :user order by emailAddress.address");
		query.setParameter("user", user);
		return (List<UserEmailAddress>) query.getResultList();
	}
}
