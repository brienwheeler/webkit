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
package com.brienwheeler.lib.email;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.util.ValidationException;

public class EmailAddressTest
{
	private static final String L_OK = "abcdefghijklmnopqrstuvwxyz0123456789_%+-";
	private static final String D_OK = "abcdefghijklmnopqrstuvwxyz0123456789-";
	
	public static final String TEST_ADDRESS_1 = "address1@domain1.com";
	public static final String TEST_ADDRESS_2 = "address2@domain2.com";
	
	public static EmailAddress getTestEmailAddress1()
	{
		return new EmailAddress(TEST_ADDRESS_1);
	}
	
	public static EmailAddress getTestEmailAddress2()
	{
		return new EmailAddress(TEST_ADDRESS_2);
	}

	@Test
	public void testIsValidNull()
	{
		Assert.assertEquals(false, EmailAddress.isValid(null));
	}
	
	@Test
	public void testIsValidValid()
	{
		Assert.assertEquals(true, EmailAddress.isValid(TEST_ADDRESS_1));
	}
	
	@Test
	public void testIsValidInvalid()
	{
		Assert.assertEquals(false, EmailAddress.isValid(""));
	}
	
	@Test(expected=ValidationException.class)
	public void testIsValid2Null()
	{
		Assert.assertEquals(true, EmailAddress.isValid(null, true));
	}
	
	@Test
	public void testIsValid2Valid()
	{
		Assert.assertEquals(true, EmailAddress.isValid(TEST_ADDRESS_1, true));
	}
	
	@Test
	public void testIsValid2InvalidNoThrow()
	{
		Assert.assertEquals(false, EmailAddress.isValid("", false));
	}
	
	@Test(expected=ValidationException.class)
	public void testIsValid2InvalidThrow()
	{
		Assert.assertEquals(false, EmailAddress.isValid("", true));
	}
	
	@Test(expected=ValidationException.class)
	public void testFailNull()
	{
		new EmailAddress(null);
	}
	
	@Test(expected=ValidationException.class)
	public void testFailEmpty()
	{
		new EmailAddress("");
	}

	@Test
	public void testOkLocalNoDot()
	{
		new EmailAddress(L_OK + "@" + D_OK + ".com");
	}

	@Test
	public void testOkLocalOneDot()
	{
		new EmailAddress(L_OK + "." + L_OK + "@" + D_OK + ".com");
	}

	@Test
	public void testOkLocalTwoDots()
	{
		new EmailAddress(L_OK + "." + L_OK + "." + L_OK + "@" + D_OK + ".com");
	}

	@Test(expected=ValidationException.class)
	public void testFailLocalStartsWithBadChar()
	{
		new EmailAddress("(" + L_OK + "@" + D_OK + ".com");
	}

	@Test(expected=ValidationException.class)
	public void testFailLocalStartsWithDot()
	{
		new EmailAddress("." + L_OK + "@" + D_OK + ".com");
	}

	@Test(expected=ValidationException.class)
	public void testFailLocalEndsWithDot()
	{
		new EmailAddress(L_OK + ".@" + D_OK + ".com");
	}

	@Test(expected=ValidationException.class)
	public void testFailLocalConsecutiveDots()
	{
		new EmailAddress(L_OK + ".." + L_OK + "@" + D_OK + ".com");
	}

	@Test(expected=ValidationException.class)
	public void testFailDomainNoDot()
	{
		new EmailAddress(L_OK + "@" + D_OK);
	}

	@Test
	public void testOkDomainTwoDots()
	{
		new EmailAddress(L_OK + "." + L_OK + "@" + D_OK + "." + D_OK + ".com");
	}

	@Test(expected=ValidationException.class)
	public void testFailDomainConsecutiveDots()
	{
		new EmailAddress(L_OK + "." + L_OK + "@" + D_OK + "..com");
	}

	@Test(expected=ValidationException.class)
	public void testFailTopDomainTooLong()
	{
		new EmailAddress(L_OK + "@" + D_OK + ".abcde");
	}
	
	@Test
	public void testGetEmailAddressString()
	{
		EmailAddress emailAddress = new EmailAddress(TEST_ADDRESS_1);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(TEST_ADDRESS_1, emailAddress.getAddress());
	}
	
	@Test
	public void testHashCode()
	{
		EmailAddress emailAddress = new EmailAddress(TEST_ADDRESS_1);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(TEST_ADDRESS_1.hashCode(), emailAddress.hashCode());
	}

	@Test
	public void testEqualsThis()
	{
		EmailAddress emailAddress = new EmailAddress(TEST_ADDRESS_1);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(true, emailAddress.equals(emailAddress));
	}

	@Test
	public void testEqualsNull()
	{
		EmailAddress emailAddress = new EmailAddress(TEST_ADDRESS_1);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(false, emailAddress.equals(null));
	}

	@Test
	public void testEqualsDifferentClass()
	{
		EmailAddress emailAddress = new EmailAddress(TEST_ADDRESS_1);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(false, emailAddress.equals(new Object()));
	}

	@Test
	public void testEqualsEqual()
	{
		EmailAddress emailAddress = new EmailAddress(TEST_ADDRESS_1);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(true, emailAddress.equals(new EmailAddress(TEST_ADDRESS_1)));
	}
	@Test
	public void testEqualsNotEqual()
	{
		EmailAddress emailAddress = new EmailAddress(TEST_ADDRESS_1);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(false, emailAddress.equals(new EmailAddress(TEST_ADDRESS_2)));
	}
	
	@Test
	public void testCompareTo()
	{
		EmailAddress emailAddress1 = new EmailAddress(TEST_ADDRESS_1);
		EmailAddress emailAddress2 = new EmailAddress(TEST_ADDRESS_2);
		
		Assert.assertTrue(emailAddress1.compareTo(emailAddress2) < 0);
		Assert.assertTrue(emailAddress1.compareTo(emailAddress1) == 0);
		Assert.assertTrue(emailAddress2.compareTo(emailAddress1) > 0);
	}
	
	@Test
	public void testProtectedConstruct()
	{
		Assert.assertNotNull(new EmailAddressSubclass());
	}
	
	private static class EmailAddressSubclass extends EmailAddress
	{
	}
}
