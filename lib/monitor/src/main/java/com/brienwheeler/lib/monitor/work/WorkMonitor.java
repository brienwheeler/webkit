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
package com.brienwheeler.lib.monitor.work;

import java.util.concurrent.atomic.AtomicReference;

import com.brienwheeler.lib.util.ValidationUtils;

public class WorkMonitor
{
	private final AtomicReference<WorkRecordCollection> workRecords;
			
	private final String sourceName;
	
	public WorkMonitor(String sourceName)
	{
		ValidationUtils.assertNotNull(sourceName, "sourceName cannot be null");
		
		this.sourceName = sourceName;
		this.workRecords = new AtomicReference<WorkRecordCollection>(new WorkRecordCollection(sourceName,
				System.currentTimeMillis()));
	}
	
	public void recordWorkOk(String workName, long duration)
	{
		workName = ValidationUtils.assertNotEmpty(workName, "workName cannot be empty");
		workRecords.get().recordWorkOk(workName, duration);
	}
	
	public void recordWorkError(String workName, long duration)
	{
		workName = ValidationUtils.assertNotEmpty(workName, "workName cannot be empty");
		workRecords.get().recordWorkError(workName, duration);
	}
	
	public WorkRecordCollection rollRecords()
	{
		long now = System.currentTimeMillis();
		WorkRecordCollection existing = workRecords.getAndSet(new WorkRecordCollection(sourceName, now));
		existing.setEndTime(now);
		return existing;
	}

	public String getSourceName()
	{
		return sourceName;
	}
	
}
