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
 * Useful utility functions for dealing with arrays.
 * 
 * @author Brien Wheeler
 */
public class ArrayUtils
{
    private ArrayUtils()
    {
    }

    /**
     * Create a comma-separated list of the array contents.
     * 
     * @param <T> the type of the array elements
     * @param array the array
     * @return a comma separated list of each element's toString(), or "null" if the array
     * 		element value is null
     */
    public static <T> String toString(T[] array)
    {
        StringBuffer buffer = new StringBuffer();
        if (array != null)
        {
            boolean first = true;
            for (T element : array)
            {
                if (!first)
                    buffer.append(",");
                buffer.append(element != null ? element.toString() : "null");
                first = false;
            }
        }
        return buffer.toString();
    }
    
    /**
     * Trim every element of the supplied String array.
     * @param array the array of Strings to trim
     * @return the passed-in array, which each element replaced by its trimmed self
     */
    public static String[] trimElements(String[] array)
    {
        if (array != null)
        {
            for (int i=0; i<array.length; i++)
            	if (array[i] != null)
            		array[i] = array[i].trim();
        }
        return array;
    }
    
    /**
     * Determine whether an array includes a given element.  This function returns 
     * 
     * @param <T> The type of the array
     * @param array The array contents to examine for membership
     * @param value The value to detect within the array contents.
     * @return true if the passed in value is null and any element of the array is null,
     * or any element of the array returns true from its equals() method when evaluated
     * against the passed in value.  False otherwise.
     */
    public static <T> boolean contains(T[] array, T value)
    {
    	for (T element : array)
    	{
    		if (element == null && value == null)
    			return true;
    		if (element != null && element.equals(value))
    			return true;
    	}
    	return false;
    }
}
