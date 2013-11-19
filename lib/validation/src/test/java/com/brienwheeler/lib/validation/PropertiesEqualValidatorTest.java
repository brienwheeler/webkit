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
package com.brienwheeler.lib.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import junit.framework.Assert;

import org.junit.Test;

public class PropertiesEqualValidatorTest
{
	private static final String ONE = "one";
	private static final String TWO = "two";
	
	@Test(expected=ValidationException.class)
	public void testProperty1Empty()
	{
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		Object object = new Prop1Empty();
		validator.validate(object);
	}

	@Test(expected=ValidationException.class)
	public void testProperty2Empty()
	{
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		Object object = new Prop2Empty();
		validator.validate(object);
	}

	@Test
	public void testGettersEqual()
	{
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		Object object = new Getters(ONE, ONE);
		Set<ConstraintViolation<Object>> violations = validator.validate(object);
		Assert.assertEquals(0, violations.size());
	}

	@Test
	public void testGettersUnequal()
	{
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		Object object = new Getters(ONE, TWO);
		Set<ConstraintViolation<Object>> violations = validator.validate(object);
		Assert.assertEquals(1, violations.size());
	}

	@Test(expected = ValidationException.class)
	public void testProtectedFieldAccessFail()
	{
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		Object object = new ProtectedFields(ONE, ONE);
		validator.validate(object);
	}

	@Test
	public void testPublicFieldsEqual()
	{
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		Object object = new PublicFields(ONE, ONE);
		Set<ConstraintViolation<Object>> violations = validator.validate(object);
		Assert.assertEquals(0, violations.size());
	}

	@Test
	public void testPublicFieldsUnequal()
	{
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		Object object = new PublicFields(ONE, TWO);
		Set<ConstraintViolation<Object>> violations = validator.validate(object);
		Assert.assertEquals(1, violations.size());
	}

	private static class TestObjectBase
	{
		protected final String property1;
		protected final String property2;
		
		protected TestObjectBase()
		{
			this(null, null);
		}
		
		protected TestObjectBase(String property1, String property2)
		{
			this.property1 = property1;
			this.property2 = property2;
		}
	}
	
	@PropertiesEqual(property1=" ", property1Label="property1Label", property2="property2", property2Label="property2Label")
	private static class Prop1Empty extends TestObjectBase
	{
	}
	
	@PropertiesEqual(property1="property1", property1Label="property1Label", property2=" ", property2Label="property2Label")
	private static class Prop2Empty extends TestObjectBase
	{
	}
	
	@PropertiesEqual(property1="property1", property1Label="property1Label", property2="property2", property2Label="property2Label")
	private static class Getters extends TestObjectBase
	{
		public Getters(String property1, String property2)
		{
			super(property1, property2);
		}
		
		@SuppressWarnings("unused")
		public String getProperty1()
		{
			return property1;
		}
		
		@SuppressWarnings("unused")
		public String getProperty2()
		{
			return property2;
		}
	}

	@PropertiesEqual(property1="property1", property1Label="property1Label", property2="property2", property2Label="property2Label")
	private static class ProtectedFields extends TestObjectBase
	{
		public ProtectedFields(String property1, String property2)
		{
			super(property1, property2);
		}
	}
	
	@PropertiesEqual(property1="property1", property1Label="property1Label", property2="property2", property2Label="property2Label")
	private static class PublicFields
	{
		@SuppressWarnings("unused")
		public final String property1;
		@SuppressWarnings("unused")
		public final String property2;
		
		protected PublicFields(String property1, String property2)
		{
			this.property1 = property1;
			this.property2 = property2;
		}
	}
	

}
