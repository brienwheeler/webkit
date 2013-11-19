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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.test.io.FileTestUtils;
import com.brienwheeler.lib.util.ValidationException;
import com.brienwheeler.svc.email.EmailServiceConfigException;

public class SmartTemplateLoaderTest
{
	private static final String TEST_TEMPLATE_FILENAME = "test.ftl";
	private static final String CLASSPATH_EXISTING_RESOURCE = "classpath:com/brienwheeler/svc/email/templates/test1.ftl";
	private static final String CLASSPATH_MISSING_RESOURCE = "classpath:com/brienwheeler/svc/email/templates/doesNotExist.ftl";
	
	@Test
	public void testConstruct()
	{
		if (System.getProperty("os.name").toLowerCase().contains("windows"))
			new SmartTemplateLoader(new File("C:/"));
		else
			new SmartTemplateLoader(new File("/"));
	}

	@Test(expected = EmailServiceConfigException.class)
	public void testConstructNoSuchDir()
	{
		new SmartTemplateLoader(new File("/NoSuchDirectoryShouldExist"));
	}
	
	@Test
	public void testFindTemplateSourceFile() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		makeTestTemplate(targetDir, TEST_TEMPLATE_FILENAME);
		
		try {
			SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
			Object templateSource = loader.findTemplateSource(TEST_TEMPLATE_FILENAME);
			Assert.assertNotNull(templateSource);
			Assert.assertEquals(File.class, templateSource.getClass());
		}
		finally {
			deleteTestTemplate(targetDir, TEST_TEMPLATE_FILENAME);
		}
	}

	@Test
	public void testFindTemplateSourceFileNotFound() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		deleteTestTemplate(targetDir, TEST_TEMPLATE_FILENAME);
		SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
		
		Object templateSource = loader.findTemplateSource(TEST_TEMPLATE_FILENAME);
		Assert.assertNull(templateSource);
	}
	
	@Test
	public void testFindTemplateSourceClasspath() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
		
		Object templateSource = loader.findTemplateSource(CLASSPATH_EXISTING_RESOURCE);
		Assert.assertNotNull(templateSource);
		Assert.assertEquals(ClassPathTemplateSource.class, templateSource.getClass());
	}

	@Test
	public void testFindTemplateSourceClasspathNotFound() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
		
		Object templateSource = loader.findTemplateSource(CLASSPATH_MISSING_RESOURCE);
		Assert.assertNull(templateSource);
	}

	@Test(expected = ValidationException.class)
	public void testGetReaderNull() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
		loader.getReader(null, "UTF-8");
	}
	
	@Test
	public void testGetReaderFile() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		makeTestTemplate(targetDir, TEST_TEMPLATE_FILENAME);
		
		try {
			SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
			Object templateSource = loader.findTemplateSource(TEST_TEMPLATE_FILENAME);
			Assert.assertNotNull(templateSource);
			Reader reader = loader.getReader(templateSource, "UTF-8");
			Assert.assertNotNull(reader);
			reader.close();
		}
		finally {
			deleteTestTemplate(targetDir, TEST_TEMPLATE_FILENAME);
		}
	}
	
	@Test
	public void testGetReaderClasspath() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		
		SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
		Object templateSource = loader.findTemplateSource(CLASSPATH_EXISTING_RESOURCE);
		Assert.assertNotNull(templateSource);
		Reader reader = loader.getReader(templateSource, "UTF-8");
		Assert.assertNotNull(reader);
	}

	@Test(expected = EmailServiceConfigException.class)
	public void testGetReaderBadSource() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		
		SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
		loader.getReader(new Object(), "UTF-8");
	}

	@Test
	public void testGetLastModifiedFile() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		makeTestTemplate(targetDir, TEST_TEMPLATE_FILENAME);
		
		try {
			SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
			Object templateSource = loader.findTemplateSource(TEST_TEMPLATE_FILENAME);
			Assert.assertNotNull(templateSource);
			Assert.assertTrue(loader.getLastModified(templateSource) > 0);
		}
		finally {
			deleteTestTemplate(targetDir, TEST_TEMPLATE_FILENAME);
		}
	}
	
	@Test
	public void testGetLastModifiedClasspath() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		
		SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
		Object templateSource = loader.findTemplateSource(CLASSPATH_EXISTING_RESOURCE);
		Assert.assertNotNull(templateSource);
		Assert.assertTrue(loader.getLastModified(templateSource) > 0);
	}

	@Test(expected = EmailServiceConfigException.class)
	public void testGetLastModifiedBadSource() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		
		SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
		loader.getLastModified(new Object());
	}

	@Test
	public void testCloseFile() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		makeTestTemplate(targetDir, TEST_TEMPLATE_FILENAME);
		
		try {
			SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
			Object templateSource = loader.findTemplateSource(TEST_TEMPLATE_FILENAME);
			Assert.assertNotNull(templateSource);
			loader.closeTemplateSource(templateSource);
		}
		finally {
			deleteTestTemplate(targetDir, TEST_TEMPLATE_FILENAME);
		}
	}
	
	@Test
	public void testCloseClasspath() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		
		SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
		Object templateSource = loader.findTemplateSource(CLASSPATH_EXISTING_RESOURCE);
		Assert.assertNotNull(templateSource);
		loader.closeTemplateSource(templateSource);
	}

	@Test(expected = EmailServiceConfigException.class)
	public void testCloseBadSource() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		
		SmartTemplateLoader loader = new SmartTemplateLoader(targetDir);
		loader.closeTemplateSource(new Object());
	}

	private File makeTestTemplate(File targetDir, String name) throws IOException
	{
		File template = new File(targetDir, name);
		FileWriter writer = new FileWriter(template);
		writer.write("Template Content\n");
		writer.close();
		return template;
	}

	private void deleteTestTemplate(File targetDir, String name) throws IOException
	{
		File template = new File(targetDir, "test.ftl");
		if (template.exists())
			template.delete();
		if (template.exists())
			throw new IOException("failed to delete test template");
	}
}
