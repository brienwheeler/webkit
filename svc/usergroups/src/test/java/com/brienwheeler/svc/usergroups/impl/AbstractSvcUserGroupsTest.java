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

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer;
import com.brienwheeler.svc.usergroups.IUserGroupService;
import com.brienwheeler.svc.usergroups.domain.UserGroup;

@ContextConfiguration({
    "classpath:com/brienwheeler/lib/spring/beans/contextBeanDumper.xml",
	"classpath:com/brienwheeler/svc/users/userService.xml",
    "classpath:com/brienwheeler/svc/usergroups/userGroupService.xml" })
public abstract class AbstractSvcUserGroupsTest extends AbstractJUnit4SpringContextTests
{
	protected final Log log = LogFactory.getLog(getClass());
	
	protected IUserGroupService userGroupService;
	
    @BeforeClass
    public static void oneTimeSetUp()
    {
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/svc/usergroups/test.properties");
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/lib/db/test.properties");
    }

	@Before
	public void setUp()
	{
		userGroupService = applicationContext.getBean("com.brienwheeler.svc.usergroups.userGroupService",
				IUserGroupService.class);
	}

	public static UserGroup makeUnpersistedUserGroup()
	{
        String uuid = UUID.randomUUID().toString();
		return new UserGroup(uuid, uuid); 
	}

	public static UserGroup makeUnpersistedUserGroup(String groupType)
	{
        String uuid = UUID.randomUUID().toString();
		return new UserGroup(uuid, groupType); 
	}

	public static UserGroup makePersistedUserGroup(ApplicationContext applicationContext)
	{
        IUserGroupService userGroupService = applicationContext.getBean("com.brienwheeler.svc.usergroups.userGroupService",
        		IUserGroupService.class);
        String uuid = UUID.randomUUID().toString();
        return userGroupService.createUserGroup(uuid, uuid);
	}

	public static UserGroup makePersistedUserGroup(ApplicationContext applicationContext, String groupType)
	{
        IUserGroupService userGroupService = applicationContext.getBean("com.brienwheeler.svc.usergroups.userGroupService",
        		IUserGroupService.class);
        String uuid = UUID.randomUUID().toString();
        return userGroupService.createUserGroup(uuid, groupType);
	}
}
