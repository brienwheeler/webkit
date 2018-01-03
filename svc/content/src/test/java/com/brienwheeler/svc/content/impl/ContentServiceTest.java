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
package com.brienwheeler.svc.content.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.brienwheeler.lib.svc.impl.ServiceBaseTestUtil;
import com.brienwheeler.lib.util.FileUtils;
import com.brienwheeler.lib.util.ValidationException;
import com.brienwheeler.svc.content.ContentId;
import com.brienwheeler.svc.content.ContentServiceConfigException;

public class ContentServiceTest
{
	protected final Log log = LogFactory.getLog(getClass());

	protected File baseDirectory = new File("target/ContentServiceTest");
	protected ContentService contentService;
	
	@Before
	public void onSetUp()
	{
		contentService = new ContentService();
		if (baseDirectory.exists()) {
			if (!FileUtils.rmdir(baseDirectory))
				throw new IllegalStateException("failed to rmdir test directory: " + baseDirectory.getAbsolutePath());
		}
	}
	
	@After
	public void tearDown()
	{
		if (baseDirectory.exists())
			FileUtils.rmdir(baseDirectory);
	}
	
	@Test(expected = ValidationException.class)
	public void testSetBaseDirectoryFailNull()
	{
		contentService.setBaseDirectory(null);
	}

	@Test(expected = ContentServiceConfigException.class)
	public void testSetBaseDirectoryFailNoExist()
	{
		contentService.setBaseDirectory(baseDirectory);
	}

	@Test(expected = ContentServiceConfigException.class)
	public void testSetBaseDirectoryFailNotDir() throws IOException
	{
		try {
			FileWriter writer = new FileWriter(baseDirectory);
			writer.write("foo\n");
			writer.close();
			
			contentService.setBaseDirectory(baseDirectory);
			Assert.fail();
		}
		catch (ContentServiceConfigException e) {
			baseDirectory.delete();
			throw e;
		}
	}
	
	@Test(expected = ContentServiceConfigException.class)
	public void testSetBaseDirectoryFailCantWrite()
	{
		baseDirectory = new TestFile(baseDirectory.getName());
		((TestFile) baseDirectory).failCanWrite = true;
		
		baseDirectory.mkdirs();
		contentService.setBaseDirectory(baseDirectory);
	}

	@Test
	public void testSetBaseDirectoryFailCantChange()
	{
		baseDirectory.mkdirs();
		contentService.setBaseDirectory(baseDirectory);
		
		try {
			contentService.setBaseDirectory(baseDirectory);
			Assert.fail();
		}
		catch (ContentServiceConfigException e) {
		}
	}
	
	@Test
	public void testSetBaseDirectory()
	{
		baseDirectory.mkdirs();
		contentService.setBaseDirectory(baseDirectory);
	}
	
	protected void startService()
	{
		baseDirectory.mkdirs();
		contentService.setBaseDirectory(baseDirectory);
		contentService.start();
	}
	
	protected void stopService()
	{
		contentService.stopImmediate();
	}
	
	@Test
	public void testStoreFailNotInitialized()
	{
		contentService.start();

		ServiceBaseTestUtil.clearWorkRecords(contentService);

		String content = "foo\n";
		try {
			contentService.storeContent("dir1/dir2", null, new StringReader(content));
		}
		catch (IllegalStateException e) {
			// expected
		}

		ServiceBaseTestUtil.verifyWorkRecord(contentService, "storeContent", 0, 1);
	}
	
	@Test
	public void testStoreContentNoExtension()
	{
		startService();
		
		ServiceBaseTestUtil.clearWorkRecords(contentService);
		
		String content = "foo\n";
		ContentId contentId = contentService.storeContent("dir1/dir2", null, new StringReader(content));
		
		Assert.assertTrue(new File(baseDirectory, contentId.getSubdirectory() + File.separator + contentId.getFilename()).exists());
		stopService();

		ServiceBaseTestUtil.verifyWorkRecord(contentService, "storeContent", 1, 0);
}

	@Test
	public void testStoreContentExtension()
	{
		startService();
		
		ServiceBaseTestUtil.clearWorkRecords(contentService);

		String content = "foo\n";
		String extension = "foo";
		ContentId contentId = contentService.storeContent("dir1/dir2", extension, new StringReader(content));
		
		Assert.assertTrue(contentId.getFilename().endsWith("." + extension));
		Assert.assertTrue(new File(baseDirectory, contentId.getSubdirectory() + File.separator + contentId.getFilename()).exists());
		stopService();

		ServiceBaseTestUtil.verifyWorkRecord(contentService, "storeContent", 1, 0);
	}
	
	private static class TestFile extends File
	{
		private static final long serialVersionUID = 1L;
		
		public boolean failCanWrite = false;
		
		public TestFile(String filename) {
			super(filename);
		}

		@Override
		public boolean canWrite() {
			if (failCanWrite)
				return false;
			return super.canWrite();
		}
		
		
	}
}
