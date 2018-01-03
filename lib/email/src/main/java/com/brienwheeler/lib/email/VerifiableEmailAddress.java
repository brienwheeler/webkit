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
package com.brienwheeler.lib.email;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class VerifiableEmailAddress extends EmailAddress
{
	public enum Status {
		UNVERIFIED,
		VERIFIED,
	}

	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private Status status;
	
	protected VerifiableEmailAddress() {}  // for Hibernate
	
	public VerifiableEmailAddress(String emailAddress)
	{
		super(emailAddress);
		status = Status.UNVERIFIED;
	}

	public VerifiableEmailAddress(String emailAddress, Status status)
	{
		super(emailAddress);
		this.status = status;
	}

	public VerifiableEmailAddress(EmailAddress emailAddress)
	{
		super(emailAddress.getAddress());
		if (emailAddress instanceof VerifiableEmailAddress)
			this.status = ((VerifiableEmailAddress) emailAddress).getStatus();
		else
			this.status = Status.UNVERIFIED;
	}
	
	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}
	
	public boolean isVerified()
	{
		return status == Status.VERIFIED;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if ((obj == null) || (obj.getClass() != VerifiableEmailAddress.class))
			return false;
		return getAddress().equals(((VerifiableEmailAddress) obj).getAddress()) &&
				status == ((VerifiableEmailAddress) obj).status;
	}
}
