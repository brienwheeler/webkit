package com.brienwheeler.lib.jmx;

import javax.management.MBeanServer;

import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jmx.support.JmxUtils;

public class AutoRegisterMBeanBaseTest
{
	@Test
	public void testRegisterInterfaceNoScanner()
	{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/brienwheeler/lib/jmx/AutoRegisterMBeanBase-test1Context.xml");
		
		MBeanServer server = JmxUtils.locateMBeanServer();
		assertNotNull(server);
		
		// TODO: verify presence of expected bean

		context.close();
	}
	
	@Test
	public void testRegisterAnnotationNoScanner()
	{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/brienwheeler/lib/jmx/AutoRegisterMBeanBase-test2Context.xml");
		
		MBeanServer server = JmxUtils.locateMBeanServer();
		assertNotNull(server);

		// TODO: verify presence of expected bean

		context.close();
	}
	
	@Test
	public void testRegisterInterfaceScanner()
	{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/brienwheeler/lib/jmx/AutoRegisterMBeanBase-test3Context.xml");
		
		MBeanServer server = JmxUtils.locateMBeanServer();
		assertNotNull(server);

		// TODO: verify presence of expected bean

		context.close();
	}
	
	@Test
	public void testRegisterAnnotationScanner()
	{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/brienwheeler/lib/jmx/AutoRegisterMBeanBase-test4Context.xml");
		
		MBeanServer server = JmxUtils.locateMBeanServer();
		assertNotNull(server);

		// TODO: verify presence of expected bean

		context.close();
	}
	
}
