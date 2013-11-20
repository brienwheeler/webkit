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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;

import com.brienwheeler.lib.util.ObjectUtils;
import com.brienwheeler.lib.util.ValidationUtils;

public class PropertiesEqualValidator implements ConstraintValidator<PropertiesEqual, Object>
{
	private String property1;
	private String property2;
	
	@Override
	public void initialize(PropertiesEqual constraintAnnotation)
	{
		property1 = ValidationUtils.assertNotEmpty(constraintAnnotation.property1(), "property1 cannot be empty");
		property2 = ValidationUtils.assertNotEmpty(constraintAnnotation.property2(), "property2 cannot be empty");
	}

	@Override
	public boolean isValid(Object target, ConstraintValidatorContext context)
	{
		Object value1 = getPropertyValue(target, property1);
		Object value2 = getPropertyValue(target, property2);
		
		return ObjectUtils.areEqual(value1, value2);
	}

	private Object getPropertyValue(Object target, String propertyName)
	{
		try {
			// try getter method first
			String getterName = "get"
					+ propertyName.substring(0, 1).toUpperCase()
					+ propertyName.substring(1);
			Method getter = target.getClass().getMethod(getterName);
			return getter.invoke(target);
		} catch (Exception e) {
			// ignore, try field level access
		}

		try {
			Field field = target.getClass().getField(propertyName);
			return field.get(target);
		} catch (Exception e) {
			// ignore, throw exception below
		}
		
		throw new ValidationException("failed to access property '" + propertyName + "'");
	}
}