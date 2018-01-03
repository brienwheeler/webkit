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

import com.brienwheeler.lib.test.ProtectedConstructorTestBase;
import com.brienwheeler.lib.util.ValidationException;

public class UserTest extends ProtectedConstructorTestBase<User>
{
	private static final String TEST_USERNAME_1 = "username1";
	private static final String TEST_HASHED_PASSWORD_1 = "hashedPassword1";
	private static final String TEST_HASHED_PASSWORD_2 = "hashedPassword2";
	
	@Override
	protected Class<User> getTargetClass()
	{
		return User.class;
	}

	@Test(expected=ValidationException.class)
	public void testConstructNullUser()
	{
		new User(null, TEST_HASHED_PASSWORD_1);
	}
	
	@Test(expected=ValidationException.class)
	public void testConstructNullHashedPassword()
	{
		new User(TEST_USERNAME_1, null);
	}
	
	@Test
	public void testConstruct()
	{
		User user = new User(TEST_USERNAME_1, TEST_HASHED_PASSWORD_1);
		Assert.assertEquals(TEST_USERNAME_1, user.getUsername());
		Assert.assertEquals(TEST_HASHED_PASSWORD_1, user.getHashedPassword());
		Assert.assertEquals(true, user.isEnabled());
		Assert.assertEquals(false, user.isAccountExpired());
		Assert.assertEquals(false, user.isCredentialsExpired());
		Assert.assertEquals(false, user.isAccountLocked());
	}
	
	@Test
	public void testSetPassword()
	{
		User user = new User(TEST_USERNAME_1, TEST_HASHED_PASSWORD_1);
		Assert.assertEquals(TEST_HASHED_PASSWORD_1, user.getHashedPassword());
		user.setHashedPassword(TEST_HASHED_PASSWORD_2);
		Assert.assertEquals(TEST_HASHED_PASSWORD_2, user.getHashedPassword());
	}

	@Test
	public void testSetEnabled()
	{
		User user = new User(TEST_USERNAME_1, TEST_HASHED_PASSWORD_1);
		Assert.assertEquals(true, user.isEnabled());
		user.setEnabled(false);
		Assert.assertEquals(false, user.isEnabled());
	}

	@Test
	public void testSetAccountExpired()
	{
		User user = new User(TEST_USERNAME_1, TEST_HASHED_PASSWORD_1);
		Assert.assertEquals(false, user.isAccountExpired());
		user.setAccountExpired(true);
		Assert.assertEquals(true, user.isAccountExpired());
	}

	@Test
	public void testSetCredentialsExpired()
	{
		User user = new User(TEST_USERNAME_1, TEST_HASHED_PASSWORD_1);
		Assert.assertEquals(false, user.isCredentialsExpired());
		user.setCredentialsExpired(true);
		Assert.assertEquals(true, user.isCredentialsExpired());
	}

	@Test
	public void testSetAccountLocked()
	{
		User user = new User(TEST_USERNAME_1, TEST_HASHED_PASSWORD_1);
		Assert.assertEquals(false, user.isAccountLocked());
		user.setAccountLocked(true);
		Assert.assertEquals(true, user.isAccountLocked());
	}

}
