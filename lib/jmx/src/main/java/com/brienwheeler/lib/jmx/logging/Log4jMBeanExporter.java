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
package com.brienwheeler.lib.jmx.logging;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.jmx.HierarchyDynamicMBean;
import org.apache.log4j.spi.LoggerRepository;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.util.ReflectionUtils;

import com.brienwheeler.lib.jmx.AutoRegisterMBeanBase;

/**
 * Adapted from http://www.jroller.com/ray/entry/managing_log4j_logging_levels_for
 * 
 * @author bwheeler
 *
 */
@ManagedResource
public class Log4jMBeanExporter extends AutoRegisterMBeanBase implements DisposableBean
{
	private static final Log log = LogFactory.getLog(Log4jMBeanExporter.class);
	
    private static final String LOG4J_HIERARCHY_DEFAULT = "log4j:hierarchy=default";

    private static AtomicReference<HierarchyDynamicMBean> hierarchyDynamicMBean =
    		new AtomicReference<HierarchyDynamicMBean>();
    
    private boolean shutdown = false;
    private boolean stopOnZero = true;
    private boolean registeredHierarchy = false;
    private DelayedRegisterThread delayedRegisterThread;
    private HashSet<ObjectName> registeredNames = new HashSet<ObjectName>();
    private long periodicity = 0L;
    
    @Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		start();
	}

	@Override
	public void destroy() throws Exception
	{
		shutdown();
	}

	public void setStopOnZero(boolean stopOnZero)
	{
		this.stopOnZero = stopOnZero;
	}

	public void setPeriodicity(long periodicity)
	{
		this.periodicity = periodicity;
	}

	@ManagedOperation
	public String registerMBeans()
	{
		return "registered " + registerMBeansInternal() + " MBeans";
	}

	private synchronized void start()
	{
		shutdown = false;
		registerAndSchedule();
	}

	@SuppressWarnings("unchecked")
	private synchronized void shutdown()
	{
		shutdown = true;
		killDelayedThread();
		
    	MBeanServer server = JmxUtils.locateMBeanServer();
    	HierarchyDynamicMBean hdm = hierarchyDynamicMBean.get();
		for (ObjectName registeredName : registeredNames)
		{
			try {
				server.unregisterMBean(registeredName);
				
				// The AbstractDynamicMBean base class of the HierarchyDynamicMBean tracks these names too
				// and will later try to unregister them and throw warning log messages unless we violate its
				// privacy and clean out its internal bookkeeping
				Field fld = ReflectionUtils.findField(hdm.getClass(), "mbeanList", Vector.class);
				fld.setAccessible(true);
				Vector<ObjectName> mbeanList = (Vector<ObjectName>) ReflectionUtils.getField(fld, hdm);
				mbeanList.remove(registeredName);
			}
			catch (Exception e) {
				log.error("Error unregistering " + registeredName.getCanonicalName(), e);
			}
		}
		registeredNames.clear();
		
		if (registeredHierarchy)
		{
			try {
				ObjectName mbo = new ObjectName(LOG4J_HIERARCHY_DEFAULT);
				server.unregisterMBean(mbo);
			}
			catch (Exception e) {
				log.error("Error unregistering Log4j MBean Hierarchy", e);
			}
			registeredHierarchy = false;
		}
	}

	private synchronized void killDelayedThread()
	{
		if (delayedRegisterThread != null)
		{
			delayedRegisterThread.interrupt();
			delayedRegisterThread = null;
		}
	}
	
	private synchronized void registerAndSchedule()
	{
		// protect race condition where background thread was waiting to enter
		// this block when it was interrupted
		if (shutdown)
			return;
		
		killDelayedThread(); // we may start a new one, but throw away any currently waiting 

		int count = registerMBeansInternal();
		if ((periodicity != 0) && (!stopOnZero || count != 0))
		{
			log.debug("scheduling re-register in " + periodicity + " ms");
			delayedRegisterThread = new DelayedRegisterThread();
			delayedRegisterThread.start();
		}
	}
	
    private synchronized int registerMBeansInternal()
    {
    	// protect against JMX invocation while shutting down
    	if (shutdown)
    		return 0;
    	
    	MBeanServer server = JmxUtils.locateMBeanServer();
    	
    	HierarchyDynamicMBean hdm;
    	synchronized (this.getClass())
    	{
    		boolean usedNew = hierarchyDynamicMBean.compareAndSet(null, new HierarchyDynamicMBean());
            hdm = hierarchyDynamicMBean.get();
    		if (usedNew)
    		{
    	        try {
					ObjectName mbo = new ObjectName(LOG4J_HIERARCHY_DEFAULT);
					server.registerMBean(hdm, mbo);
					registeredHierarchy = true;
					// Add the root logger to the Hierarchy MBean
					hdm.addLoggerMBean(Logger.getRootLogger().getName());
				}
    	        catch (Exception e) {
					log.error("Error initializing Log4jMBeanExporter", e);
					return 0;
				}
    		}
    	}
        
        // Get each logger from the Log4J Repository and add it to
        // the Hierarchy MBean created above.
        LoggerRepository r = LogManager.getLoggerRepository();

        Enumeration<?> loggers = r.getCurrentLoggers();
    	int count = 0;
    	
        while ( loggers.hasMoreElements() ) {
            String name = ((Logger) loggers.nextElement()).getName();
            // this name definition copied from HierarchyDynamicMBean
            ObjectName objectName;
			try {
				objectName = new ObjectName("log4j", "logger", name);
			}
			catch (Exception e) {
				log.error("Error creating JMX name for " + name, e);
				continue;
			}
			
            if (!server.isRegistered(objectName))
            {
	            if (log.isDebugEnabled()) {
	            	log.debug("[contextInitialized]: Registering " + name);
	            }
	            registeredNames.add(hdm.addLoggerMBean(name));
	            count++;
            }
        }
        
        log.debug("registered " + count + " new Log4j MBeans");
        return count;
    }

    /**
     * Helper class to wait a specified number of ms and then re-register Log4j MBeans
     * 
     * @author bwheeler
     */
    private final class DelayedRegisterThread extends Thread
    {
		@Override
		public void run() {
			try {
				Thread.sleep(periodicity);
			}
			catch (InterruptedException e) {
				// aborted or shutdown
				Thread.currentThread().interrupt();
				return;
			}
			registerAndSchedule();
		}
    }
}
