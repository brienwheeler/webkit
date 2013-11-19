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

import java.util.Arrays;

import com.brienwheeler.lib.util.ValidationException;
import com.brienwheeler.lib.util.ValidationUtils;

/**
 * Class to hold command line arguments and iterate over them, throwing
 * IllegalStateException if there is no further argument when one is requested.
 * 
 * @author bwheeler
 */
public class CommandLine
{
	private String[] args;
	private int curArg = 0;
	
	/**
	 * Construct the CommandLine iterator against the given String array
	 * @param args arguments (usually a Java main function String array).  Cannot be null.
	 * @throws ValidationException if args is null
	 */
	public CommandLine(String[] args)
	{
		ValidationUtils.assertNotNull(args, "args cannot be null");
		// make copy of String array so caller modifications don't propagate
		this.args = Arrays.copyOf(args, args.length);
	}
	
	/**
	 * @return whether the iterator has further arguments that have not been iterated over.
	 */
	public boolean hasMoreArgs()
	{
		return curArg < args.length;
	}

	/**
	 * Peek but do not iterate over the next argument in the iterator.
	 * @return the next argument which is not considered to have been iterated over (i.e.,
	 * 		the next call to peekNextArg or getNextArg will return the same argument).
	 * @throws IllegalStateException if all arguments have been iterated over.
	 */
	public String peekNextArg()
	{
		if (curArg >= args.length)
			throw new IllegalStateException("no more arguments in CommandLine");
		
		return args[curArg];
	}
	
	/**
	 * Iterate over and return the next argument in the iterator.
	 * @param error message to be included in IllegalStateException if no argument available (allowed to be null).
	 * @return the next argument which is then considered to have been iterated over.
	 * @throws IllegalStateException if all arguments have been iterated over.
	 */
	public String getNextArg(String error)
	{
		if (curArg >= args.length)
			throw new IllegalStateException(error != null ? error : "no more arguments in CommandLine");
		
		return args[curArg++];
	}

	/**
	 * Iterate over and return the next argument in the iterator.
	 * @return the next argument which is then considered to have been iterated over.
	 * @throws IllegalStateException if all arguments have been iterated over.
	 */
	public String getNextArg()
	{
		return getNextArg(null);
	}

	/**
	 * Iterates over and discards the next argument, then iterates over and returns the argument after that
	 * (useful if you have used peekNextArg() to detect an option that requires a value stored in the
	 * immediately subsequent argument, as in "-configFile file.cfg").
	 * 
	 * @param error message to be included in IllegalStateException if at least two arguments are not
	 * 		available (allowed to be null).
	 * @return the second argument remaining in the iterator.
	 * @throws IllegalStateException if there are not at least two non-iterated arguments remaining
	 * 		in the iterator.
	 */
	public String skipAndGetNextArg(String error)
	{
		getNextArg(null);
		return getNextArg(error);
	}
	
}
