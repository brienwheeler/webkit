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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.test.util.ReflectionTestUtils;

import com.brienwheeler.lib.util.ArrayUtils;

public class MessageSourceMessageInterpolatorTest
{
	private static final String STATIC_MESSAGE = "message";
	private static final String MESSAGE_CODE = "messageCode";
	
	private static final String FIELD1 = "field1";
	private static final String FIELD2 = "field2";
	private static final String FIELD1_CODE = "field1Code";
	private static final String FIELD2_CODE = "field2Code";
	private static final String ONE = "one";
	private static final String TWO = "two";

	@Test
	public void testSetMessageSource()
	{
		MessageSourceMessageInterpolator messageInterpolator = new MessageSourceMessageInterpolator();
		MessageSource messageSource = new MockMessageSource();
		messageInterpolator.setMessageSource(messageSource);
		Assert.assertSame(messageSource, ReflectionTestUtils.getField(messageInterpolator, "messageSource"));
	}
	
	@Test
	public void testInterpolateStaticMessage()
	{
		MockMessageSource messageSource = new MockMessageSource();
		
		MessageSourceMessageInterpolator messageInterpolator = new MessageSourceMessageInterpolator();
		messageInterpolator.setMessageSource(messageSource);
		
		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		ValidatorFactory validatorFactory = configuration.messageInterpolator(messageInterpolator).buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		
		PublicFields target = new StaticMessage(ONE, TWO);
		Set<ConstraintViolation<PublicFields>> violations = validator.validate(target);
		Assert.assertEquals(1, violations.size());
		ConstraintViolation<PublicFields> violation = violations.iterator().next();
		Assert.assertEquals(STATIC_MESSAGE, violation.getMessage());
	}
	
	@Test
	public void testInterpolateSimpleLookup()
	{
		String messageValue = "messageValue";
		
		MockMessageSource messageSource = new MockMessageSource();
		messageSource.put(MESSAGE_CODE, messageValue);
		
		MessageSourceMessageInterpolator messageInterpolator = new MessageSourceMessageInterpolator();
		messageInterpolator.setMessageSource(messageSource);
		
		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		ValidatorFactory validatorFactory = configuration.messageInterpolator(messageInterpolator).buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		
		PublicFields target = new ResolvedMessageStaticLabels(ONE, TWO);
		Set<ConstraintViolation<PublicFields>> violations = validator.validate(target);
		Assert.assertEquals(1, violations.size());
		ConstraintViolation<PublicFields> violation = violations.iterator().next();
		Assert.assertEquals(messageValue, violation.getMessage());
	}
	
	@Test
	public void testInterpolateRecursiveLookupBundle()
	{
		MockMessageSource messageSource = new MockMessageSource();
		messageSource.put(MESSAGE_CODE, "{" + FIELD1_CODE + "} does not equal {" + FIELD2_CODE + "}");
		messageSource.put(FIELD1_CODE, "field1Label");
		messageSource.put(FIELD2_CODE, "field2Label");
		
		MessageSourceMessageInterpolator messageInterpolator = new MessageSourceMessageInterpolator();
		messageInterpolator.setMessageSource(messageSource);
		
		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		ValidatorFactory validatorFactory = configuration.messageInterpolator(messageInterpolator).buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		
		PublicFields target = new ResolvedMessageStaticLabels(ONE, TWO);
		Set<ConstraintViolation<PublicFields>> violations = validator.validate(target);
		Assert.assertEquals(1, violations.size());
		ConstraintViolation<PublicFields> violation = violations.iterator().next();
		Assert.assertEquals("field1Label does not equal field2Label", violation.getMessage());
	}

