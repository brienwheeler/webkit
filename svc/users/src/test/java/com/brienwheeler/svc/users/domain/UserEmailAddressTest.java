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
package com.brienwheeler.svc.users.domain;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.email.AbstractLibEmailTest;
import com.brienwheeler.lib.email.EmailAddress;
import com.brienwheeler.lib.email.VerifiableEmailAddress;
import com.brienwheeler.lib.test.ProtectedConstructorTestBase;
import com.brienwheeler.lib.util.ValidationException;
import com.brienwheeler.svc.users.impl.AbstractSvcUsersTest;

public class UserEmailAddressTest extends ProtectedConstructorTestBase<UserEmailAddress>
{
	@Override
	protected Class<UserEmailAddress> getTargetClass()
	{
		return UserEmailAddress.class;
	}

	@Test(expected=ValidationException.class)
	public void testConstructNullUser()
	{
		new UserEmailAddress(null, AbstractLibEmailTest.makeEmailAddress());
	}

	@Test(expected=ValidationException.class)
	public void testConstructNullRole()
	{
		new UserEmailAddress(AbstractSvcUsersTest.makeUnpersistedUser(), null);
	}

	@Test
	public void testConstruct()
	{
		User user = AbstractSvcUsersTest.makeUnpersistedUser();
		EmailAddress emailAddress = AbstractLibEmailTest.makeEmailAddress();
		UserEmailAddress userEmailAddress = new UserEmailAddress(user, emailAddress);
		Assert.assertTrue(user == userEmailAddress.getUser());
		Assert.assertEquals(EmailAddress.class, emailAddress.getClass());
		Assert.assertEquals(VerifiableEmailAddress.class, userEmailAddress.getEmailAddress().getClass());
		Assert.assertEquals(emailAddress.getAddress(), userEmailAddress.getEmailAddress().getAddress());
	}
	
}
