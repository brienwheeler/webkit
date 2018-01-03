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
package com.brienwheeler.lib.validation;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.MessageInterpolator;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.MessageSource;

/**
 * Some of this borrowed from Hibernate's ResourceBundleMessageInterpolator.
 * 
 * @author bwheeler
 */
public class MessageSourceMessageInterpolator implements MessageInterpolator
{
	/**
	 * Regular expression used to do message interpolation.
	 */
	private static final Pattern messageParameterPattern = Pattern.compile( "(\\{[^\\}]+?\\})" );

	/**
	 * Underlying MessageSource for resolving strings
	 */
	private MessageSource messageSource;

	@Required
	public void setMessageSource(MessageSource messageSource)
	{
		this.messageSource = messageSource;
	}
	
	@Override
	public String interpolate(String messageTemplate, Context context)
	{
		return interpolate(messageTemplate, context, Locale.getDefault());
	}

	@Override
	public String interpolate(String messageTemplate, Context context, Locale locale)
	{
		String resolvedMessage = messageSource.getMessage(messageTemplate, null, locale);
		
		// replaceVariables
		Matcher matcher = messageParameterPattern.matcher(resolvedMessage);
		StringBuffer sb = new StringBuffer();
		String resolvedParameterValue;
		while (matcher.find()) {
			String parameterValue = removeCurlyBraces(matcher.group(1));
			
			// first check the annotation constraints
			Object constraintValue = context.getConstraintDescriptor().getAttributes().get(parameterValue);
			if (constraintValue != null)
				parameterValue = constraintValue.toString();
			
			// recurse back to MessageSource to find replacement value 
			resolvedParameterValue = interpolate(parameterValue, context, locale);
			matcher.appendReplacement(sb, escapeMetaCharacters(resolvedParameterValue));
		}
		matcher.appendTail( sb );
		resolvedMessage = sb.toString();
		
		// last but not least we have to take care of escaped literals
		resolvedMessage = resolvedMessage.replace( "\\{", "{" );
		resolvedMessage = resolvedMessage.replace( "\\}", "}" );
		resolvedMessage = resolvedMessage.replace( "\\\\", "\\" );
		return resolvedMessage;
	}

	/**
	 * @param s The string in which to replace the meta characters '$' and '\'.
	 *
	 * @return A string where meta characters relevant for {@link Matcher#appendReplacement} are escaped.
	 */
	private String escapeMetaCharacters(String s) {
		String escapedString = s.replace( "\\", "\\\\" );
		escapedString = escapedString.replace( "$", "\\$" );
		return escapedString;
	}

	private String removeCurlyBraces(String parameter) {
		return parameter.substring( 1, parameter.length() - 1 );
	}
}
