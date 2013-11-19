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
package com.brienwheeler.svc.usergroups.impl;

import java.util.List;

import javax.persistence.Query;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.brienwheeler.lib.db.dao.DeletableDaoBase;
import com.brienwheeler.svc.usergroups.domain.UserGroup;
import com.brienwheeler.svc.usergroups.domain.UserGroupMember;
import com.brienwheeler.svc.users.domain.User;

public class UserGroupMemberDao extends DeletableDaoBase<UserGroupMember> implements IUserGroupMemberDao
{
	@Override
	public Class<UserGroupMember> getEntityClass()
	{
		return UserGroupMember.class;
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	public UserGroupMember findForGroupAndUser(UserGroup userGroup, User user)
	{
		Query query = entityManager.createQuery("from UserGroupMember where userGroup = :userGroup and user = :user");
		query.setParameter("userGroup", userGroup);
		query.setParameter("user", user);
		return getSingleResultOrNull(query);
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	@SuppressWarnings("unchecked")
	public List<UserGroupMember> findForUser(User user)
	{
		Query query = entityManager.createQuery("from UserGroupMember where user = :user");
		query.setParameter("user", user);
		return (List<UserGroupMember>) query.getResultList();
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	@SuppressWarnings("unchecked")
	public List<UserGroupMember> findForUserAndType(User user, String groupType)
	{
		Query query = entityManager.createQuery("from UserGroupMember u where u.user = :user and u.userGroup.groupType = :groupType");
		query.setParameter("user", user);
		query.setParameter("groupType", groupType);
		return (List<UserGroupMember>) query.getResultList();
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	@SuppressWarnings("unchecked")
	public List<UserGroupMember> findForGroup(UserGroup userGroup)
	{
		Query query = entityManager.createQuery("from UserGroupMember where userGroup = :userGroup");
		query.setParameter("userGroup", userGroup);
		return (List<UserGroupMember>) query.getResultList();
	}

}
