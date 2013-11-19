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
package com.brienwheeler.svc.usergroups.validation;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.svc.usergroups.domain.UserGroup;
import com.brienwheeler.svc.usergroups.impl.AbstractSvcUserGroupsTest;

public class UserGroupNotExistValidatorTest extends AbstractSvcUserGroupsTest
{
	private static final String TEST_GROUP_TYPE = "TestGroupType";
	
	@UserGroupNotExist(groupType=TEST_GROUP_TYPE)
	private String userGroupName;
	
	@Test
	public void testIsValid() throws NoSuchFieldException
	{
		UserGroupNotExistValidator validator = new UserGroupNotExistValidator();
		validator.setApplicationContext(applicationContext);
		
		Field usernameField = getClass().getDeclaredField("userGroupName");
		UserGroupNotExist annotation = usernameField.getAnnotation(UserGroupNotExist.class);
		validator.initialize(annotation);
		
		UserGroup userGroup = makeUnpersistedUserGroup(TEST_GROUP_TYPE);
		Assert.assertTrue(validator.isValid(userGroup.getGroupName(), null));
		userGroup = makePersistedUserGroup(applicationContext, TEST_GROUP_TYPE);
		Assert.assertFalse(validator.isValid(userGroup.getGroupName(), null));
	}
}
