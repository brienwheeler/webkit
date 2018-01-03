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
package com.brienwheeler.web.spring.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.brienwheeler.lib.db.domain.DbId;
import com.brienwheeler.lib.util.ValidationUtils;
import com.brienwheeler.svc.users.IUserService;
import com.brienwheeler.svc.users.domain.User;

public class SetUserInSessionInterceptor extends HandlerInterceptorAdapter
{
	private static final Log log = LogFactory.getLog(SetUserInSessionInterceptor.class);
	private static final String SESSION_ATTR_USER = "com.brienwheeler.web.spring.security.SetUserInSessionInterceptor.user";
	
	private IUserService userService;
	
	@Required
	public void setUserService(IUserService userService)
	{
		this.userService = userService;
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception
	{
		long userId = SecurityUtils.getLoggedInUserId();
		
		if (userId != 0) {
			// we have a logged in user, see if the session has the user on it
			HttpSession session = request.getSession(false);
			if (session != null) {
				User user = (User) session.getAttribute(SESSION_ATTR_USER);
				if (user == null) {
					user = userService.findById(new DbId<User>(User.class, userId));
					if (user == null)
						throw new IllegalStateException("failed to lookup authenticated user");
					else {
						log.info("setting user id " + user.getId() + " into session " + session.getId());
						session.setAttribute(SESSION_ATTR_USER, user);
					}
				}
				else if (user.getId() != userId)
					throw new IllegalStateException("id of stored user does not match current authenticated user");
			}
		}
		
		return super.preHandle(request, response, handler);
	}

	public static User getCachedUser(HttpSession session, boolean throwOnFail)
	{
		ValidationUtils.assertNotNull(session, "session cannot be null");
		User user = (User) session.getAttribute(SESSION_ATTR_USER);
		if ((user == null) && throwOnFail)
			throw new IllegalStateException("no cached user found in session");
		return user;
	}

	public static User getCachedUser(HttpSession session)
	{
		return getCachedUser(session, true);
	}
}