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
package com.brienwheeler.lib.spring.beans;

import java.io.FileNotFoundException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.brienwheeler.lib.test.spring.beans.PropertiesTestUtils;

public class PropertyPlaceholderConfigurerTest
{
    @Test
    public void testProcessLocationSimple()
    {
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/lib/spring/beans/PropertyPlaceholderConfigurer-test1.properties");
        Assert.assertEquals("val1", System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-test1.prop1"));
        Assert.assertEquals("val2", System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-test1.prop2"));
    }

    @Test
    public void testProcessProperties()
    {
    	PropertiesTestUtils.clearAllTestSystemProperties();
    	
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/lib/spring/beans/PropertyPlaceholderConfigurer-test1.properties");
        Assert.assertEquals("val1", System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-test1.prop1"));
        
        Properties properties = new Properties();
        properties.setProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-testProcessProperties",
        		"test-${com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-test1.prop1}");
        
        Assert.assertNull(System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-testProcessProperties"));
        PropertyPlaceholderConfigurer.processProperties(properties);
        Assert.assertEquals("test-val1", System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-testProcessProperties"));
    }

    @Test
    public void testProcessLocationRecursive()
    {
        /*
         * This test assures that placeholders in a file can reference other
         * placeholders in the same file, regardless of ordering. By using very
         * simple property name values, we can easily control their ordering in
         * the Properties Hashtable superclass
         */
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/lib/spring/beans/PropertyPlaceholderConfigurer-test2a.properties");
        Assert.assertEquals("val1", System.getProperty("1"));
        Assert.assertEquals("val1", System.getProperty("2"));
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/lib/spring/beans/PropertyPlaceholderConfigurer-test2b.properties");
        Assert.assertEquals("val2", System.getProperty("3"));
        Assert.assertEquals("val2", System.getProperty("4"));
    }

    @Test
    public void testSetPlaceholderPrefixAndSuffix()
    {
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/lib/spring/beans/PropertyPlaceholderConfigurer-test3.properties", "%{", "}");
        Assert.assertEquals("val1", System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-test3.prop1"));
        Assert.assertEquals("val1", System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-test3.prop2"));
    }

    @Test
    public void testIOException()
    {
        try
        {
            PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/lib/spring/beans/PropertyPlaceholderConfigurer-test-not-existent.properties");
            Assert.fail();
        }
        catch (BeanInitializationException e)
        {
            Assert.assertNotNull(e.getCause());
            Assert.assertEquals(FileNotFoundException.class, e.getCause().getClass());
        }
    }

    @Test
    public void testWithinContext()
    {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/brienwheeler/lib/spring/beans/PropertyPlaceholderConfigurer-test5Context.xml");
        Assert.assertNotNull(context);
        Assert.assertEquals("val1", System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-test5.prop1"));
        Assert.assertEquals("val1", System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-test5.prop2"));
    }

    @Test
    public void testMultipleWithinContext()
    {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/brienwheeler/lib/spring/beans/PropertyPlaceholderConfigurer-test6Context.xml");
        Assert.assertNotNull(context);
        Assert.assertEquals("val1", System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-test6a.prop1"));
        Assert.assertEquals("val2", System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-test6a.prop2"));
        Assert.assertEquals("val1", System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-test6b.prop1"));
        Assert.assertEquals("val2", System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-test6b.prop2"));
    }
    
    @Test
    public void testOrdering()
    {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/brienwheeler/lib/spring/beans/PropertyPlaceholderConfigurer-test7Context.xml");
        Assert.assertNotNull(context);
        Assert.assertEquals("APPLICATION_5", System.getProperty("com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer-test7.prop1"));
    }
}
