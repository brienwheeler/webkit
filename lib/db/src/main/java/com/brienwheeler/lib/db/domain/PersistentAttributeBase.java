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
package com.brienwheeler.lib.db.domain;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.brienwheeler.lib.db.DbValidationUtils;
import com.brienwheeler.lib.util.ValidationUtils;

@MappedSuperclass
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={ "owner", "name" })
})
public abstract class PersistentAttributeBase<
			AttrClass extends PersistentAttributeBase<AttrClass,OwnerClass>,
			OwnerClass extends GeneratedIdEntityBase<OwnerClass>
		>
		extends VersionedGeneratedIdEntityBase<AttrClass>
{
	@ManyToOne(optional=false)
	private OwnerClass owner;
	
	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false)
	private String value;

	protected PersistentAttributeBase() {} // for Hibernate
	
	public PersistentAttributeBase(OwnerClass owner, String name, String value)
	{
		DbValidationUtils.assertPersisted(owner);
		name = ValidationUtils.assertNotEmpty(name, "name cannot be empty");
		value = ValidationUtils.assertNotEmpty(value, "value cannot be empty");
		
		this.owner = owner;
		this.name = name;
		this.value = value;
	}

	public OwnerClass getOwner()
	{
		return owner;
	}

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
}
