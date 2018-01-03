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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class ParamVerifyingUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter
{
	private String parameterName;
	private boolean postOnly = true;

	public void setParameterName(String parameterName)
	{
		this.parameterName = parameterName;
	}

	@Override
	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response)
	{
		// we change the notion of how to handle a GET request to the login URL and
		// let it flow through to the DispatcherServlet so that we can host the form
		// and the filter on the same URL if desired.
        if (postOnly && !request.getMethod().equals("POST"))
        	return false;

        // call super class to determine if request URL matches target
		// if not, we definitely don't require authentication.
		if (!super.requiresAuthentication(request, response))
			return false;

		// when the URL matches, we still only require authentication if our parameterName
		// is null (meaning we don't care what submit button was used, or if the non-null
		// parameterName is present as a parameter (meaning that the submit button we care
		// about was used)
		return (parameterName == null) || (parameterName.isEmpty()) || (request.getParameter(parameterName) != null);
	}

	@Override
	public void setPostOnly(boolean postOnly)
	{
		this.postOnly = postOnly;
		super.setPostOnly(postOnly);
	}

}
