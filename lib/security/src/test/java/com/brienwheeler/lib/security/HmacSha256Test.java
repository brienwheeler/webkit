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
package com.brienwheeler.lib.security;

import junit.framework.Assert;

import org.junit.Test;

import com.brienwheeler.lib.test.UtilsTestBase;

public class HmacSha256Test extends UtilsTestBase<HmacSha256>
{
	private static final String TEST_SIGN_DATA = "GET\n" + 
			"elasticmapreduce.amazonaws.com\n" + 
			"/\n" +
			"AWSAccessKeyId=AKIAIOSFODNN7EXAMPLE&Action=DescribeJobFlows&SignatureMethod=HmacSHA256&SignatureVersion=2&Timestamp=2011-10-03T15%3A19%3A30&Version=2009-03-31";
	private static final String TEST_SIGN_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
	private static final String TEST_SIGNATURE = "i91nKc4PWAt0JJIdXwz9HxZCJDdiy6cf/Mj6vPxyYIs=";
	
	@Override
	protected Class<HmacSha256> getUtilClass()
	{
		return HmacSha256.class;
	}

	@Test
	public void testBase64HmacSha256()
	{
		Assert.assertEquals(TEST_SIGNATURE, HmacSha256.base64HmacSha256(TEST_SIGN_KEY, TEST_SIGN_DATA));
	}
}
