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

import com.brienwheeler.lib.email.VerifiableEmailAddress.Status;
import com.brienwheeler.lib.test.ProtectedConstructorTestBase;

public class VerifiableEmailAddressTest extends ProtectedConstructorTestBase<VerifiableEmailAddress>
{
	private static final String TEST_ADDRESS_1 = "address1@domain1.com";
	private static final String TEST_ADDRESS_2 = "address2@domain2.com";
	
	@Override
	protected Class<VerifiableEmailAddress> getTargetClass()
	{
		return VerifiableEmailAddress.class;
	}

	@Test
	public void testConstructDefault()
	{
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(TEST_ADDRESS_1);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(Status.UNVERIFIED, emailAddress.getStatus());
		Assert.assertEquals(false, emailAddress.isVerified());
	}
	
	@Test
	public void testConstructWithStatusUnverified()
	{
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(TEST_ADDRESS_1,
				Status.UNVERIFIED);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(Status.UNVERIFIED, emailAddress.getStatus());
		Assert.assertEquals(false, emailAddress.isVerified());
	}

	@Test
	public void testConstructWithStatusVerified()
	{
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(TEST_ADDRESS_1,
				Status.VERIFIED);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(Status.VERIFIED, emailAddress.getStatus());
		Assert.assertEquals(true, emailAddress.isVerified());
	}
	
	@Test
	public void testConstructCopyEA()
	{
		EmailAddress src = EmailAddressTest.getTestEmailAddress1();
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(src);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(src.getAddress(), emailAddress.getAddress());
		Assert.assertEquals(Status.UNVERIFIED, emailAddress.getStatus());
	}
	
	@Test
	public void testConstructCopyVEAUnverified()
	{
		EmailAddress src = new VerifiableEmailAddress(TEST_ADDRESS_1, Status.UNVERIFIED);
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(src);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(src.getAddress(), emailAddress.getAddress());
		Assert.assertEquals(Status.UNVERIFIED, emailAddress.getStatus());
	}

	@Test
	public void testConstructCopyVEAVerified()
	{
		EmailAddress src = new VerifiableEmailAddress(TEST_ADDRESS_1, Status.VERIFIED);
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(src);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(src.getAddress(), emailAddress.getAddress());
		Assert.assertEquals(Status.VERIFIED, emailAddress.getStatus());
	}

	@Test
	public void testSetStatus()
	{
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(TEST_ADDRESS_1,
				Status.UNVERIFIED);
		Assert.assertNotNull(emailAddress);
		Assert.assertEquals(Status.UNVERIFIED, emailAddress.getStatus());
		emailAddress.setStatus(Status.VERIFIED);
		Assert.assertEquals(Status.VERIFIED, emailAddress.getStatus());
	}
	
	@Test
	public void testEqualsThis()
	{
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(TEST_ADDRESS_1);
		Assert.assertNotNull(emailAddress);
		Assert.assertTrue(emailAddress.equals(emailAddress));
	}

	@Test
	public void testEqualsNull()
	{
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(TEST_ADDRESS_1);
		Assert.assertNotNull(emailAddress);
		Assert.assertFalse(emailAddress.equals(null));
	}

	@Test
	public void testEqualsDifferentClass()
	{
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(TEST_ADDRESS_1);
		Assert.assertNotNull(emailAddress);
		Assert.assertFalse(emailAddress.equals(new Object()));
	}

	@Test
	public void testEqualsEqual()
	{
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(TEST_ADDRESS_1, Status.VERIFIED);
		Assert.assertNotNull(emailAddress);
		Assert.assertTrue(emailAddress.equals(new VerifiableEmailAddress(TEST_ADDRESS_1, Status.VERIFIED)));
	}

	@Test
	public void testEqualsNotEqualAddress()
	{
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(TEST_ADDRESS_1, Status.VERIFIED);
		Assert.assertNotNull(emailAddress);
		Assert.assertFalse(emailAddress.equals(new VerifiableEmailAddress(TEST_ADDRESS_2, Status.VERIFIED)));
	}

	@Test
	public void testEqualsNotEqualStatus()
	{
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(TEST_ADDRESS_1, Status.VERIFIED);
		Assert.assertNotNull(emailAddress);
		Assert.assertFalse(emailAddress.equals(new VerifiableEmailAddress(TEST_ADDRESS_1, Status.UNVERIFIED)));
	}
}
