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
package com.brienwheeler.apps.schematool;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.brienwheeler.lib.test.spring.beans.PropertiesTestUtils;

public class SchemaToolBeanTest
{
	private static final String CONTEXT_LOCATION_DPUP = "classpath:com/brienwheeler/apps/schematool/appEntityManagerFactory-defaultPUM.xml";
	private static final String CONTEXT_LOCATION_LVPUP = "classpath:com/brienwheeler/lib/db/appEntityManagerFactory.xml";
	
	private static final String CONTEXT_LOCATION_SCHEMATOOL = "classpath:com/brienwheeler/apps/schematool/schemaTool.xml";
	
    @Test
    public void testSchemaExportDefaultPUP()
    {
    	PropertiesTestUtils.clearAllTestSystemProperties();

    	System.setProperty("com.brienwheeler.apps.schematool.schemaTool.mode", "CLEAN");
    	System.setProperty("com.brienwheeler.apps.schematool.schemaTool.exec", "false");
    	System.setProperty("com.brienwheeler.apps.schematool.schemaTool.closeContextOnDone", "false");
    	System.setProperty("com.brienwheeler.apps.schematool.schemaTool.emfContextLocation", CONTEXT_LOCATION_DPUP);
        System.setProperty("com.brienwheeler.apps.schematool.schemaTool.emfPersistenceLocationsPropValue",
                "classpath:com/brienwheeler/lib/db/persistence.xml");
        
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONTEXT_LOCATION_SCHEMATOOL);
        context.close();
    }

    @Test
    public void testSchemaExportLocationValidatingPUP()
    {
    	PropertiesTestUtils.clearAllTestSystemProperties();

    	System.setProperty("com.brienwheeler.apps.schematool.schemaTool.mode", "CLEAN");
    	System.setProperty("com.brienwheeler.apps.schematool.schemaTool.exec", "false");
    	System.setProperty("com.brienwheeler.apps.schematool.schemaTool.closeContextOnDone", "false");
    	System.setProperty("com.brienwheeler.apps.schematool.schemaTool.emfContextLocation", CONTEXT_LOCATION_LVPUP);
        System.setProperty("com.brienwheeler.apps.schematool.schemaTool.emfPersistenceLocationsPropValue",
                "classpath:com/brienwheeler/lib/db/persistence.xml," +
                "classpath:com/brienwheeler/lib/db/persistence-test.xml");
        
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONTEXT_LOCATION_SCHEMATOOL);
        context.close();
    }

}
