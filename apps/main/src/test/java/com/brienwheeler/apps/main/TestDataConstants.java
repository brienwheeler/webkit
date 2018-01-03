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

public class TestDataConstants
{
	public static final String PROPS_FILE1 = "classpath:com/brienwheeler/apps/main/test.properties";
	public static final String PROPS_FILE1_PROP = "com.brienwheeler.apps.main.testProp"; // matches property name in FILE
	public static final String PROPS_FILE1_VALUE = "testVal"; // matches value in FILE

	public static final String PROPS_FILE2 = "classpath:com/brienwheeler/apps/main/test2.properties";
	public static final String PROPS_FILE2_PROP = "com.brienwheeler.apps.main.testProp2"; // matches property name in FILE
	public static final String PROPS_FILE2_VALUE = "testVal2"; // matches value in FILE

	public static final String RMAP_TEST_PROP = "com.brienwheeler.apps.main.testResourceMapProp"; // matches resourceMap
	public static final String RMAP_TEST_VAL = "testResourceMapValue"; // matches resourceMap

	public static final String RMAP_CTX_CLASSPATH = "classpath:com/brienwheeler/apps/main/testContext.xml";
	public static final String RMAP_CTX_DIRECT = "testContextDirect";
	public static final String RMAP_CTX_INDIRECT = "testContextIndirect";
	public static final String RMAP_CTX_LIST = "testContextList";
	public static final String RMAP_CTX_CIRCULAR = "testContextCircular1";
	public static final String RMAP_CTX_BADPROPSPEC = "testBadPropSpec";
	
	public static final String RMAP_PROP_DIRECT = "testPropsDirect1";
	public static final String RMAP_PROP_INDIRECT = "testPropsIndirect";
	public static final String RMAP_PROP_LIST = "testPropsList";
	public static final String RMAP_PROP_CIRCULAR = "testPropsCircular1";

	public static final String RMAP_CTX_WITH_PROPS = "testContextWithProps";
	public static final String RMAP_CTX_WITH_PROPS2 = "testContextWithProps2";
	public static final String RMAP_CTX_WITH_PROPS3 = "testContextWithProps3";
	public static final String RMAP_CTX_WITH_PROPS4 = "testContextWithProps4";

	public static final String RMAP2_LOCATION = "classpath:resourceMap2.xml";
	public static final String RMAP3_LOCATION = "classpath:resourceMap3.xml";
	
	private TestDataConstants() {}
}
