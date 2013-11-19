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
package com.brienwheeler.apps.main;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.brienwheeler.lib.util.ValidationException;

public class MainBaseTest
{
	private static final String ONE = "1";
	private static final String TWO = "2";

	private static final String P = "-p";

	@Test(expected = ValidationException.class)
	public void testConstructNull()
	{
		new Main1(null);
	}
	
	@Test
	public void testConstruct()
	{
		Main1 main = new Main1(new String[0]);
		CommandLine commandLine = (CommandLine) ReflectionTestUtils.getField(main, "commandLine");
		Assert.assertFalse(commandLine.hasMoreArgs());
	}
	
	@Test
	public void testProcessArgs()
	{
		String[] args = new String[] { ONE, TWO };
		Main1 main = new Main1(args);
		main.run();
		ArrayList<String> processedArgs = main.getProcessedArgs();
		Assert.assertTrue(Arrays.equals(args, processedArgs.toArray(new String[processedArgs.size()])));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFailArgs()
	{
		String[] args = new String[] { ONE, TWO };
		Main1 main = new Main1(args);
		main.failArgProcessing = true;
		main.run();
	}

	@Test
	public void testSkipBaseArgs()
	{
		String[] args = new String[] { P, TestDataConstants.PROPS_FILE1 };
		Main1 main = new Main1(args);
		main.dontUseBastOpts();
		
		System.clearProperty(TestDataConstants.PROPS_FILE1_PROP);
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		main.run();
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		ArrayList<String> processedArgs = main.getProcessedArgs();
		Assert.assertTrue(Arrays.equals(args, processedArgs.toArray(new String[processedArgs.size()])));
	}
	
	@Test
	public void testProcessBaseArgs()
	{
		String[] args = new String[] { P, TestDataConstants.PROPS_FILE1 };
		Main1 main = new Main1(args);
		
		System.clearProperty(TestDataConstants.PROPS_FILE1_PROP);
		Assert.assertNull(System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		main.run();
		Assert.assertEquals(TestDataConstants.PROPS_FILE1_VALUE, System.getProperty(TestDataConstants.PROPS_FILE1_PROP));
		ArrayList<String> processedArgs = main.getProcessedArgs();
		Assert.assertTrue(processedArgs.isEmpty());
	}
	
	private static class Main1 extends MainBase
	{
		private final ArrayList<String> processedArgs = new ArrayList<String>();
		public boolean failArgProcessing = false;
		
		public Main1(String[] args)
		{
			super(args);
		}

		@Override
		protected void onRun()
		{
		}

		@Override
		protected boolean processCommandLineOption(CommandLine commandLine)
		{
			if (failArgProcessing)
				return false;
			
			if (super.processCommandLineOption(commandLine))
				return true;
				
			processedArgs.add(commandLine.getNextArg());
			return true;
		}

		public ArrayList<String> getProcessedArgs()
		{
			return processedArgs;
		}
		
		public void dontUseBastOpts()
		{
			setUseBaseOpts(false);
		}
	}
	
}
