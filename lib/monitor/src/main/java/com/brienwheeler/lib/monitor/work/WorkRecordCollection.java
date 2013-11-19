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
package com.brienwheeler.lib.monitor.work;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.brienwheeler.lib.util.ValidationUtils;

public class WorkRecordCollection
{
	private final long startTime;
	private long endTime;
	private final ConcurrentHashMap<String, MutableWorkRecord> workRecords = new ConcurrentHashMap<String, MutableWorkRecord>();
	private final String sourceName;
	
	public WorkRecordCollection(String sourceName, long startTime)
	{
		ValidationUtils.assertNotNull(sourceName,  "sourceName cannot be null");
		
		this.sourceName = sourceName;
		this.startTime = startTime;
	}
	
	private void recordWork(String workName, long duration, boolean ok)
	{
		workName = ValidationUtils.assertNotEmpty(workName, "workName cannot be empty");
		MutableWorkRecord mutableWorkRecord = workRecords.get(workName);
		if (mutableWorkRecord == null)
		{
			mutableWorkRecord = new MutableWorkRecord(workName);
			MutableWorkRecord existing = workRecords.putIfAbsent(workName, mutableWorkRecord);
			if (existing != null)
				mutableWorkRecord = existing;
		}
		if (ok)
			mutableWorkRecord.recordWorkOk(duration);
		else
			mutableWorkRecord.recordWorkError(duration);
	}
	
	public void recordWorkOk(String workName, long duration)
	{
		recordWork(workName, duration, true);
	}

	public void recordWorkError(String workName, long duration)
	{
		recordWork(workName, duration, false);
	}

	public int size()
	{
		return workRecords.size();
	}
	
	public long getStartTime()
	{
		return startTime;
	}

	public String getSourceName()
	{
		return sourceName;
	}
	
	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public Set<String> getWorkRecordNames()
	{
		return workRecords.keySet();
	}

	public WorkRecord getWorkRecord(String name)
	{
		MutableWorkRecord mutableWorkRecord = workRecords.get(name);
		if (mutableWorkRecord == null)
			return null;
		return new WorkRecord(mutableWorkRecord);
	}
	
}
