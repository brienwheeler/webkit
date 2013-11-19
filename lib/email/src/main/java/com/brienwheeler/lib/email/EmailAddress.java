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
package com.brienwheeler.lib.email;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;

import com.brienwheeler.lib.util.ValidationException;

@Embeddable
@MappedSuperclass
public class EmailAddress implements Comparable<EmailAddress>
{
	public static final String VALID_REGEX = "^[a-z0-9_%+-]+(?:\\.[a-z0-9_%+-]+)*@(?:[a-z0-9-]+\\.)+[a-z]{2,4}$";
	public static final Pattern VALID_PATTERN = Pattern.compile(VALID_REGEX, Pattern.CASE_INSENSITIVE);
		
	@Column(nullable=false)
	private String address;

	public static boolean isValid(String address)
	{
		return isValid(address, false); 
	}

	public static boolean isValid(String address, boolean throwOnInvalid)
	{
		if (address != null) {
			Matcher matcher = VALID_PATTERN.matcher(address.trim().toLowerCase());
			if (matcher.matches())
				return true;
		}
	
		if (throwOnInvalid)
			throw new ValidationException("invalid email address format: " + address);
		return false; 
	}

	protected EmailAddress() {}  // for Hibernate
	
	public EmailAddress(String address)
	{
		isValid(address, true);
		this.address = address.trim().toLowerCase();
	}

	public String getAddress()
	{
		return address;
	}
	
	@Override
	public String toString()
	{
		return address;
	}

	@Override
	public int hashCode()
	{
		return address.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if ((obj == null) || (obj.getClass() != EmailAddress.class))
			return false;
		return address.equals(((EmailAddress) obj).address);
	}

	@Override
	public int compareTo(EmailAddress obj)
	{
		return address.compareTo(obj.address);
	}
}
