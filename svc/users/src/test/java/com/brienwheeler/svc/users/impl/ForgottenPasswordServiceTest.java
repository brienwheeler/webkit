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
package com.brienwheeler.svc.users.impl;

import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.brienwheeler.lib.email.EmailAddress;
import com.brienwheeler.lib.email.VerifiableEmailAddress;
import com.brienwheeler.lib.svc.impl.ServiceBaseTestUtil;
import com.brienwheeler.lib.test.spring.aop.AopTestUtils;
import com.brienwheeler.lib.util.EnvironmentTestUtils;
import com.brienwheeler.lib.util.EnvironmentUtils;
import com.brienwheeler.lib.util.OperationDisallowedException;
import com.brienwheeler.lib.util.ValidationException;
import com.brienwheeler.svc.email.IEmailService;
import com.brienwheeler.svc.users.ForgottenPasswordData;
import com.brienwheeler.svc.users.domain.User;

public class ForgottenPasswordServiceTest extends AbstractSvcUsersTest
{
	@Test
	public void testSendEmail()
	{
		IEmailService emailService = applicationContext.getBean("com.brienwheeler.svc.email.emailService",
				IEmailService.class);

		ServiceBaseTestUtil.clearWorkRecords(forgottenPasswordService);
		ServiceBaseTestUtil.clearWorkRecords(emailService);
		
		User user = makePersistedUser(applicationContext);
		VerifiableEmailAddress emailAddress = makePersistedEmailAddress(applicationContext, user);
		
		boolean sent = forgottenPasswordService.sendForgottenPasswordEmail(emailAddress);
		Assert.assertTrue(sent);
		
		ServiceBaseTestUtil.verifyWorkRecord(forgottenPasswordService, "sendForgottenPasswordEmail", 1, 0);
		ServiceBaseTestUtil.verifyWorkRecord(emailService, "sendEmail", 1, 0);
	}
	
	@Test
	public void testSendEmailNoSuchAddress()
	{
		User user = makeUnpersistedUser();
		EmailAddress emailAddress = new EmailAddress(user.getUsername() + "@test.com");
		
		ServiceBaseTestUtil.clearWorkRecords(forgottenPasswordService);

		boolean sent = forgottenPasswordService.sendForgottenPasswordEmail(emailAddress);
		Assert.assertFalse(sent);
		
		ServiceBaseTestUtil.verifyWorkRecord(forgottenPasswordService, "sendForgottenPasswordEmail", 1, 0);
	}
	
	@Test
	public void testSetDefaultSubject()
	{
		ForgottenPasswordService target = AopTestUtils.getTarget(forgottenPasswordService);
		target.setDefaultSubject("");
		Assert.assertEquals("", ReflectionTestUtils.getField(target, "defaultSubject"));
		target.setDefaultSubject("test subject");
		Assert.assertEquals("test subject", ReflectionTestUtils.getField(target, "defaultSubject"));
		target.setDefaultSubject("");
	}

	@Test(expected = ValidationException.class)
	public void testSetDefaultSubjectNull()
	{
		ForgottenPasswordService target = AopTestUtils.getTarget(forgottenPasswordService);
		target.setDefaultSubject(null);
	}
	
	@Test
	public void testGetForgottenPasswordData()
	{
		Assert.assertNotNull(doGetForgottenPasswordData(false));
	}

	@Test(expected = OperationDisallowedException.class)
	public void testGetForgottenPasswordDataProduction()
	{
		doGetForgottenPasswordData(true);
	}
	
	@Test
	public void testIsCurrentYes()
	{
		ForgottenPasswordData verificationData = doGetForgottenPasswordData(false);
		Assert.assertTrue(forgottenPasswordService.isCurrent(verificationData));
	}
	
	@Test
	public void testIsCurrentNo() throws InterruptedException
	{
		ForgottenPasswordService target = AopTestUtils.getTarget(forgottenPasswordService);
		Period period = (Period) ReflectionTestUtils.getField(target, "expirationPeriod");
		try {
			target.setExpirationPeriod("PT0S");
			ForgottenPasswordData verificationData = doGetForgottenPasswordData(false);
			Thread.sleep(5L);
			Assert.assertFalse(forgottenPasswordService.isCurrent(verificationData));
		}
		finally {
			ReflectionTestUtils.setField(target, "expirationPeriod", period);
		}
	}
	
