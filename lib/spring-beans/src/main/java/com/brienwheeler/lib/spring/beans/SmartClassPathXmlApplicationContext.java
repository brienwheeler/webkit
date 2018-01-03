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

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SmartClassPathXmlApplicationContext extends ClassPathXmlApplicationContext
{
	
	public SmartClassPathXmlApplicationContext()
	{
		super();
	}

	public SmartClassPathXmlApplicationContext(ApplicationContext parent)
	{
		super(parent);
	}

	public SmartClassPathXmlApplicationContext(String path, Class<?> clazz)
			throws BeansException
	{
		super(path, clazz);
	}

	public SmartClassPathXmlApplicationContext(String... configLocations)
			throws BeansException
	{
		super(configLocations);
	}

	public SmartClassPathXmlApplicationContext(String configLocation)
			throws BeansException
	{
		super(configLocation);
	}

	public SmartClassPathXmlApplicationContext(String[] configLocations,
			ApplicationContext parent) throws BeansException
	{
		super(configLocations, parent);
	}

	public SmartClassPathXmlApplicationContext(String[] configLocations,
			boolean refresh, ApplicationContext parent) throws BeansException
	{
		super(configLocations, refresh, parent);
	}

	public SmartClassPathXmlApplicationContext(String[] configLocations,
			boolean refresh) throws BeansException
	{
		super(configLocations, refresh);
	}

	public SmartClassPathXmlApplicationContext(String[] paths, Class<?> clazz,
			ApplicationContext parent) throws BeansException
	{
		super(paths, clazz, parent);
	}

	public SmartClassPathXmlApplicationContext(String[] paths, Class<?> clazz)
			throws BeansException
	{
		super(paths, clazz);
	}

	/**
	 * Since this method in the parent class doesn't allow customization of the
	 * bean definition reader class, we copy the logic here.
	 */
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		XmlBeanDefinitionReader beanDefinitionReader = new SmartXmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment.
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions.
		initBeanDefinitionReader(beanDefinitionReader);
		loadBeanDefinitions(beanDefinitionReader);
	}

}
