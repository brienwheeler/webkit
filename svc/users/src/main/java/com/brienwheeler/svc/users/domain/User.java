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
package com.brienwheeler.svc.users.domain;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.brienwheeler.lib.db.domain.VersionedGeneratedIdEntityBase;
import com.brienwheeler.lib.util.ValidationUtils;

@Entity
public class User extends VersionedGeneratedIdEntityBase<User>
{
	@Column(nullable=false, unique=true)
	private String username;
	
	@Column(nullable=false)
	private String hashedPassword;
	
	@Column(nullable=false)
	private boolean enabled;

	@Column(nullable=false)
	private boolean accountExpired;
	
	@Column(nullable=false)
	private boolean credentialsExpired;
	
	@Column(nullable=false)
	private boolean accountLocked;
	
	protected User() {} // for Hibernate

	public User(String username, String hashedPassword)
	{
		username = ValidationUtils.assertNotEmpty(username, "username cannot be empty");
		hashedPassword = ValidationUtils.assertNotEmpty(hashedPassword, "hashedPassword cannot be empty");
		
		this.username = username;
		this.hashedPassword = hashedPassword;
		this.enabled = true;
		this.accountExpired = false;
		this.credentialsExpired = false;
		this.accountLocked = false;
	}

	public String getUsername()
	{
		return username;
	}

	public String getHashedPassword()
	{
		return hashedPassword;
	}

	public void setHashedPassword(String  hashedPassword)
	{
		this.hashedPassword = hashedPassword;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isAccountExpired()
	{
		return accountExpired;
	}

	public void setAccountExpired(boolean accountExpired)
	{
		this.accountExpired = accountExpired;
	}

	public boolean isCredentialsExpired()
	{
		return credentialsExpired;
	}

	public void setCredentialsExpired(boolean credentialsExpired)
	{
		this.credentialsExpired = credentialsExpired;
	}

	public boolean isAccountLocked()
	{
		return accountLocked;
	}

	public void setAccountLocked(boolean accountLocked)
	{
		this.accountLocked = accountLocked;
	}
}
