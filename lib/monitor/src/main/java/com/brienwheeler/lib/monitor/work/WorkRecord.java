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

public class WorkRecord
{
	private final String workName;
	private final int workOkCount;
	private final long workOkDuration;
	private final int workErrorCount;
	private final long workErrorDuration;

	public WorkRecord(MutableWorkRecord workRecord) {
		this.workName = workRecord.getWorkName();
		this.workOkCount = workRecord.getWorkOkCount();
		this.workOkDuration = workRecord.getWorkOkDuration();
		this.workErrorCount = workRecord.getWorkErrorCount();
		this.workErrorDuration = workRecord.getWorkErrorDuration();
	}
	
	public String getWorkName() {
		return workName;
	}

	public int getWorkOkCount() {
		return workOkCount;
	}

	public long getWorkOkDuration() {
		return workOkDuration;
	}
	
	public float getWorkOkAvgDuration()
	{
		return workOkCount == 0 ? 0f : (float) workOkDuration / (float) workOkCount; 
	}

	public int getWorkErrorCount() {
		return workErrorCount;
	}

	public long getWorkErrorDuration() {
		return workErrorDuration;
	}

	public float getWorkErrorAvgDuration()
	{
		return workErrorCount == 0 ? 0f : (float) workErrorDuration / (float) workErrorCount; 
	}
}
