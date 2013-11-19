package com.brienwheeler.lib.jmx.logging;

import static org.junit.Assert.assertNotNull;

import javax.management.MBeanServer;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jmx.support.JmxUtils;

public class Log4jMBeanExporterTest
{
	@Test
	public void testRegisterInterfaceNoScanner() throws InterruptedException
	{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:com/brienwheeler/lib/jmx/logging/Log4jMBeanExporter-test1Context.xml");
		
		MBeanServer server = JmxUtils.locateMBeanServer();
		assertNotNull(server);

		// TODO: verify presence of expected bean

		context.close();
		
		// TODO: verify beans gone
	}
	

}