	@Test
	public void testInterpolateRecursiveLookupAnnotationStatic()
	{
		MockMessageSource messageSource = new MockMessageSource();
		messageSource.put(MESSAGE_CODE, "{property1Label} does not equal {property2Label}");
		
		MessageSourceMessageInterpolator messageInterpolator = new MessageSourceMessageInterpolator();
		messageInterpolator.setMessageSource(messageSource);
		
		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		ValidatorFactory validatorFactory = configuration.messageInterpolator(messageInterpolator).buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		
		PublicFields target = new ResolvedMessageStaticLabels(ONE, TWO);
		Set<ConstraintViolation<PublicFields>> violations = validator.validate(target);
		Assert.assertEquals(1, violations.size());
		ConstraintViolation<PublicFields> violation = violations.iterator().next();
		Assert.assertEquals("field1Label does not equal field2Label", violation.getMessage());
	}

	@Test
	public void testInterpolateRecursiveLookupAnnotationBundle()
	{
		MockMessageSource messageSource = new MockMessageSource();
		messageSource.put(MESSAGE_CODE, "{property1Label} does not equal {property2Label}");
		messageSource.put(FIELD1_CODE, "field1Label");
		messageSource.put(FIELD2_CODE, "field2Label");

		MessageSourceMessageInterpolator messageInterpolator = new MessageSourceMessageInterpolator();
		messageInterpolator.setMessageSource(messageSource);
		
		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		ValidatorFactory validatorFactory = configuration.messageInterpolator(messageInterpolator).buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		
		PublicFields target = new ResolvedMessageRecursiveLabels(ONE, TWO);
		Set<ConstraintViolation<PublicFields>> violations = validator.validate(target);
		Assert.assertEquals(1, violations.size());
		ConstraintViolation<PublicFields> violation = violations.iterator().next();
		Assert.assertEquals("field1Label does not equal field2Label", violation.getMessage());
	}

	private abstract static class PublicFields
	{
		@SuppressWarnings("unused")
		public final String field1;
		@SuppressWarnings("unused")
		public final String field2;
		
		protected PublicFields(String field1, String field2)
		{
			this.field1 = field1;
			this.field2 = field2;
		}
	}

	@PropertiesEqual(property1=FIELD1, property1Label=FIELD1_CODE, property2=FIELD2, property2Label=FIELD2_CODE,
			message=STATIC_MESSAGE)
	private static class StaticMessage extends PublicFields
	{
		public StaticMessage(String field1, String field2)
		{
			super(field1, field2);
		}
	}
	
	@PropertiesEqual(property1=FIELD1, property1Label="field1Label", property2=FIELD2, property2Label="field2Label",
			message="{" + MESSAGE_CODE + "}")
	private static class ResolvedMessageStaticLabels extends PublicFields
	{
		public ResolvedMessageStaticLabels(String field1, String field2)
		{
			super(field1, field2);
		}
	}

	@PropertiesEqual(property1=FIELD1, property1Label="{" + FIELD1_CODE + "}",
			property2=FIELD2, property2Label="{" + FIELD2_CODE + "}",
			message="{" + MESSAGE_CODE + "}")
	private static class ResolvedMessageRecursiveLabels extends PublicFields
	{
		public ResolvedMessageRecursiveLabels(String field1, String field2)
		{
			super(field1, field2);
		}
	}

	private static class MockMessageSource implements MessageSource
	{
		private final Map<String,String> lookupMap = new HashMap<String,String>();
		
		public MockMessageSource()
		{
			lookupMap.put("testMessage", "testReplacement");
		}
		
		public void put(String code, String value)
		{
			lookupMap.put(code,  value);
		}
		
		@Override
		public String getMessage(String code, Object[] args, String defaultMessage, Locale locale)
		{
			String lookedUpValue = lookupMap.get(code);
			return lookedUpValue != null ? lookedUpValue : defaultMessage;
		}

		@Override
		public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException
		{
			String lookedUpValue = lookupMap.get(code);
			if (lookedUpValue != null)
				return lookedUpValue;
			return code;
		}

		@Override
		public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException
		{
			for (String code : resolvable.getCodes()) {
				String lookedUpValue = lookupMap.get(code);
				if (lookedUpValue != null)
					return lookedUpValue;
			}
			String defaultValue = resolvable.getDefaultMessage();
			if (defaultValue != null)
				return defaultValue;
			throw new NoSuchMessageException(ArrayUtils.toString(resolvable.getCodes()));
		}
	}
	
}
