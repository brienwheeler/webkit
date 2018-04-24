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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Useful utility functions for dealing with Collections.
 * 
 * @author Brien Wheeler
 */
public class CollectionUtils
{
	private CollectionUtils()
	{
	}
	
	/**
	 * Enforce that a Collection contains no more than one element and return that element
	 * if it exists.
	 *
	 * @param <T> collection element type
	 * @param collection the Collection to examine
	 * @return null if the Collection is null or empty, the Collection's single object if
	 * 		its size is one, or throw if its size is greater than one.
	 * @throws IllegalArgumentException if the Collection contains more than one element
	 */
	public static <T> T singleObject(Collection<T> collection)
	{
		if (collection == null || collection.isEmpty())
			return null;
		if (collection.size() > 1)
			throw new IllegalArgumentException("more than one element in collection");
		return collection.iterator().next();
	}
	
	public static boolean areEqual(Collection<?> collection1, Collection<?> collection2)
	{
		if (collection1 == null && collection2 == null)
			return true;
		if (collection1 == null || collection2 == null)
			return false;
		if (collection1.size() != collection2.size())
			return false;
		for (Object member : collection1) {
			if (!collection2.contains(member))
				return false;
		}
		return true;
	}

	public static <T> List<T> makeList(T... values)
	{
		List<T> list = new ArrayList<T>();
		for (T value : values)
			list.add(value);
		return list;
	}
}
