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
package com.brienwheeler.svc.monitor.intervene.impl;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.junit.Test;

public class LoggingInterventionListenerTest
{
	@Test
	public void testDefaultLogEnabled()
	{
		LoggingInterventionListener listener = new LoggingInterventionListener();
		listener.recordInterventionRequest(this, "intervention required");
	}

	@Test
	public void testDefaultLogDisabled()
	{
		LoggingInterventionListener listener = new LoggingInterventionListener();
		listener.setEnabled(false);
		listener.recordInterventionRequest(this, "intervention required");
	}
	
	@Test
	public void testProvidedLogEnabled()
	{
		LoggingInterventionListener listener = new LoggingInterventionListener();
		TestLog testLog = new TestLog();
		listener.recordInterventionRequest(this, testLog, "intervention required");
		Assert.assertEquals(1, testLog.getWarnCount());
	}

	@Test
	public void testProvidedLogDisabled()
	{
		LoggingInterventionListener listener = new LoggingInterventionListener();
		listener.setEnabled(false);
		TestLog testLog = new TestLog();
		listener.recordInterventionRequest(this, testLog, "intervention required");
		Assert.assertEquals(0, testLog.getWarnCount());
	}

	public static class TestLog implements Log
	{
		private int debugCount = 0;
		private int traceCount = 0;
		private int infoCount = 0;
		private int warnCount = 0;
		private int errorCount = 0;
		private int fatalCount = 0;
		
		@Override
		public boolean isDebugEnabled() {
			return true;
		}

		@Override
		public void debug(Object arg0, Throwable arg1) {
			if (isDebugEnabled())
				debugCount++;
		}

		@Override
		public void debug(Object arg0) {
			debug(arg0, null);
		}

		@Override
		public boolean isTraceEnabled() {
			return true;
		}

		@Override
		public void trace(Object arg0, Throwable arg1) {
			if (isTraceEnabled())
				traceCount++;
		}

		@Override
		public void trace(Object arg0) {
			trace(arg0, null);
		}

		@Override
		public boolean isInfoEnabled() {
			return true;
		}

		@Override
		public void info(Object arg0, Throwable arg1) {
			if (isInfoEnabled())
				infoCount++;
		}

		@Override
		public void info(Object arg0) {
			info(arg0, null);
		}

		@Override
		public boolean isWarnEnabled() {
			return true;
		}

		@Override
		public void warn(Object arg0, Throwable arg1) {
			if (isWarnEnabled())
				warnCount++;
		}

		@Override
		public void warn(Object arg0) {
			warn(arg0, null);
		}
		
		@Override
		public boolean isErrorEnabled() {
			return true;
		}

		@Override
		public void error(Object arg0, Throwable arg1) {
			if (isErrorEnabled())
				errorCount++;
		}

		@Override
		public void error(Object arg0) {
			error(arg0, null);
		}

		@Override
		public boolean isFatalEnabled() {
			return true;
		}

		@Override
		public void fatal(Object arg0, Throwable arg1) {
			if (isFatalEnabled())
				fatalCount++;
		}

		@Override
		public void fatal(Object arg0) {
			fatal(arg0, null);
		}

		public int getDebugCount() {
			return debugCount;
		}

		public int getTraceCount() {
			return traceCount;
		}

		public int getInfoCount() {
			return infoCount;
		}

		public int getWarnCount() {
			return warnCount;
		}

		public int getErrorCount() {
			return errorCount;
		}

		public int getFatalCount() {
			return fatalCount;
		}
	}
}
