package com.brienwheeler.lib.jmx;

import org.junit.Test;
import org.springframework.jmx.JmxException;

import com.brienwheeler.lib.test.error.ExceptionTestUtils;

public class ExceptionsTest
{
	@Test
	public void testExceptions()
	{
		ExceptionTestUtils.testExceptionConstructors(JmxException.class);
	}
}
