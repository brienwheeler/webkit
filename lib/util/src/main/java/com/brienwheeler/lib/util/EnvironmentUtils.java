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
package com.brienwheeler.lib.util;

/**
 * Allows classes to query which "environment" they are running in and conditionalize
 * behavior appropriately.  For example, certain JMX methods or other behaviors may be
 * desired in non-production environments but suppressed in production.  By setting
 * the "com.brienwheeler.lib.util.environment" system property to something other than
 * "production" (case insensitive), this function will start returning false.
 * 
 * @author bwheeler
 */
public class EnvironmentUtils
{
	public static final String SYSTEM_PROPERTY = "com.brienwheeler.lib.util.environment";
	public static final String PRODUCTION_ENVIRONMENT = "production";
	
	private EnvironmentUtils() {}
	
	public static boolean isProduction()
	{
		return PRODUCTION_ENVIRONMENT.equalsIgnoreCase(getEnvironment());
	}
	
	public static String getEnvironment()
	{
		String env = System.getProperty(SYSTEM_PROPERTY);
		return ((env != null) && !env.trim().isEmpty()) ? env.trim() : PRODUCTION_ENVIRONMENT;
	}
	
	public static boolean getBooleanProperty(String propertyName)
	{
		return Boolean.valueOf(System.getProperty(propertyName)).booleanValue();
	}
}
