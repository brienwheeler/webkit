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
package com.brienwheeler.apps.main;

import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.util.ValidationException;

public class CommandLineTest
{
	private static final String ONE = "1";
	private static final String TWO = "2";
	
	@Test(expected = ValidationException.class)
	public void testConstructNull()
	{
		new CommandLine(null);
	}

	@Test
	public void testConstructEmpty()
	{
		CommandLine commandLine = new CommandLine(new String[0]);
		Assert.assertFalse(commandLine.hasMoreArgs());
	}

	@Test
	public void testConstruct()
	{
		CommandLine commandLine = new CommandLine(new String[] { ONE } );
		Assert.assertTrue(commandLine.hasMoreArgs());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testEmptyPeek()
	{
		CommandLine commandLine = new CommandLine(new String[0]);
		commandLine.peekNextArg();
	}

	@Test
	public void testPeek()
	{
		CommandLine commandLine = new CommandLine(new String[] { ONE } );
		Assert.assertEquals(ONE, commandLine.peekNextArg());
		Assert.assertEquals(ONE, commandLine.peekNextArg());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testSkipEmpty()
	{
		CommandLine commandLine = new CommandLine(new String[0]);
		commandLine.getNextArg();
	}

	@Test
	public void testSkipLast()
	{
		CommandLine commandLine = new CommandLine(new String[] { ONE });
		Assert.assertTrue(commandLine.hasMoreArgs());
		commandLine.getNextArg();
		Assert.assertFalse(commandLine.hasMoreArgs());
	}

	@Test
	public void testSkip()
	{
		CommandLine commandLine = new CommandLine(new String[] { ONE, TWO });
		Assert.assertTrue(commandLine.hasMoreArgs());
		commandLine.getNextArg();
		Assert.assertTrue(commandLine.hasMoreArgs());
		Assert.assertEquals(TWO, commandLine.peekNextArg());
	}

	@Test(expected = IllegalStateException.class)
	public void testSkipAndGetFail()
	{
		CommandLine commandLine = new CommandLine(new String[] { ONE });
		commandLine.skipAndGetNextArg("error");
	}

	@Test
	public void testSkipAndGet()
	{
		CommandLine commandLine = new CommandLine(new String[] { ONE, TWO });
		Assert.assertEquals(TWO, commandLine.skipAndGetNextArg("error"));
	}
}
