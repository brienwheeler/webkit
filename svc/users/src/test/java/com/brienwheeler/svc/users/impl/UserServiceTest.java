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

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.db.DbTestUtils;
import com.brienwheeler.lib.db.domain.DbId;
import com.brienwheeler.lib.email.VerifiableEmailAddress;
import com.brienwheeler.lib.svc.impl.ServiceBaseTestUtil;
import com.brienwheeler.lib.util.ValidationException;
import com.brienwheeler.svc.users.DuplicateUserException;
import com.brienwheeler.svc.users.IUserService;
import com.brienwheeler.svc.users.domain.User;

public class UserServiceTest extends AbstractSvcUsersTest
{
    @Test
    public void testCreate()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userService);
    	
    	User user = makePersistedUser(applicationContext);
    	Assert.assertNotNull(user);
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userService, "createUser", 1, 0);
    }
	
    @Test(expected = ValidationException.class)
    public void testCreateEmptyUsername()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userService);

    	userService.createUser(" ", "hashedPassword");

    	ServiceBaseTestUtil.verifyWorkRecord(userService, "createUser", 0, 1);
    }
    
    @Test(expected = ValidationException.class)
    public void testCreateEmptyPassword()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userService);

    	userService.createUser("username", " ");

    	ServiceBaseTestUtil.verifyWorkRecord(userService, "createUser", 0, 1);
    }
    
    @Test
    public void testCreateWithCallback()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userService);
    	ServiceBaseTestUtil.clearWorkRecords(userEmailAddressService);
    	
    	User user = makeUnpersistedUser();
    	final VerifiableEmailAddress emailAddress = makeUnpersistedEmailAddress(user);

    	IUserService.CreateUserCallback callback = new IUserService.CreateUserCallback() {
			@Override
			public void userCreated(User user) {
				userEmailAddressService.addEmailAddress(user, emailAddress);
			}
		};
    	
		user = userService.createUser(user.getUsername(), user.getHashedPassword(), callback);
		
		List<VerifiableEmailAddress> emailAddresses = userEmailAddressService.findByUser(user);
		Assert.assertEquals(1, emailAddresses.size());
		
    	ServiceBaseTestUtil.verifyWorkRecord(userService, "createUser", 1, 0);
    	ServiceBaseTestUtil.verifyWorkRecord(userEmailAddressService, "addEmailAddress", 1, 0);
    }
	
    @Test
    public void testCreateWithCallbackNull()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userService);
    	
    	User user = makeUnpersistedUser();
    	user = userService.createUser(user.getUsername(), user.getHashedPassword(),
    			(IUserService.CreateUserCallback[]) null);
    	Assert.assertNotNull(user);
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userService, "createUser", 1, 0);
    }
	
    @Test
    public void testCreateDuplicate()
    {
    	User user = makePersistedUser(applicationContext);
    	ServiceBaseTestUtil.clearWorkRecords(userService);
    	
    	try {
    		userService.createUser(user.getUsername(), user.getHashedPassword());
    		Assert.fail();
    	}
    	catch (DuplicateUserException e) {
    		// expected
    	}
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userService, "createUser", 0, 1);
    }
    
    @Test
    public void testFindById()
    {
    	User user = makePersistedUser(applicationContext);
    	ServiceBaseTestUtil.clearWorkRecords(userService);
    	
    	User fetched = userService.findById(user.getDbId());
    	Assert.assertNotSame(user, fetched);
    	Assert.assertEquals(user, fetched);
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userService, "findById", 1, 0);
    }

    @Test
    public void testFindByIdNoMatch()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userService);
    	
    	User fetched = userService.findById(new DbId<User>(User.class, Long.MAX_VALUE)); // assume not present ;)
    	Assert.assertNull(fetched);
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userService, "findById", 1, 0);
    }
    
    @Test
    public void testSetNewPassword()
    {
    	Object[] objects = DbTestUtils.doInHibernateSession(applicationContext, new Callable<Object[]>() {
    		@Override
    		public Object[] call() {
		    	User user = makePersistedUser(applicationContext);
		    	ServiceBaseTestUtil.clearWorkRecords(userService);
		    	
		    	String oldPass = user.getHashedPassword();
		    	String newPass = oldPass + "-TEST";
		    	
		    	userService.setNewPassword(user, newPass);

		    	ServiceBaseTestUtil.verifyWorkRecord(userService, "setNewPassword", 1, 0);
		    	
		    	return new Object[] { user, oldPass, newPass };
    		}
    	});

    	User fetched = userService.findById(((User) objects[0]).getDbId());
    	Assert.assertNotSame((User) objects[0], fetched);
    	Assert.assertEquals((String) objects[2], fetched.getHashedPassword());
    }
}
