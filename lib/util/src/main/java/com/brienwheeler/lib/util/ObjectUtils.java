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
package com.brienwheeler.lib.util;

/**
 * Useful utility functions for dealing with Collections.
 * 
 * @author Brien Wheeler
 */
public class ObjectUtils
{
	private ObjectUtils()
	{
	}

	/**
	 * Evaluate two Objects for equality, with null safety
	 * @param o1 the first Object
	 * @param o2 the second Object
	 * @return true if both o1 and o2 are null, false if only one is null, otherwise
	 * 		o1.equals(o2)
	 */
	public static boolean areEqual(Object o1, Object o2)
	{
		// both null or same Java object
		if (o1 == o2)
			return true;
		// both non-null, so if one is null they are not equal
		if ((o1 == null) || (o2 == null))
			return false;
		// both non-null, not same Java object - fall into equals()
		return o1.equals(o2);
	}
	
	/**
	 * Return a unique identification String for an object, consisting of its simple class name
	 * followed by its {@link System.identityHashCode()}
	 * @param object
	 * @return unique identifier string
	 */
	public static String getUniqueId(Object object)
	{
		StringBuilder id = new StringBuilder();
		id.append(object.getClass().getSimpleName());
		id.append(":");
		id.append(Integer.toHexString(System.identityHashCode(object)));
		return id.toString();
	}
}
