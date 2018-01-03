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
import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.brienwheeler.lib.email.EmailAddress;
import com.brienwheeler.svc.users.IUserEmailAddressService;
import com.brienwheeler.svc.users.IUserRoleService;
import com.brienwheeler.svc.users.IUserService;
import com.brienwheeler.svc.users.domain.User;

public class UserDetailsService implements
		org.springframework.security.core.userdetails.UserDetailsService
{
	private boolean allowEmailLookup = true;
	private boolean allowUsernameLookup = true;
	private IUserService userService;
	private IUserEmailAddressService userEmailAddressService;
	private IUserRoleService userRoleService;
	
	@Override
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException
	{
		User user = null;
		
		if (allowEmailLookup && EmailAddress.isValid(username))
			user = userEmailAddressService.findByEmailAddress(new EmailAddress(username));
		
		if ((user == null) && allowUsernameLookup)
			user = userService.findByUsername(username);
		
		if (user == null)
			throw new UsernameNotFoundException("username not found: " + username);
		
		List<String> roles = userRoleService.getUserRoles(user);
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (String role : roles)
			authorities.add(new SimpleGrantedAuthority(role));
		
		return new com.brienwheeler.web.spring.security.UserDetails(user, authorities);
	}

	@Required
	public void setUserService(IUserService userService)
	{
		this.userService = userService;
	}
	
	@Required
	public void setUserEmailAddressService(IUserEmailAddressService userEmailAddressService)
	{
		this.userEmailAddressService = userEmailAddressService;
	}
		
	@Required
	public void setUserRoleService(IUserRoleService userRoleService)
	{
		this.userRoleService = userRoleService;
	}

	public void setAllowEmailLookup(boolean allowEmailLookup)
	{
		this.allowEmailLookup = allowEmailLookup;
	}

	public void setAllowUsernameLookup(boolean allowUsernameLookup)
	{
		this.allowUsernameLookup = allowUsernameLookup;
	}

}
