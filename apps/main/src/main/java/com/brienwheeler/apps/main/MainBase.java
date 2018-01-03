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

import com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer;
import com.brienwheeler.lib.util.ValidationException;
import com.brienwheeler.lib.util.ValidationUtils;

/**
 * Abstract base class for classes that accept command line arguments and are executed.  When the
 * run() method is called, the command line arguments are processed and then the subclass' onRun()
 * method is invoked.
 * 
 * If useBaseOpts is true, then this class will handle "-p" command line options, processing
 * properties files when those arguments are handled by processComandLineOption().  If useBaseOpts
 * is false, the subclass is responsible for all command line argument handling.
 * 
 * @author bwheeler
 */
public abstract class MainBase
{
	protected abstract void onRun();
	
	private boolean useBaseOpts = true;
	private final CommandLine commandLine;

	/**
	 * Construct the object with its command line arguments
	 * @param args JVM command line arguments
	 * @throws ValidationException is args is null
	 */
	public MainBase(String[] args)
	{
		ValidationUtils.assertNotNull(args, "args cannot be null");
		this.commandLine = new CommandLine(args);
	}

	/**
	 * Start the execution process (process command line arguments in order and then
	 * invoke the object's onRun() method).
	 */
	public final void run()
	{
		processCommandLine();
		onRun();
	}

	/**
	 * Using a CommandLine object, iterate over the command line arguments, calling processCommandLineOption.
	 * @throws IllegalArgumentException on an unrecognized command line option (as indicated by a return value
	 * 		of false from processCommandLineOption).
	 */
	protected final void processCommandLine()
	{
		while (commandLine.hasMoreArgs())
		{
			if (!processCommandLineOption(commandLine))
			{
				throw new IllegalArgumentException("unknown option " + commandLine.peekNextArg());
			}
		}
	}
	
	/**
	 * Intended to be overridden by subclasses.  If useBaseOpts is set, this method will handle "-p"
	 * command line arguments, processing the property file in the location specified by the next (required)
	 * argument.
	 * @param commandLine the internal command line argument iterator to provide convenient access to
	 * 		the current and following arguments
	 * @return true if the command line option was understood and processed, false otherwise.
	 */
	protected boolean processCommandLineOption(CommandLine commandLine)
	{
		if (useBaseOpts)
		{
			String opt = commandLine.peekNextArg();
			
			if (opt.equals("-p"))
			{
				String optVal = commandLine.skipAndGetNextArg("-p option requires value");
				PropertyPlaceholderConfigurer.processLocation(optVal);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Subclasses can use this to turn off automatic "-p" processing in processCommandLineOption.
	 * Alternatively, since that is all that is currently in processCommandLineOption, subclasses
	 * could simply never call super.processCommandLineOption.
	 * 
	 * @param useBaseOpts whether this base class should automatically process "-p" comand line
	 * 		options when iterating over the command line.
	 */
	protected void setUseBaseOpts(boolean useBaseOpts)
	{
		this.useBaseOpts = useBaseOpts;
	}
}
