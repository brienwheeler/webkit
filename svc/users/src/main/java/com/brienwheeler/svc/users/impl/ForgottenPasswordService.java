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

import java.util.HashMap;
import java.util.Map;

import com.brienwheeler.lib.monitor.work.MonitoredWork;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.brienwheeler.lib.email.EmailAddress;
import com.brienwheeler.lib.security.HmacSha256;
import com.brienwheeler.lib.svc.GracefulShutdown;
import com.brienwheeler.lib.svc.impl.SpringStoppableServiceBase;
import com.brienwheeler.lib.util.EnvironmentUtils;
import com.brienwheeler.lib.util.OperationDisallowedException;
import com.brienwheeler.lib.util.ValidationUtils;
import com.brienwheeler.svc.email.IEmailService;
import com.brienwheeler.svc.users.ForgottenPasswordData;
import com.brienwheeler.svc.users.IForgottenPasswordService;
import com.brienwheeler.svc.users.IUserEmailAddressService;
import com.brienwheeler.svc.users.IUserService;
import com.brienwheeler.svc.users.domain.User;

public class ForgottenPasswordService extends SpringStoppableServiceBase
		implements IForgottenPasswordService
{
	private IEmailService emailService;
	private IUserEmailAddressService userEmailAddressService;
	private IUserService userService;
	private Period expirationPeriod;
	private String defaultSubject = "";
	private String secretKey;
	private String templateName;
		
	@Required
	public void setEmailService(IEmailService emailService)
	{
		this.emailService = emailService;
	}

	@Required
	public void setUserEmailAddressService(IUserEmailAddressService userEmailAddressService)
	{
		this.userEmailAddressService = userEmailAddressService;
	}

	@Required
	public void setUserService(IUserService userService)
	{
		this.userService = userService;
	}
	
	@Required
	public void setExpirationPeriod(String expirationPeriod)
	{
		this.expirationPeriod = Period.parse(expirationPeriod);
	}

	public void setDefaultSubject(String defaultSubject)
	{
		ValidationUtils.assertNotNull(defaultSubject, "defaultSubject cannot be null");
		this.defaultSubject = defaultSubject;
	}

	@Required
	public void setSecretKey(String secretKey)
	{
		this.secretKey = secretKey;
	}

	@Required
	public void setTemplateName(String templateName)
	{
		this.templateName = templateName;
	}

	@Override
	@MonitoredWork
    @GracefulShutdown
	public boolean sendForgottenPasswordEmail(EmailAddress emailAddress)
	{
		ValidationUtils.assertNotNull(emailAddress, "emailAddress cannot be null");
		
		ForgottenPasswordData verificationData = doGetForgottenPasswordData(emailAddress);
		if (verificationData == null)
			return false;
		
		Map<String,Object> templateModel = new HashMap<String,Object>();
		templateModel.put("verificationData", verificationData);
		
		emailService.sendEmail(emailAddress, defaultSubject, templateName, templateModel);
		return true;
	}

	@Override
	@GracefulShutdown // don't care about monitoring
	public ForgottenPasswordData getForgottenPasswordData(EmailAddress emailAddress)
	{
		ValidationUtils.assertNotNull(emailAddress, "emailAddress cannot be null");
		
		if (EnvironmentUtils.isProduction())
			throw new OperationDisallowedException("getForgottenPasswordData() not allowed in production environments");
		
		return doGetForgottenPasswordData(emailAddress);
	}
	
	public boolean isCurrent(ForgottenPasswordData verificationData)
	{
		ValidationUtils.assertNotNull(verificationData, "verificationData cannot be null");
		
		return verificationData.getExpiration() > new DateTime().getMillis();
	}

	public boolean isValid(ForgottenPasswordData verificationData)
	{
		ValidationUtils.assertNotNull(verificationData, "verificationData cannot be null");
		
		if (!isCurrent(verificationData))
			return false;
		if ((verificationData.getEmailAddress() == null) || !StringUtils.hasText(verificationData.getEmailAddress().getAddress()))
			return false;
		return getSignature(verificationData.getEmailAddress(), verificationData.getExpiration()).equals(verificationData.getSignature());
	}
	
	@Override
	@MonitoredWork
    @GracefulShutdown
	@Transactional
	public boolean resetPassword(ForgottenPasswordData verificationData, String newHashedPassword)
	{
		ValidationUtils.assertNotNull(verificationData, "verificationData cannot be null");
		newHashedPassword = ValidationUtils.assertNotEmpty(newHashedPassword, "newHashedPassword cannot be empty");
		
		if (!isValid(verificationData))
			return false;
		
		User user = userEmailAddressService.findByEmailAddress(verificationData.getEmailAddress());
		if (user == null)
			return false; // hard to imagine how this would happen
		
		userService.setNewPassword(user, newHashedPassword);
		return true;
	}

	private ForgottenPasswordData doGetForgottenPasswordData(EmailAddress emailAddress)
	{
		User user = userEmailAddressService.findByEmailAddress(emailAddress);
		if (user == null)
			return null;
		long expiration = new DateTime().plus(expirationPeriod).getMillis();
		return new ForgottenPasswordData(emailAddress, expiration, getSignature(emailAddress, expiration));
	}
	
	private String getSignature(EmailAddress emailAddress, long expiration)
	{
		String signString = emailAddress.getAddress() + "\n" + expiration + "\n";
		return HmacSha256.base64HmacSha256(secretKey, signString);
	}
}
