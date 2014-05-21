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

import com.brienwheeler.lib.monitor.work.MonitoredWork;
import com.brienwheeler.lib.svc.GracefulShutdown;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.brienwheeler.lib.db.DbValidationUtils;
import com.brienwheeler.lib.svc.impl.SpringStoppableServiceBase;
import com.brienwheeler.lib.util.ArrayUtils;
import com.brienwheeler.lib.util.ValidationUtils;
import com.brienwheeler.svc.users.IUserRoleService;
import com.brienwheeler.svc.users.domain.User;
import com.brienwheeler.svc.users.domain.UserRole;

public class UserRoleService extends SpringStoppableServiceBase implements IUserRoleService
{
	private IUserRoleDao userRoleDao;
	
	@Required
	public void setUserRoleDao(IUserRoleDao userRoleDao)
	{
		this.userRoleDao = userRoleDao;
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	@MonitoredWork
    @GracefulShutdown
	public List<String> getUserRoles(User user)
	{
		DbValidationUtils.assertPersisted(user);
		
		return userRoleDao.findByUserAsStrings(user);
	}
	
	@Override
	@Transactional
	@MonitoredWork
    @GracefulShutdown
    public void setUserRoles(final User user, final String... roles)
	{
		DbValidationUtils.assertPersisted(user);
		ValidationUtils.assertNotNull(roles, "roles cannot be null");

		List<UserRole> currentRoles = userRoleDao.findByUser(user);
		
		for (UserRole currentRole : currentRoles)
		{
			if (!ArrayUtils.contains(roles, currentRole.getRole()))
				userRoleDao.delete(currentRole);
		}
		
		for (String role : roles)
		{
			if (!userRoleListContains(currentRoles, role))
				userRoleDao.save(new UserRole(user, role));
		}
	}

	private boolean userRoleListContains(List<UserRole> roleList, String role)
	{
		for (UserRole userRole : roleList)
			if (userRole.getRole().equals(role))
				return true;
		return false;
	}
}
