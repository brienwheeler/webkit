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
package com.brienwheeler.lib.test.error;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ExceptionTestUtils 
{
    private static Class<?>[][] signatures = { new Class[] {},
        new Class[] { String.class },
        new Class[] { String.class, Throwable.class },
        new Class[] { Throwable.class }, };
    
    private static Object[][] arguments = { new Object[] {},
        new Object[] { "message" },
        new Object[] { "message", new RuntimeException() },
        new Object[] { new RuntimeException() }, };

	private ExceptionTestUtils() {}
	
	public static <T extends Throwable> void testExceptionConstructors(Class<T> clazz)
	{
		for (int i=0; i<signatures.length; i++) {
			try {
				Constructor<T> constructor = clazz.getConstructor(signatures[i]);
				constructor.newInstance(arguments[i]);
			}
			catch (NoSuchMethodException e) {
				// ok to not implement all signatures
			}
			catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			catch (InstantiationException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
