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
package com.brienwheeler.lib.spring.beans;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.springframework.context.ApplicationContext;

import com.brienwheeler.lib.util.ValidationUtils;

public class AutowireUtils
{
	private AutowireUtils() {}
	
	public static <T> Collection<T> getAutowireBeans(ApplicationContext applicationContext,
			Class<T> clazz, Log log)
	{
		ValidationUtils.assertNotNull(applicationContext, "applicationContext cannot be null");
		ValidationUtils.assertNotNull(clazz, "clazz cannot be null");
		ValidationUtils.assertNotNull(log, "log cannot be null");
		
		Map<String,T> beans = applicationContext.getBeansOfType(clazz);
		if (beans.size() > 0) {
			log.info("autowiring " + beans.size() + " " + clazz.getSimpleName());
		}
		return beans.values();
	}
}
