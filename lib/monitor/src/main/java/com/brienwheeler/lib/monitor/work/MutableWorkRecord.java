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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.brienwheeler.lib.util.ValidationUtils;

public class MutableWorkRecord
{
	private final String workName;
	private final AtomicInteger workOkCount = new AtomicInteger(0);
	private final AtomicLong workOkDuration = new AtomicLong(0);
	private final AtomicInteger workErrorCount = new AtomicInteger(0);
	private final AtomicLong workErrorDuration = new AtomicLong(0);

	public MutableWorkRecord(String workName) {
		workName = ValidationUtils.assertNotEmpty(workName, "workName cannot be empty");
		this.workName = workName;
	}
	
	public void recordWorkOk(long duration)
	{
		workOkCount.incrementAndGet();
		workOkDuration.addAndGet(duration);
	}

	public void recordWorkError(long duration)
	{
		workErrorCount.incrementAndGet();
		workErrorDuration.addAndGet(duration);
	}

	public String getWorkName() {
		return workName;
	}

	public int getWorkOkCount() {
		return workOkCount.get();
	}

	public long getWorkOkDuration() {
		return workOkDuration.get();
	}

	public int getWorkErrorCount() {
		return workErrorCount.get();
	}

	public long getWorkErrorDuration() {
		return workErrorDuration.get();
	}
}
