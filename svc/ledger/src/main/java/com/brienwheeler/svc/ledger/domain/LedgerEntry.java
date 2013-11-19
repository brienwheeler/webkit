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
package com.brienwheeler.svc.ledger.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.brienwheeler.lib.db.domain.GeneratedIdEntityBase;
import com.brienwheeler.svc.users.domain.User;

@Entity
public class LedgerEntry extends GeneratedIdEntityBase<LedgerEntry>
{
	@ManyToOne(optional=false)
	private User user;
	
	@Column(nullable=false)
	@Type(type="com.brienwheeler.lib.db.joda.time.PersistentDateTimeAsBigInt")
	private DateTime timestamp;
	
	@Column(nullable=true)
	private String detail;
	
	protected LedgerEntry() {} // for Hibernate
	
	public LedgerEntry(User user, String detail)
	{
		this(user, new DateTime(), detail);
	}
	
	public LedgerEntry(User user, DateTime timestamp, String detail)
	{
		this.user = user;
		this.timestamp = timestamp;
		this.detail = detail;
	}

	public User getUser()
	{
		return user;
	}

	public DateTime getTimestamp()
	{
		return timestamp;
	}

	public String getDetail()
	{
		return detail;
	}	
}
