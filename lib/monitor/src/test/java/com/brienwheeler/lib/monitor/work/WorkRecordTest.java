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

import org.junit.Assert;
import org.junit.Test;

public class WorkRecordTest
{
	@Test
	public void testConstruct()
	{
		MutableWorkRecord mutableWorkRecord = new MutableWorkRecord("workName");
		mutableWorkRecord.recordWorkOk(100);
		mutableWorkRecord.recordWorkOk(200);
		mutableWorkRecord.recordWorkError(300);
		mutableWorkRecord.recordWorkError(400);
		mutableWorkRecord.recordWorkError(500);
		
		WorkRecord workRecord = new WorkRecord(mutableWorkRecord);
		Assert.assertEquals("workName", workRecord.getWorkName());
		Assert.assertEquals(2, workRecord.getWorkOkCount());
		Assert.assertEquals(300, workRecord.getWorkOkDuration());
		Assert.assertEquals(150F, workRecord.getWorkOkAvgDuration(), 0.01F);
		Assert.assertEquals(3, workRecord.getWorkErrorCount());
		Assert.assertEquals(1200, workRecord.getWorkErrorDuration());
		Assert.assertEquals(400F, workRecord.getWorkErrorAvgDuration(), 0.01F);
	}
}
