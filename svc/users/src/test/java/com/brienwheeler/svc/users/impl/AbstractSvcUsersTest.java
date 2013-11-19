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

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.brienwheeler.lib.db.DbValidationUtils;
import com.brienwheeler.lib.email.VerifiableEmailAddress;
import com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer;
import com.brienwheeler.svc.users.IForgottenPasswordService;
import com.brienwheeler.svc.users.IUserEmailAddressService;
import com.brienwheeler.svc.users.IUserRoleService;
import com.brienwheeler.svc.users.IUserService;
import com.brienwheeler.svc.users.domain.User;

@ContextConfiguration({
    "classpath:com/brienwheeler/lib/spring/beans/contextBeanDumper.xml",
	"classpath:com/brienwheeler/svc/users/userEmailAddressService.xml",
	"classpath:com/brienwheeler/svc/users/userRoleService.xml",
    "classpath:com/brienwheeler/svc/users/userService.xml",
	"classpath:com/brienwheeler/svc/users/forgottenPasswordService.xml" })
public abstract class AbstractSvcUsersTest extends AbstractJUnit4SpringContextTests
{
	protected final Log log = LogFactory.getLog(getClass());
	
	protected IForgottenPasswordService forgottenPasswordService;
	protected IUserEmailAddressService userEmailAddressService;
	protected IUserRoleService userRoleService;
	protected IUserService userService;
	
    @BeforeClass
    public static void oneTimeSetUp()
    {
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/svc/users/test.properties");
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/svc/email/test.properties");
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/lib/db/test.properties");
    }

	@Before
	public void setUp()
	{
		forgottenPasswordService = applicationContext.getBean("com.brienwheeler.svc.users.forgottenPasswordService",
				IForgottenPasswordService.class);
        userEmailAddressService = applicationContext.getBean("com.brienwheeler.svc.users.userEmailAddressService",
        		IUserEmailAddressService.class);
        userRoleService = applicationContext.getBean("com.brienwheeler.svc.users.userRoleService",
        		IUserRoleService.class);
        userService = applicationContext.getBean("com.brienwheeler.svc.users.userService",
        		IUserService.class);
	}

	public static User makeUnpersistedUser()
	{
        String uuid = UUID.randomUUID().toString();
		return new User(uuid, uuid); 
	}

	public static User makePersistedUser(ApplicationContext applicationContext)
	{
        IUserService userService = applicationContext.getBean("com.brienwheeler.svc.users.userService",
        		IUserService.class);
        String uuid = UUID.randomUUID().toString();
        return userService.createUser(uuid, uuid);
	}
	
	public static VerifiableEmailAddress makeUnpersistedEmailAddress(User user)
	{
		return new VerifiableEmailAddress(user.getUsername() + "@test.com");
	}
	
	public static VerifiableEmailAddress makePersistedEmailAddress(ApplicationContext applicationContext, User user)
	{
		DbValidationUtils.assertPersisted(user);
		IUserEmailAddressService userEmailAddressService = applicationContext.getBean("com.brienwheeler.svc.users.userEmailAddressService",
				IUserEmailAddressService.class);
		VerifiableEmailAddress emailAddress = new VerifiableEmailAddress(user.getUsername() + "@test.com");
		userEmailAddressService.addEmailAddress(user, emailAddress);
		return emailAddress;
	}
}
