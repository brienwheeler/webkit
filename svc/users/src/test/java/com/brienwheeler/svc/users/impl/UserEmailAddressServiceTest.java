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

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.email.EmailAddress;
import com.brienwheeler.lib.email.VerifiableEmailAddress;
import com.brienwheeler.lib.svc.impl.ServiceBaseTestUtil;
import com.brienwheeler.lib.util.OperationDisallowedException;
import com.brienwheeler.lib.util.ValidationException;
import com.brienwheeler.svc.users.DuplicateUserEmailAddressException;
import com.brienwheeler.svc.users.domain.User;

public class UserEmailAddressServiceTest extends AbstractSvcUsersTest
{
	@Test
	public void testFindByEmailNull()
	{
		ServiceBaseTestUtil.clearWorkRecords(userEmailAddressService);
		
		try {
			userEmailAddressService.findByEmailAddress(null);
			Assert.fail();
		}
		catch (ValidationException e) {
			// expected
		}

		ServiceBaseTestUtil.verifyWorkRecord(userEmailAddressService, "findByEmailAddress", 0, 1);
	}
	
	@Test
	public void testFindByVerifiableEmailAddress()
	{
		User user = makePersistedUser(applicationContext);
		VerifiableEmailAddress emailAddress = makePersistedEmailAddress(applicationContext, user);
		
		User fetched = userEmailAddressService.findByEmailAddress(emailAddress);
		Assert.assertNotSame(user, fetched);
		Assert.assertEquals(user, fetched);
	}

	//
	// This test is here because there was a bug in the findByEmailAddress query that caused an error
	// if you passed in an EmailAddress instead of a VerifiableEmailAddress
	//
	@Test
	public void testFindByEmailAddress()
	{
		User user = makePersistedUser(applicationContext);
		VerifiableEmailAddress emailAddress = makePersistedEmailAddress(applicationContext, user);

		EmailAddress searchEmailAddress = new EmailAddress(emailAddress.getAddress());		
		User fetched = userEmailAddressService.findByEmailAddress(searchEmailAddress);
		
		Assert.assertNotSame(user, fetched);
		Assert.assertEquals(user, fetched);
	}
	
	@Test
	public void testFindByUser()
	{
		User user = makePersistedUser(applicationContext);
		VerifiableEmailAddress emailAddress = makePersistedEmailAddress(applicationContext, user);
		
		VerifiableEmailAddress emailAddress2 = new VerifiableEmailAddress("two-" + emailAddress.getAddress());
		userEmailAddressService.addEmailAddress(user, emailAddress2);
		
		List<VerifiableEmailAddress> addresses = userEmailAddressService.findByUser(user);
		Assert.assertEquals(2, addresses.size());
	}
	
	@Test
	public void testAddDuplicateToSameUser()
	{
		User user = makePersistedUser(applicationContext);
		VerifiableEmailAddress emailAddress = makePersistedEmailAddress(applicationContext, user);

		VerifiableEmailAddress emailAddress2 = new VerifiableEmailAddress(emailAddress);
		userEmailAddressService.addEmailAddress(user, emailAddress2);

		List<VerifiableEmailAddress> addresses = userEmailAddressService.findByUser(user);
		Assert.assertEquals(1, addresses.size());
	}

	@Test(expected=DuplicateUserEmailAddressException.class)
	public void testAddDuplicateToDifferentUser()
	{
		User user = makePersistedUser(applicationContext);
		VerifiableEmailAddress emailAddress = makePersistedEmailAddress(applicationContext, user);
		
		User user2 = makePersistedUser(applicationContext);
		VerifiableEmailAddress emailAddress2 = new VerifiableEmailAddress(emailAddress);
		userEmailAddressService.addEmailAddress(user2, emailAddress2);
	}
	
	@Test(expected=ValidationException.class)
	public void testRemoveEmailAddressNullUser()
	{
		User user = makePersistedUser(applicationContext);
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(user.getUsername() + "@test.com");
		userEmailAddressService.removeEmailAddress(null, emailAddress);
	}

	@Test(expected=ValidationException.class)
	public void testRemoveEmailAddressNullEmail()
	{
		User user = makePersistedUser(applicationContext);
		userEmailAddressService.removeEmailAddress(user, null);
	}

	@Test
	public void testRemoveEmailAddressNonExistent()
	{
		User user = makePersistedUser(applicationContext);
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(user.getUsername() + "@test.com");
		Assert.assertFalse(userEmailAddressService.removeEmailAddress(user, emailAddress));
	}

	@Test(expected=OperationDisallowedException.class)
	public void testRemoveEmailAddressWrongUser()
	{
		User user = makePersistedUser(applicationContext);
		User user2 = makePersistedUser(applicationContext);
		VerifiableEmailAddress emailAddress = makePersistedEmailAddress(applicationContext, user);
		userEmailAddressService.removeEmailAddress(user2, emailAddress);
	}

	@Test
	public void testRemoveEmailAddress()
	{
		User user = makePersistedUser(applicationContext);
		VerifiableEmailAddress emailAddress = makePersistedEmailAddress(applicationContext, user);
		Assert.assertTrue(userEmailAddressService.removeEmailAddress(user, emailAddress));
	}
}