	@Test
	public void testIsValidYes()
	{
		ForgottenPasswordData verificationData = doGetForgottenPasswordData(false);
		Assert.assertTrue(forgottenPasswordService.isValid(verificationData));
	}
	
	@Test
	public void testIsValidNoNotCurrent() throws InterruptedException
	{
		ForgottenPasswordService target = AopTestUtils.getTarget(forgottenPasswordService);
		Period period = (Period) ReflectionTestUtils.getField(target, "expirationPeriod");
		try {
			target.setExpirationPeriod("PT0S");
			ForgottenPasswordData verificationData = doGetForgottenPasswordData(false);
			Thread.sleep(5L);
			Assert.assertFalse(forgottenPasswordService.isValid(verificationData));
		}
		finally {
			ReflectionTestUtils.setField(target, "expirationPeriod", period);
		}
	}
	
	@Test
	public void testIsValidNoEmptyEmail() throws InterruptedException
	{
		ForgottenPasswordData verificationData = doGetForgottenPasswordData(false);
		ForgottenPasswordData badData = new ForgottenPasswordData(null, verificationData.getExpiration(),
				verificationData.getSignature());
		Assert.assertFalse(forgottenPasswordService.isValid(badData));
	}
	
	@Test
	public void testIsValidNoBadSignature() throws InterruptedException
	{
		ForgottenPasswordData verificationData = doGetForgottenPasswordData(false);
		ForgottenPasswordData badData = new ForgottenPasswordData(verificationData.getEmailAddress(),
				verificationData.getExpiration(), "");
		Assert.assertFalse(forgottenPasswordService.isValid(badData));
	}
	
	@Test
	public void testResetPasswordInvalidVerificationData()
	{
		ForgottenPasswordData verificationData = doGetForgottenPasswordData(false);
		User user = userEmailAddressService.findByEmailAddress(verificationData.getEmailAddress());
		String oldPass = user.getHashedPassword();
		String newPass = oldPass + "-TEST";
		
		ServiceBaseTestUtil.clearWorkRecords(forgottenPasswordService);

		ForgottenPasswordData badData = new ForgottenPasswordData(verificationData.getEmailAddress(),
				verificationData.getExpiration(), "");
		Assert.assertFalse(forgottenPasswordService.resetPassword(badData, newPass));
		
		User fetched = userService.findById(user.getDbId());
		Assert.assertNotSame(user, fetched);
		Assert.assertEquals(user.getHashedPassword(), fetched.getHashedPassword());
		
		ServiceBaseTestUtil.verifyWorkRecord(forgottenPasswordService, "resetPassword", 1, 0);
	}
	
	@Test
	public void testResetPassword()
	{
		ForgottenPasswordData verificationData = doGetForgottenPasswordData(false);
		User user = userEmailAddressService.findByEmailAddress(verificationData.getEmailAddress());
		String oldPass = user.getHashedPassword();
		String newPass = oldPass + "-TEST";
		
		ServiceBaseTestUtil.clearWorkRecords(forgottenPasswordService);

		Assert.assertTrue(forgottenPasswordService.resetPassword(verificationData, newPass));
		
		User fetched = userService.findById(user.getDbId());
		Assert.assertNotSame(user, fetched);
		Assert.assertEquals(newPass, fetched.getHashedPassword());
		
		ServiceBaseTestUtil.verifyWorkRecord(forgottenPasswordService, "resetPassword", 1, 0);
	}

	private ForgottenPasswordData doGetForgottenPasswordData(boolean isProduction)
	{
		String environment = EnvironmentUtils.getEnvironment();
		
		try {
			EnvironmentTestUtils.setProduction(isProduction);
			User user = makePersistedUser(applicationContext);
			VerifiableEmailAddress emailAddress = makePersistedEmailAddress(applicationContext, user);
	
			return forgottenPasswordService.getForgottenPasswordData(emailAddress);
		}
		finally {
			EnvironmentTestUtils.setEnvironment(environment);
		}
		
	}
}
