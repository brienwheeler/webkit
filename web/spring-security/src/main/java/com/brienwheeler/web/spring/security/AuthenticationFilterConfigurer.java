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
package com.brienwheeler.web.spring.security;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class AuthenticationFilterConfigurer implements BeanPostProcessor
{
	private String filterName;
	private boolean postOnly = false;
	private String usernameParameter = "username";
	private String passwordParameter = "password";

	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
	{
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
	{
		if ((bean instanceof UsernamePasswordAuthenticationFilter) &&
				((filterName == null) || ((filterName != null) && filterName.equals(beanName))))
		{
			configureFilter((UsernamePasswordAuthenticationFilter) bean);
		}
		return bean;
	}

	private void configureFilter(UsernamePasswordAuthenticationFilter filter)
	{
		filter.setPostOnly(postOnly);
		filter.setUsernameParameter(usernameParameter);
		filter.setPasswordParameter(passwordParameter);
	}

	public void setFilterName(String filterName)
	{
		this.filterName = filterName;
	}
	
	public void setPostOnly(boolean postOnly)
	{
		this.postOnly = postOnly;
	}

	public void setUsernameParameter(String usernameParameter)
	{
		this.usernameParameter = usernameParameter;
	}

	public void setPasswordParameter(String passwordParameter)
	{
		this.passwordParameter = passwordParameter;
	}
}
