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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.test.UtilsTestBase;
import com.brienwheeler.lib.test.io.FileTestUtils;

public class FileUtilsTest extends UtilsTestBase<FileUtils>
{
	@Override
	protected Class<FileUtils> getUtilClass()
	{
		return FileUtils.class;
	}
	
	@Test
	public void testNotExist() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		File testDir = new File(targetDir, UUID.randomUUID().toString());
		Assert.assertFalse(testDir.exists());
		Assert.assertTrue(FileUtils.rmdir(testDir));
	}

	@Test(expected=IllegalStateException.class)
	public void testNotDirectory() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		File testFile = FileTestUtils.makeTestFile(targetDir);
		try {
			FileUtils.rmdir(testFile);
			Assert.fail();
		}
		finally {
			testFile.delete();
		}
	}
	
	@Test
	public void testExists() throws IOException
	{
		File targetDir = FileTestUtils.getTargetDirectory();
		File testDir = new File(targetDir, UUID.randomUUID().toString());

		Assert.assertFalse(testDir.exists());
		testDir.mkdirs();
		Assert.assertTrue(testDir.exists());
		
		File testSubFile = FileTestUtils.makeTestFile(testDir);
		try {
			Assert.assertTrue(testSubFile.exists());
			
			Assert.assertTrue(FileUtils.rmdir(testDir));
			Assert.assertFalse(testDir.exists());
			Assert.assertFalse(testSubFile.exists());
		}
		finally {
			testSubFile.delete();
			testDir.delete();
		}
	}

}
