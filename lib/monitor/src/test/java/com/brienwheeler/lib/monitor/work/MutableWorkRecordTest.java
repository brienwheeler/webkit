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

import org.junit.Assert;
import org.junit.Test;

public class MutableWorkRecordTest
{
	@Test
	public void testConstruct()
	{
		MutableWorkRecord mutableWorkRecord = new MutableWorkRecord("workName");
		Assert.assertEquals("workName", mutableWorkRecord.getWorkName());
		Assert.assertEquals(0, mutableWorkRecord.getWorkOkCount());
		Assert.assertEquals(0, mutableWorkRecord.getWorkOkDuration());
		Assert.assertEquals(0, mutableWorkRecord.getWorkErrorCount());
		Assert.assertEquals(0, mutableWorkRecord.getWorkErrorDuration());
	}

	@Test
	public void testRecordOk()
	{
		MutableWorkRecord mutableWorkRecord = new MutableWorkRecord("workName");
		mutableWorkRecord.recordWorkOk(100);
		mutableWorkRecord.recordWorkOk(150);
		Assert.assertEquals(2, mutableWorkRecord.getWorkOkCount());
		Assert.assertEquals(250, mutableWorkRecord.getWorkOkDuration());
	}

	@Test
	public void testRecordError()
	{
		MutableWorkRecord mutableWorkRecord = new MutableWorkRecord("workName");
		mutableWorkRecord.recordWorkError(1000);
		mutableWorkRecord.recordWorkError(1500);
		Assert.assertEquals(2, mutableWorkRecord.getWorkErrorCount());
		Assert.assertEquals(2500, mutableWorkRecord.getWorkErrorDuration());
	}
}