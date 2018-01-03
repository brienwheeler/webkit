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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.brienwheeler.lib.db.domain.VersionedGeneratedIdEntityBase;
import com.brienwheeler.lib.email.EmailAddress;
import com.brienwheeler.lib.email.VerifiableEmailAddress;
import com.brienwheeler.lib.util.ValidationUtils;

@Entity
public class UserEmailAddress extends VersionedGeneratedIdEntityBase<UserEmailAddress>
{
	@ManyToOne(optional=false)
	private User user;
	
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="emailAddress", column=@Column(nullable=false, unique=true))
	})
	private VerifiableEmailAddress emailAddress;
	
	protected UserEmailAddress() {} // for Hibernate

	public UserEmailAddress(User user, EmailAddress emailAddress)
	{
		ValidationUtils.assertNotNull(user, "user cannot be null");
		ValidationUtils.assertNotNull(emailAddress, "emailAddress cannot be null");
		
		this.user = user;
		this.emailAddress = new VerifiableEmailAddress(emailAddress);
	}

	public User getUser()
	{
		return user;
	}

	public VerifiableEmailAddress getEmailAddress()
	{
		return emailAddress;
	}
}
