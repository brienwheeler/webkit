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

import java.util.ArrayList;
import java.util.List;

import com.brienwheeler.lib.monitor.work.MonitoredWork;
import com.brienwheeler.lib.svc.GracefulShutdown;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.brienwheeler.lib.email.EmailAddress;
import com.brienwheeler.lib.email.VerifiableEmailAddress;
import com.brienwheeler.lib.svc.impl.SpringStoppableServiceBase;
import com.brienwheeler.lib.util.OperationDisallowedException;
import com.brienwheeler.lib.util.ValidationUtils;
import com.brienwheeler.svc.users.DuplicateUserEmailAddressException;
import com.brienwheeler.svc.users.IUserEmailAddressService;
import com.brienwheeler.svc.users.domain.User;
import com.brienwheeler.svc.users.domain.UserEmailAddress;

public class UserEmailAddressService extends SpringStoppableServiceBase implements IUserEmailAddressService
{
	private IUserEmailAddressDao userEmailAddressDao;
	
	@Required
	public void setUserEmailAddressDao(IUserEmailAddressDao userEmailAddressDao)
	{
		this.userEmailAddressDao = userEmailAddressDao;
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	@MonitoredWork
    @GracefulShutdown
	public User findByEmailAddress(EmailAddress emailAddress)
	{
		ValidationUtils.assertNotNull(emailAddress, "emailAddress cannot be null");
		
		UserEmailAddress userEmailAddress = userEmailAddressDao.findByEmailAddress(emailAddress);
		return userEmailAddress == null ? null : userEmailAddress.getUser();
	}

	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	@MonitoredWork
    @GracefulShutdown
	public List<VerifiableEmailAddress> findByUser(User user)
	{
		ValidationUtils.assertNotNull(user, "user cannot be null");

		List<UserEmailAddress> userEmailAddresses = userEmailAddressDao.findByUser(user);
		List<VerifiableEmailAddress> emailAddresses = new ArrayList<VerifiableEmailAddress>();
		for (UserEmailAddress userEmailAddress : userEmailAddresses)
			emailAddresses.add(userEmailAddress.getEmailAddress());
		return emailAddresses;
	}

	@Override
	@Transactional
	@MonitoredWork
    @GracefulShutdown
	public void addEmailAddress(User user, VerifiableEmailAddress emailAddress)
	{
		ValidationUtils.assertNotNull(user, "user cannot be null");
		ValidationUtils.assertNotNull(emailAddress, "emailAddress cannot be null");
		
		User existingUser = findByEmailAddress(emailAddress);
		if (existingUser != null)
		{
			if (existingUser.equals(user))
				return;
			throw new DuplicateUserEmailAddressException("email address " + emailAddress.getAddress() + " already in use");
		}
		
		userEmailAddressDao.save(new UserEmailAddress(user, emailAddress));
	}

	@Override
	@Transactional
	@MonitoredWork
    @GracefulShutdown
	public boolean removeEmailAddress(User user, EmailAddress emailAddress)
	{
			ValidationUtils.assertNotNull(user, "user cannot be null");
			ValidationUtils.assertNotNull(emailAddress, "emailAddress cannot be null");
			
			UserEmailAddress existing = userEmailAddressDao.findByEmailAddress(emailAddress);
			if (existing == null)
				return false;
			
			if (!existing.getUser().equals(user))
				throw new OperationDisallowedException(user.toString() + " is not associated with email address " + emailAddress.getAddress());
			
			userEmailAddressDao.delete(existing);
			return true;
	}
}
