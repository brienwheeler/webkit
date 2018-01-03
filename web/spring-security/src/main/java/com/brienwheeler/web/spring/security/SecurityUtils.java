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

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.brienwheeler.lib.db.domain.DbId;
import com.brienwheeler.lib.util.ValidationUtils;
import com.brienwheeler.svc.users.IUserService;
import com.brienwheeler.svc.users.domain.User;

public class SecurityUtils
{
	private static final String LOGGED_IN_USER = "com.brienwheeler.web.spring.security.SecurityUtils.loggedInUser";
	
	private SecurityUtils() {}
	
	public static long getLoggedInUserId()
	{
		org.springframework.security.core.userdetails.UserDetails userDetails = getLoggedInUserDetails();
		if (userDetails instanceof UserDetails)
			return ((UserDetails) userDetails).getUserId();
		return 0;
	}

	public static long ensureLoggedInUserId()
	{
		long userId = getLoggedInUserId();
		if (userId == 0)
			throw new NoLoggedInUserException();
		return userId;
	}
	
	public static User getLoggedInUser(HttpSession session, IUserService userService)
	{
		ValidationUtils.assertNotNull(session, "session cannot be null");
		ValidationUtils.assertNotNull(userService, "userService cannot be null");
		
		long userId = ensureLoggedInUserId();
		
		Object attribute = session.getAttribute(LOGGED_IN_USER);
		if ((attribute instanceof User) && (((User) attribute).getId() == userId))
			return (User) attribute;
		
		User user = userService.findById(new DbId<User>(User.class, userId));
		if (user == null)
			throw new IllegalStateException("logged in user id not found in database");
		
		session.setAttribute(LOGGED_IN_USER, user);
		return user;
	}
	
	public static void setLoggedInUser(User user, Collection<? extends GrantedAuthority> authorities)
	{
		ValidationUtils.assertNotNull(user, "user cannot be null");
		ValidationUtils.assertTrue(user.getId() != 0, "user cannot be unpersisted");
		ValidationUtils.assertNotNull(authorities, "authorities cannot be null");
		
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext == null)
			throw new IllegalStateException("can't set logged in user if securityContext is null");
		
		if ((securityContext.getAuthentication() != null) && 
				(!(securityContext.getAuthentication() instanceof AnonymousAuthenticationToken)) &&
				(securityContext.getAuthentication().getPrincipal() != null))
		{
			Object principal = securityContext.getAuthentication().getPrincipal();
			if (!(principal instanceof UserDetails) || 
					(((UserDetails) principal).getUserId() != user.getId()))
			{
				throw new IllegalStateException("cannot overwrite currently logged in user");
			}
			// fall through to set new Authentication object in case authorities have changed
		}

		securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
				new UserDetails(user, authorities), user.getHashedPassword(), authorities));
	}
	
	public static boolean loggedInUserHasRole(String role)
	{
		role = ValidationUtils.assertNotEmpty(role, "role cannot be empty");
		return getLoggedInUserGrantedAuthorities().contains(new SimpleGrantedAuthority(role));
	}
	
	private static org.springframework.security.core.userdetails.UserDetails getLoggedInUserDetails()
	{
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if ((securityContext != null) && (securityContext.getAuthentication() != null) &&
				(securityContext.getAuthentication().getPrincipal() != null) &&
				(securityContext.getAuthentication().getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails))
			return (org.springframework.security.core.userdetails.UserDetails) securityContext.getAuthentication().getPrincipal();
		return null;
	}
	
	private static Collection<? extends GrantedAuthority> getLoggedInUserGrantedAuthorities()
	{
		org.springframework.security.core.userdetails.UserDetails userDetails = getLoggedInUserDetails();
		if (userDetails == null)
			return new ArrayList<GrantedAuthority>();
		return userDetails.getAuthorities();
	}
}
