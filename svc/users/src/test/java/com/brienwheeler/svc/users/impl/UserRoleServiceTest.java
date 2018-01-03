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

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.svc.impl.ServiceBaseTestUtil;
import com.brienwheeler.lib.util.ValidationException;
import com.brienwheeler.svc.users.domain.User;

public class UserRoleServiceTest extends AbstractSvcUsersTest
{
	private static final String TEST_ROLE_1 = "role1";
	private static final String TEST_ROLE_2 = "role2";
	
    @Test
    public void testSetUserRolesNullUser()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userRoleService);
    	
    	try {
    		userRoleService.setUserRoles(null, TEST_ROLE_1);
    		Assert.fail();
    	}
    	catch (ValidationException e) {
    		// expected
    	}
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userRoleService, "setUserRoles", 0, 1);
    }

    @Test
    public void testSetUserRolesNullRoles()
    {
    	User user = makePersistedUser(applicationContext);

    	ServiceBaseTestUtil.clearWorkRecords(userRoleService);

    	try {
    		userRoleService.setUserRoles(user, (String[]) null);
    		Assert.fail();
    	}
    	catch (ValidationException e) {
    		// expected
    	}
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userRoleService, "setUserRoles", 0, 1);
    }

    @Test
    public void testSetUserRoles()
    {
    	User user = makePersistedUser(applicationContext);

    	ServiceBaseTestUtil.clearWorkRecords(userRoleService);

    	userRoleService.setUserRoles(user, TEST_ROLE_1);
    	
    	List<String> roles = userRoleService.getUserRoles(user);
    	Assert.assertEquals(1, roles.size());
    	Assert.assertEquals(TEST_ROLE_1, roles.get(0));

    	ServiceBaseTestUtil.verifyWorkRecord(userRoleService, "setUserRoles", 1, 0);
    	ServiceBaseTestUtil.verifyWorkRecord(userRoleService, "getUserRoles", 1, 0);
    }
	
    @Test
    public void testSetUserRolesNoChange()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userRoleService);

    	User user = makePersistedUser(applicationContext);
    	userRoleService.setUserRoles(user, TEST_ROLE_1);
    	List<String> roles = userRoleService.getUserRoles(user);
    	Assert.assertEquals(1, roles.size());
    	Assert.assertEquals(TEST_ROLE_1, roles.get(0));
    	
    	userRoleService.setUserRoles(user, TEST_ROLE_1);
    	roles = userRoleService.getUserRoles(user);
    	Assert.assertEquals(1, roles.size());
    	Assert.assertEquals(TEST_ROLE_1, roles.get(0));

    	ServiceBaseTestUtil.verifyWorkRecord(userRoleService, "setUserRoles", 2, 0);
    	ServiceBaseTestUtil.verifyWorkRecord(userRoleService, "getUserRoles", 2, 0);
    }

    @Test
    public void testSetUserRolesChange()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userRoleService);

    	User user = makePersistedUser(applicationContext);
    	userRoleService.setUserRoles(user, TEST_ROLE_1);
    	List<String> roles = userRoleService.getUserRoles(user);
    	Assert.assertEquals(1, roles.size());
    	Assert.assertEquals(TEST_ROLE_1, roles.get(0));
    	
    	userRoleService.setUserRoles(user, TEST_ROLE_2);
    	roles = userRoleService.getUserRoles(user);
    	Assert.assertEquals(1, roles.size());
    	Assert.assertEquals(TEST_ROLE_2, roles.get(0));

    	ServiceBaseTestUtil.verifyWorkRecord(userRoleService, "setUserRoles", 2, 0);
    	ServiceBaseTestUtil.verifyWorkRecord(userRoleService, "getUserRoles", 2, 0);
    }
}
