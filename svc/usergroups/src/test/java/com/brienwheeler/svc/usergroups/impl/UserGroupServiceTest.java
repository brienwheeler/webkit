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
package com.brienwheeler.svc.usergroups.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.svc.impl.ServiceBaseTestUtil;
import com.brienwheeler.lib.util.ValidationException;
import com.brienwheeler.svc.usergroups.DuplicateUserGroupException;
import com.brienwheeler.svc.usergroups.domain.UserGroup;
import com.brienwheeler.svc.users.domain.User;
import com.brienwheeler.svc.users.impl.AbstractSvcUsersTest;

public class UserGroupServiceTest extends AbstractSvcUserGroupsTest
{
    @Test(expected = ValidationException.class)
    public void testCreateEmptyName()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userGroupService);
    	
    	userGroupService.createUserGroup(" ", "groupType");
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userGroupService, "createUserGroup", 0, 1);
    }
    
    @Test(expected = ValidationException.class)
    public void testCreateEmptyType()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userGroupService);
    	
    	userGroupService.createUserGroup("groupName", " ");
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userGroupService, "createUserGroup", 0, 1);
    }
    
    @Test
    public void testCreate()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userGroupService);
    	
    	UserGroup userGroup = makePersistedUserGroup(applicationContext);
    	Assert.assertNotNull(userGroup);
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userGroupService, "createUserGroup", 1, 0);
    }
	
    @Test
    public void testCreateDuplicate()
    {
    	UserGroup userGroup = makePersistedUserGroup(applicationContext);
    	ServiceBaseTestUtil.clearWorkRecords(userGroupService);
    	
    	try {
	    	userGroupService.createUserGroup(userGroup.getGroupName(), userGroup.getGroupType());
	    	Assert.fail();
    	}
    	catch (DuplicateUserGroupException e) {
    		// expected
    	}
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userGroupService, "createUserGroup", 0, 1);
    }
    
    @Test(expected = ValidationException.class)
    public void findByNameAndTypeEmptyName()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userGroupService);
    	
    	userGroupService.findByNameAndType(" ", "groupType");
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userGroupService, "findByNameAndType", 0, 1);
    }
    
    @Test(expected = ValidationException.class)
    public void findByNameAndTypeEmptyType()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userGroupService);
    	
    	userGroupService.findByNameAndType("groupName", " ");
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userGroupService, "findByNameAndType", 0, 1);
    }
    
    @Test
    public void findByNameAndTypeNoMatch()
    {
    	ServiceBaseTestUtil.clearWorkRecords(userGroupService);

    	UserGroup userGroup = makeUnpersistedUserGroup();
    	UserGroup fetched = userGroupService.findByNameAndType(userGroup.getGroupName(),
    			userGroup.getGroupType());
    	Assert.assertNull(fetched);
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userGroupService, "findByNameAndType", 1, 0);
    }
    
    @Test
    public void findByNameAndType()
    {
    	UserGroup userGroup = makePersistedUserGroup(applicationContext);    	
    	ServiceBaseTestUtil.clearWorkRecords(userGroupService);

    	UserGroup fetched = userGroupService.findByNameAndType(userGroup.getGroupName(),
    			userGroup.getGroupType());
    	Assert.assertNotSame(userGroup, fetched);
    	Assert.assertEquals(userGroup, fetched);

    	ServiceBaseTestUtil.verifyWorkRecord(userGroupService, "findByNameAndType", 1, 0);
    }
    
    @Test
    public void testAddUserToGroup()
    {
    	User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
    	UserGroup userGroup = makePersistedUserGroup(applicationContext);
    	
    	ServiceBaseTestUtil.clearWorkRecords(userGroupService);

    	userGroupService.addUserToGroup(userGroup, user);
    	List<UserGroup> fetched = userGroupService.getGroupsForUser(user);
    	Assert.assertEquals(1, fetched.size());
    	Assert.assertNotSame(userGroup, fetched.get(0));
    	Assert.assertEquals(userGroup, fetched.get(0));

    	ServiceBaseTestUtil.verifyWorkRecord(userGroupService, "addUserToGroup", 1, 0);
    }

    @Test
    public void testAddUserToGroupExisting()
    {
    	User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
    	UserGroup userGroup = makePersistedUserGroup(applicationContext);
    	
    	ServiceBaseTestUtil.clearWorkRecords(userGroupService);

    	userGroupService.addUserToGroup(userGroup, user);
    	userGroupService.addUserToGroup(userGroup, user);
    	List<UserGroup> fetched = userGroupService.getGroupsForUser(user);
    	Assert.assertEquals(1, fetched.size());
    	Assert.assertNotSame(userGroup, fetched.get(0));
    	Assert.assertEquals(userGroup, fetched.get(0));

    	ServiceBaseTestUtil.verifyWorkRecord(userGroupService, "addUserToGroup", 2, 0);
    }

    @Test
    public void testRemoveUserFromGroup()
    {
    	User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
    	UserGroup userGroup = makePersistedUserGroup(applicationContext);
    	
    	userGroupService.addUserToGroup(userGroup, user);
    	List<UserGroup> fetched = userGroupService.getGroupsForUser(user);
    	Assert.assertEquals(1, fetched.size());

    	ServiceBaseTestUtil.clearWorkRecords(userGroupService);

    	userGroupService.removeUserFromGroup(userGroup, user);
    	fetched = userGroupService.getGroupsForUser(user);
    	Assert.assertEquals(0, fetched.size());
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userGroupService, "removeUserFromGroup", 1, 0);
    }

    @Test
    public void testRemoveUserFromGroupNonExisting()
    {
    	User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
    	UserGroup userGroup = makePersistedUserGroup(applicationContext);
    	
    	List<UserGroup> fetched = userGroupService.getGroupsForUser(user);
    	Assert.assertEquals(0, fetched.size());

    	ServiceBaseTestUtil.clearWorkRecords(userGroupService);

    	userGroupService.removeUserFromGroup(userGroup, user);
    	fetched = userGroupService.getGroupsForUser(user);
    	Assert.assertEquals(0, fetched.size());
    	
    	ServiceBaseTestUtil.verifyWorkRecord(userGroupService, "removeUserFromGroup", 1, 0);
    }
    
    @Test
    public void testGetGroupsForUser()
    {
    	User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
    	UserGroup userGroup1 = makePersistedUserGroup(applicationContext);
    	UserGroup userGroup2 = makePersistedUserGroup(applicationContext);

    	userGroupService.addUserToGroup(userGroup1, user);
    	userGroupService.addUserToGroup(userGroup2, user);
    	
    	List<UserGroup> groupList = userGroupService.getGroupsForUser(user);
    	Assert.assertEquals(2, groupList.size());
    	Assert.assertTrue(groupList.contains(userGroup1));
    	Assert.assertTrue(groupList.contains(userGroup2));
    }

    @Test
    public void testGetGroupsForUserAndType()
    {
    	User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
    	UserGroup userGroup1 = makePersistedUserGroup(applicationContext);
    	UserGroup userGroup2 = makePersistedUserGroup(applicationContext);

    	userGroupService.addUserToGroup(userGroup1, user);
    	userGroupService.addUserToGroup(userGroup2, user);
    	
    	List<UserGroup> groupList = userGroupService.getGroupsForUserAndType(user, userGroup1.getGroupType());
    	Assert.assertEquals(1, groupList.size());
    	Assert.assertTrue(groupList.contains(userGroup1));
    }

    @Test
    public void testGetUsersForGroup()
    {
    	User user1 = AbstractSvcUsersTest.makePersistedUser(applicationContext);
    	User user2 = AbstractSvcUsersTest.makePersistedUser(applicationContext);
    	UserGroup userGroup = makePersistedUserGroup(applicationContext);

    	userGroupService.addUserToGroup(userGroup, user1);
    	userGroupService.addUserToGroup(userGroup, user2);
    	
    	List<User> userList = userGroupService.getUsersForGroup(userGroup);
    	Assert.assertEquals(2, userList.size());
    	Assert.assertTrue(userList.contains(user1));
    	Assert.assertTrue(userList.contains(user2));
    }
}
