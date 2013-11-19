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

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.brienwheeler.lib.db.domain.DbId;
import com.brienwheeler.lib.svc.MonitoredWork;
import com.brienwheeler.lib.svc.impl.SpringStoppableServiceBase;
import com.brienwheeler.lib.util.ValidationUtils;
import com.brienwheeler.svc.users.DuplicateUserException;
import com.brienwheeler.svc.users.IUserService;
import com.brienwheeler.svc.users.domain.User;

public class UserService extends SpringStoppableServiceBase implements IUserService
{
	private IUserDao userDao;
	
	@Override
	@Transactional
	@MonitoredWork
	public User createUser(String username, String hashedPassword, CreateUserCallback... callbacks)
	{
		username = ValidationUtils.assertNotEmpty(username, "username cannot be empty");
		hashedPassword = ValidationUtils.assertNotEmpty(hashedPassword, "hashedPassword cannot be empty");
	
		if (findByUsername(username) != null)
			throw new DuplicateUserException("username already exists: " + username);

		User user = userDao.save(new User(username, hashedPassword));
		
		if (callbacks != null)
		{
			for (CreateUserCallback callback : callbacks)
				callback.userCreated(user);
		}
		
		return user;
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	@MonitoredWork
	public User findById(DbId<User> userId)
	{
		return userDao.findById(userId.getId());
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	@MonitoredWork
	public User findByUsername(String username)
	{
		username = ValidationUtils.assertNotEmpty(username, "username cannot be empty");
		
		return userDao.findByUsername(username);
	}

	@Override
	@Transactional
	@MonitoredWork
	public void setNewPassword(User user, String newHashedPassword)
	{
		ValidationUtils.assertNotNull(user, "user cannot be null");
		newHashedPassword = ValidationUtils.assertNotEmpty(newHashedPassword, "newHashedPassword cannot be empty");
		
		user.setHashedPassword(newHashedPassword);
		userDao.save(user);
	}
	
	@Required
	public void setUserDao(IUserDao userDao)
	{
		this.userDao = userDao;
	}
}
