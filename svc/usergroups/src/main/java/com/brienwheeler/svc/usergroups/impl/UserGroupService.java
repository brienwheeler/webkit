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
package com.brienwheeler.svc.usergroups.impl;

import java.util.ArrayList;
import java.util.List;

import com.brienwheeler.lib.monitor.work.MonitoredWork;
import com.brienwheeler.lib.svc.GracefulShutdown;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.brienwheeler.lib.db.DbValidationUtils;
import com.brienwheeler.lib.svc.impl.SpringStoppableServiceBase;
import com.brienwheeler.lib.util.ValidationUtils;
import com.brienwheeler.svc.usergroups.DuplicateUserGroupException;
import com.brienwheeler.svc.usergroups.IUserGroupService;
import com.brienwheeler.svc.usergroups.domain.UserGroup;
import com.brienwheeler.svc.usergroups.domain.UserGroupMember;
import com.brienwheeler.svc.users.domain.User;

public class UserGroupService extends SpringStoppableServiceBase 
		implements IUserGroupService
{
	private IUserGroupDao userGroupDao;
	private IUserGroupMemberDao userGroupMemberDao;
	
	@Required
	public void setUserGroupDao(IUserGroupDao userGroupDao) 
	{
		this.userGroupDao = userGroupDao;
	}

	@Required
	public void setUserGroupMemberDao(IUserGroupMemberDao userGroupMemberDao) 
	{
		this.userGroupMemberDao = userGroupMemberDao;
	}

	@Override
	@Transactional
	@MonitoredWork
    @GracefulShutdown
	public UserGroup createUserGroup(String groupName, String groupType)
	{
		groupName = ValidationUtils.assertNotEmpty(groupName, "groupName cannot be empty");
		groupType = ValidationUtils.assertNotEmpty(groupType, "groupType cannot be empty");

		if (findByNameAndType(groupName, groupType) != null)
			throw new DuplicateUserGroupException("group already exists: " + groupName + "/" + groupType);
		
		UserGroup userGroup = new UserGroup(groupName, groupType);
		return userGroupDao.save(userGroup);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@MonitoredWork
    @GracefulShutdown
	public UserGroup findByNameAndType(String groupName, String groupType)
	{
		groupName = ValidationUtils.assertNotEmpty(groupName, "groupName cannot be empty");
		groupType = ValidationUtils.assertNotEmpty(groupType, "groupType cannot be empty");

		return userGroupDao.findByNameAndType(groupName, groupType);
	}

	@Override
	@Transactional
	@MonitoredWork
    @GracefulShutdown
	public void addUserToGroup(UserGroup userGroup, User user)
	{
		DbValidationUtils.assertPersisted(userGroup);
		DbValidationUtils.assertPersisted(user);
		
		UserGroupMember existing = userGroupMemberDao.findForGroupAndUser(userGroup, user);
		if (existing != null)
			return;
		
		UserGroupMember groupMember = new UserGroupMember(userGroup, user);
		userGroupMemberDao.save(groupMember);
	}
	
	@Override
	@Transactional
	@MonitoredWork
    @GracefulShutdown
	public void removeUserFromGroup(UserGroup userGroup, User user)
	{
		DbValidationUtils.assertPersisted(userGroup);
		DbValidationUtils.assertPersisted(user);
		
		UserGroupMember existing = userGroupMemberDao.findForGroupAndUser(userGroup, user);
		if (existing != null)
			userGroupMemberDao.delete(existing);
	}
	
	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@MonitoredWork
    @GracefulShutdown
	public List<UserGroup> getGroupsForUser(User user)
	{
		DbValidationUtils.assertPersisted(user);
		
		List<UserGroupMember> groupMemberList = userGroupMemberDao.findForUser(user);
		ArrayList<UserGroup> groupList = new ArrayList<UserGroup>(groupMemberList.size());
		for (UserGroupMember groupMember : groupMemberList)
			groupList.add(groupMember.getUserGroup());
		return groupList;
	}
	
	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@MonitoredWork
    @GracefulShutdown
	public List<UserGroup> getGroupsForUserAndType(User user, String groupType)
	{
		DbValidationUtils.assertPersisted(user);
		ValidationUtils.assertNotEmpty(groupType, "groupType cannot be empty");
		
		List<UserGroupMember> groupMemberList = userGroupMemberDao.findForUserAndType(user, groupType);
		ArrayList<UserGroup> groupList = new ArrayList<UserGroup>(groupMemberList.size());
		for (UserGroupMember groupMember : groupMemberList)
			groupList.add(groupMember.getUserGroup());
		return groupList;
	}
	
	@Override
	public UserGroup getSingleGroupOfTypeForUser(User user, String groupType)
	{
		// validation done in getGroupsForUserAndType
		List<UserGroup> userGroups = getGroupsForUserAndType(user, groupType);
		if (userGroups.size() != 1)
			throw new IllegalStateException("user " + user.getId() + " does not have exactly one UserGroup of type " +
					groupType + " (" + userGroups.size() + ")");
		return userGroups.get(0);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@MonitoredWork
    @GracefulShutdown
	public List<User> getUsersForGroup(UserGroup userGroup)
	{
		DbValidationUtils.assertPersisted(userGroup);
		
		List<UserGroupMember> groupMemberList = userGroupMemberDao.findForGroup(userGroup);
		ArrayList<User> userList = new ArrayList<User>(groupMemberList.size());
		for (UserGroupMember groupMember : groupMemberList)
			userList.add(groupMember.getUser());
		return userList;
	}
}
