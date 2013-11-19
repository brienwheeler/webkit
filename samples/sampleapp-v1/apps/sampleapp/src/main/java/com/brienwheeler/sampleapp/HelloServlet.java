package com.brienwheeler.sampleapp;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class HelloServlet implements Servlet 
{
	@Override
	public void init(ServletConfig config) throws ServletException
	{
	}

	@Override
	public ServletConfig getServletConfig()
	{
		return null;
	}

	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException
	{
		if (!(res instanceof HttpServletResponse))
			throw new RuntimeException("only supports HttpServletResponse");
		HttpServletResponse response = (HttpServletResponse) res;
		response.getWriter().print("Hello");
	}

	@Override
	public String getServletInfo()
	{
		return null;
	}

	@Override
	public void destroy()
	{
	}

}
