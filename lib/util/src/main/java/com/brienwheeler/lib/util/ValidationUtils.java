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
 * Utility functions for validation assertions.
 * 
 * @author Brien Wheeler
 */
public class ValidationUtils
{
    private ValidationUtils()
    {
    }

    /**
     * Assert that a given String is non-empty (not null and contains at least one non-whitespace character).
     * 
     * @param string the String to test
     * @param message error detail for the {@link ValidationException}
     * @return the trimmed string
     * @throws ValidationException if string == null || string.length() == 0
     */
    public static String assertNotEmpty(String string, String message)
    {
    	if ((string == null) || string.trim().isEmpty())
            throw new ValidationException(message);
    	return string.trim();
    }

    /**
     * Assert that a given object is not null.
     * 
     * @param object the object to test
     * @param message error detail for the {@link ValidationException}
     * @throws ValidationException if object == null
     */
    public static void assertNotNull(Object object, String message)
    {
        if (object == null)
            throw new ValidationException(message);
    }

    /**
     * Assert that a given condition is true.
     * 
     * @param condition the condition to test
     * @param message error detail for the {@link ValidationException}
     * @throws ValidationException if condition == false
     */
    public static void assertTrue(boolean condition, String message)
    {
        if (!condition)
            throw new ValidationException(message);
    }
    
    /**
     * Assert that a given condition is false.
     * 
     * @param condition the condition to test
     * @param message error detail for the {@link ValidationException}
     * @throws ValidationException if condition == true
     */
    public static void assertFalse(boolean condition, String message)
    {
        if (condition)
            throw new ValidationException(message);
    }
    
}
