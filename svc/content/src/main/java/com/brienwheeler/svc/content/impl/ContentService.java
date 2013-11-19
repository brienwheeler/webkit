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
package com.brienwheeler.svc.content.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.brienwheeler.lib.svc.MonitoredWork;
import com.brienwheeler.lib.svc.impl.SpringStoppableServiceBase;
import com.brienwheeler.lib.util.ValidationUtils;
import com.brienwheeler.svc.content.ContentId;
import com.brienwheeler.svc.content.ContentServiceConfigException;
import com.brienwheeler.svc.content.ContentServiceException;
import com.brienwheeler.svc.content.IContentService;

public class ContentService extends SpringStoppableServiceBase
		implements IContentService
{
	public static final int DEFAULT_BUFFER_SIZE = 4096;
	
	private final AtomicReference<File> baseDirectory = new AtomicReference<File>();
	private AtomicInteger bufferSize = new AtomicInteger(DEFAULT_BUFFER_SIZE);
	
	@Override
	public void setBaseDirectory(File baseDirectory)
	{
		ValidationUtils.assertNotNull(baseDirectory, "baseDirectory cannot be null");
		
		if (!baseDirectory.exists())
			throw new ContentServiceConfigException("baseDirectory does not exist: " + baseDirectory.getAbsolutePath());
		if (!baseDirectory.isDirectory())
			throw new ContentServiceConfigException("baseDirectory is not a directory: " + baseDirectory.getAbsolutePath());
		if (!baseDirectory.canWrite())
			throw new ContentServiceConfigException("baseDirectory is not writable: " + baseDirectory.getAbsolutePath());
		
		if (!this.baseDirectory.compareAndSet(null,  baseDirectory))
			throw new ContentServiceConfigException("cannot change baseDirectory");
	}

	@Override
	@MonitoredWork
	public ContentId storeContent(String subdirectory, String extension, Reader reader)
	{
		ValidationUtils.assertNotNull(subdirectory, "subdirectory cannot be null");
		ValidationUtils.assertNotNull(reader, "reader cannot be null");
		
		File directory = new File(getBaseDirectory(), subdirectory);
		if (!directory.exists()) {
			if (!directory.mkdirs())
				throw new ContentServiceException("error creating subdirectory: " + directory.getAbsolutePath());
			log.info("created directory " + directory.getAbsolutePath());
		}
		if (!directory.canWrite())
			throw new ContentServiceException("subdirectory not writable: " + directory.getAbsolutePath());

		UUID uuid = UUID.randomUUID();
		String filename = uuid.toString();
		if (extension != null)
			filename = filename + "." + extension;
		
		File outfile = new File(directory, filename);
		if (outfile.exists())
			throw new ContentServiceException("unique file already exists: " + outfile.getAbsolutePath());

		FileWriter outWriter;
		try {
			outWriter = new FileWriter(outfile);
		} 
		catch (IOException e) {
			throw new ContentServiceException("error creating outfile: " + outfile.getAbsolutePath(), e);
		}
		
		int nread;
		char buf[] = new char[bufferSize.get()];
		
		try {
			while ((nread = reader.read(buf)) != -1)
			{
				outWriter.write(buf,  0,  nread);
			}
			outWriter.close();
			log.info("saved content to " + outfile.getAbsolutePath());
			return new ContentId(subdirectory, filename);
		}
		catch (IOException e) {
			tryToClean(outfile, outWriter);
			throw new ContentServiceException("error writing file: " + outfile.getAbsolutePath(), e);
		}
	}

	private File getBaseDirectory()
	{
		File baseDirectory = this.baseDirectory.get();
		if (baseDirectory == null)
			throw new IllegalStateException("baseDirectory not initialized");
		return baseDirectory;
	}
	
	private void tryToClean(File outfile, FileWriter outWriter)
	{
		try {
			outWriter.close();
		}
		catch (IOException e) {
			// nothing
		}
		
		if (outfile.exists() && !outfile.delete())
		{
			recordInterventionRequest("failed to clean up file after content store error: " + outfile.getAbsolutePath());
		}
	}
	
}
