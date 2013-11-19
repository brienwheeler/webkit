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
package com.brienwheeler.svc.email.impl;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.springframework.core.io.ClassPathResource;

import com.brienwheeler.lib.util.ValidationUtils;
import com.brienwheeler.svc.email.EmailServiceConfigException;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;

public class SmartTemplateLoader implements TemplateLoader
{
	private static final String CLASSPATH_PREFIX = "classpath:";
	
	private FileTemplateLoader fileLoader;

	public SmartTemplateLoader(File baseDirectory)
	{
		ValidationUtils.assertNotNull(baseDirectory, "baseDirectory cannot be null");
		
		try {
			fileLoader = new FileTemplateLoader(baseDirectory);
		}
		catch (IOException e) {
			throw new EmailServiceConfigException(e);
		}
	}

	@Override
	public Object findTemplateSource(String name) throws IOException
	{
		ValidationUtils.assertNotNull(name, "name cannot be null");
		
		if (!name.startsWith(CLASSPATH_PREFIX))
			return fileLoader.findTemplateSource(name);

		ClassPathResource resource = new ClassPathResource(name.substring(CLASSPATH_PREFIX.length()));
		if (resource.exists())
			return new ClassPathTemplateSource(resource);
		else
			return null;
	}

	@Override
	public Reader getReader(Object templateSource, String encoding) throws IOException 
	{
		ValidationUtils.assertNotNull(templateSource, "templateSource cannot be null");
		
		if (templateSource instanceof File)
			return fileLoader.getReader(templateSource, encoding);
		
		if (templateSource instanceof ClassPathTemplateSource)
			return ((ClassPathTemplateSource) templateSource).getReader(encoding);
		
		throw new EmailServiceConfigException("unexpected templateSource class " + templateSource.getClass().getName());
	}
	
	@Override
	public long getLastModified(Object templateSource)
	{
		ValidationUtils.assertNotNull(templateSource, "templateSource cannot be null");
		
		if (templateSource instanceof File)
			return fileLoader.getLastModified(templateSource);

		if (templateSource instanceof ClassPathTemplateSource)
			return ((ClassPathTemplateSource) templateSource).getLastModified();
		
		throw new EmailServiceConfigException("unexpected templateSource class " + templateSource.getClass().getName());
	}

	@Override
	public void closeTemplateSource(Object templateSource) throws IOException
	{
		ValidationUtils.assertNotNull(templateSource, "templateSource cannot be null");
		
		if (templateSource instanceof File) {
			fileLoader.closeTemplateSource(templateSource);
			return;
		}
		
		if (templateSource instanceof ClassPathTemplateSource) {
			((ClassPathTemplateSource) templateSource).close();
			return;
		}
		
		throw new EmailServiceConfigException("unexpected templateSource class " + templateSource.getClass().getName());
	}
	
}
