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
package com.brienwheeler.svc.users.validation;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.email.VerifiableEmailAddress;
import com.brienwheeler.svc.users.domain.User;
import com.brienwheeler.svc.users.impl.AbstractSvcUsersTest;

public class EmailAddressNotExistValidatorTest extends AbstractSvcUsersTest
{
	@EmailAddressNotExist
	private String emailAddress;
	
	@Test
	public void testIsValid() throws NoSuchFieldException
	{
		EmailAddressNotExistValidator validator = new EmailAddressNotExistValidator();
		validator.setApplicationContext(applicationContext);
		
		Field emailAddressField = getClass().getDeclaredField("emailAddress");
		EmailAddressNotExist annotation = emailAddressField.getAnnotation(EmailAddressNotExist.class);
		validator.initialize(annotation);
		
		User user = makeUnpersistedUser();
		VerifiableEmailAddress address = makeUnpersistedEmailAddress(user);
		
		Assert.assertTrue(validator.isValid(address.getAddress(), null));
		
		user = makePersistedUser(applicationContext);
		address = makePersistedEmailAddress(applicationContext, user);
		Assert.assertFalse(validator.isValid(address.getAddress(), null));
	}
}
