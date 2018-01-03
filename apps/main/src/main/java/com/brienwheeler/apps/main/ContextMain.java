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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.brienwheeler.lib.jmx.MBeanRegistrationSupport;
import com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer;
import com.brienwheeler.lib.spring.beans.SmartClassPathXmlApplicationContext;

/**
 * A class to process properties files and launch application contexts based upon command line
 * arguments and a resource mapping file (by default located at classpath:resourceMap.xml).  Intended
 * to be used as the MainClass of an executable JAR that includes a resource map, one or more
 * application contexts, and all classes to support launch of those contexts.
 * 
 * a "-m" command line argument can be used to redefine the location of the resource map.
 * a "-n" command line argument can be used to not look for or process a resource map.
 * a "-p" command line argument will result in processing one or more properties files
 * 		based on the next command line argument (which can be a file location or an alias
 * 		from the resource map)
 * a "-c" command line argument will result in launching one or more application contexts
 * 		based on the next command line argument (which can be a beans file location or an alias
 * 		from the resource map).
 * 
 * -p and -c options are processed in order.  That is, if the command line is
 * "-c ctx1 -p props -c ctx2" then the props will only be in effect when ctx2 is launched.
 * 
 * The -m and -n arguments are detected and handled before any properties files are processed 
 * or contexts are launched (since those arguments may depend on the resource map).
 * 
 * See the Programmer's Guide for detailed information on the resourceMap.xml format.
 * 
 * @author bwheeler
 */
@ManagedResource
public class ContextMain extends MainBase implements ApplicationListener<ContextClosedEvent>
{
	private static final Log log = LogFactory.getLog(ContextMain.class);
	
	private static final String RESOURCE_MAP = "classpath:resourceMap.xml";
	private static final String BEAN_DEFAULTCONTEXT = "defaultContext";
	private static final String BEAN_CONTEXTMAP = "contextMap";
	private static final String BEAN_PROPERTIESMAP = "propertiesMap";
	private static final String BEAN_SYSPROPS = "systemProperties";

	private static final String PROPSPEC_PREFIX = "properties[";
	private static final String PROPSPEC_SUFFIX = "]";

	private boolean shuttingDown = false;

	private final List<IAction> commandLineActions = new ArrayList<IAction>();

	private boolean useResourceMap = true;
	private Properties systemProperties = null;

	// context data
	private Properties contextMap = null; // loaded resouceMap contents
	private String defaultContext = null; // context to launch if no -c argument
	private String resourceMapLocation = RESOURCE_MAP;
	private boolean launchDefaultContext = true;
	private final Map<String,List<AbstractApplicationContext>> contextAliasMap =
			new HashMap<String,List<AbstractApplicationContext>>(); // processed definitions results
	private final List<AbstractApplicationContext> contextLaunchOrder =
			new ArrayList<AbstractApplicationContext>(); // order launched to reverse for shutdown
	private final Set<String> contextsResolving = new HashSet<String>(); // circular dependency prevention

	// properties data
	private Properties propertiesMap = null; // loaded resouceMap contents
	private final Set<String> propertiesProcessed = new HashSet<String>(); // processed properties tracking
	private final Set<String> propertiesResolving = new HashSet<String>(); // circular dependency prevention
	
	/**
	 * Java main method to construct a ContextMain and run it.
	 * @param args JVM command line arguments
	 */
	public static void main(String[] args)
	{
		new ContextMain(args).run();
	}
	
	/**
	 * Construct the ContextMain object.
	 * @param args command line arguments specifying the properties files to process and the
	 * application contexts to launch.
	 */
	public ContextMain(String[] args)
	{
		super(args);
		// we will process our own command line so that property load operations are sequenced
		// with context load operations, not all done before context loads
		super.setUseBaseOpts(false);
	}

	@Override
	protected boolean processCommandLineOption(CommandLine commandLine)
	{
		// here for good form and just in case useBaseOpts is turned to true in the future
		if (super.processCommandLineOption(commandLine))
			return true;

		String opt = commandLine.peekNextArg();
		
		if (opt.equals("-p"))
		{
			commandLineActions.add(new PropertyLoadAction(commandLine.skipAndGetNextArg("-p option must have a value")));
			return true;
		}

		if (opt.equals("-c"))
		{
			commandLineActions.add(new ContextLoadAction(commandLine.skipAndGetNextArg("-c option must have a value")));
			launchDefaultContext = false;
			return true;
		}

		if (opt.equals("-m"))
		{
			resourceMapLocation = commandLine.skipAndGetNextArg("-m option must have a value");
			return true;
		}

		if (opt.equals("-n"))
		{
			commandLine.getNextArg();
			useResourceMap = false;
			return true;
		}

		return false;
	}

	@Override
	protected void onRun()
	{
		MBeanRegistrationSupport.registerMBean(this);
		
		if (useResourceMap)
			loadResourceMap();
		
		// before launching any contexts, process any properties specified in the resourceMap
		if (systemProperties != null)
			PropertyPlaceholderConfigurer.processProperties(systemProperties);
		
		for (IAction action : commandLineActions)
			action.execute();
		if (launchDefaultContext && defaultContext != null)
			findOrLaunchContext(defaultContext);
		
		waitForAllContexts();
	}

	private <T> T getResource(ApplicationContext context, String resourceName, Class<T> clazz)
	{
		try {
			return context.getBean(resourceName, clazz);
		}
		catch (NoSuchBeanDefinitionException e) {
			// ok
		}
		catch (Exception e) {
			log.error("error loading resourceMap element " + resourceName, e);
		}
		return null;
	}
	
	private void loadResourceMap()
	{
		ApplicationContext context = new ClassPathXmlApplicationContext(resourceMapLocation);
		defaultContext = getResource(context, BEAN_DEFAULTCONTEXT, String.class);
		systemProperties = getResource(context, BEAN_SYSPROPS, Properties.class);
		contextMap = getResource(context, BEAN_CONTEXTMAP, Properties.class);
		propertiesMap = getResource(context, BEAN_PROPERTIESMAP, Properties.class);
	}
	
	private void loadPropertySpec(String propertySpecList)
	{
		if (propertiesProcessed.contains(propertySpecList))
			return;
		
		if (propertiesResolving.contains(propertySpecList))
			throw new ResourceMapError("circular dependency: " + propertiesResolving);
		propertiesResolving.add(propertySpecList);
		
		try {
			// if the list has more than one spec string, split then iteratively recurse
			if (propertySpecList.contains(",")) {
				String[] propertySpecs = propertySpecList.split(",");
				for (String propertySpec : propertySpecs)
					loadPropertySpec(propertySpec);
				return;
			}
			
			// ok, propertySpecList has just one spec string in it.  Possibly alias or location.
			
			// check context map and recurse if present
			if (propertiesMap != null) {
				String resolvedSpecList = propertiesMap.getProperty(propertySpecList);
				if (resolvedSpecList != null) {
					loadPropertySpec(resolvedSpecList);
					return;
				}
			}

			// not an alias, must be a location at this point
			PropertyPlaceholderConfigurer.processLocation(propertySpecList);
			propertiesProcessed.add(propertySpecList);
		}
		finally {
			propertiesResolving.remove(propertySpecList);
		}
	}
	
	private List<AbstractApplicationContext> findOrLaunchContext(String contextSpecList)
	{
		// check to see if we've already figured this out
		List<AbstractApplicationContext> resultingContexts = contextAliasMap.get(contextSpecList);
		if (resultingContexts != null)
			return resultingContexts;

		// circular dependency detection
		if (contextsResolving.contains(contextSpecList))
			throw new ResourceMapError("circular dependency: " + contextSpecList);
		String originalContextSpecList = contextSpecList;
		contextsResolving.add(originalContextSpecList);
		
		try {
			// create container for eventual results
			resultingContexts = new ArrayList<AbstractApplicationContext>();
			
			// first check to see if it has a properties prefix and process/strip it from spec string
			// do this before looking for comma separated list below so that a comma-separated list
			// within the properties prefix gets handled correctly
			if (contextSpecList.startsWith(PROPSPEC_PREFIX)) {
				int specEnd = contextSpecList.indexOf(PROPSPEC_SUFFIX);
				if (specEnd == -1)
					throw new ResourceMapError("unterminated property spec in " + contextSpecList);
				
				String propertySpecList = contextSpecList.substring(PROPSPEC_PREFIX.length(), specEnd);
				contextSpecList = contextSpecList.substring(specEnd + PROPSPEC_SUFFIX.length()).trim();
				loadPropertySpec(propertySpecList);
			}

			// if the list has more than one spec string, split then recurse for first spec and remaining spec list
			int comma = contextSpecList.indexOf(",");
			if (comma != -1) {
				String currentSpec = contextSpecList.substring(0, comma).trim();
				resultingContexts.addAll(findOrLaunchContext(currentSpec));
				String remainingSpecList = contextSpecList.substring(comma + 1).trim();
				resultingContexts.addAll(findOrLaunchContext(remainingSpecList));
				contextAliasMap.put(contextSpecList, resultingContexts);
				return resultingContexts;
			}
	
			// be robust against a properties spec with no context -- user might do something like this:
			// properties[props1],context1,properties[props2],context2

			// in any case, an empty contextSpecList implies no further action
			if (contextSpecList.isEmpty()) {
				return resultingContexts;
			}
			
			// ok, contextSpecList has just one spec string in it.  Possibly alias or location.
	
			// check context map and recurse if present
			if (contextMap != null) {
				String resolvedSpecList = contextMap.getProperty(contextSpecList);
				if (resolvedSpecList != null) {
					resultingContexts.addAll(findOrLaunchContext(resolvedSpecList));
					contextAliasMap.put(contextSpecList, resultingContexts);
					return resultingContexts;
				}
			}
			
			// not an alias, must be a location at this point
			AbstractApplicationContext context = new SmartClassPathXmlApplicationContext(contextSpecList);
			synchronized (contextLaunchOrder) {
				// this allows a context to close itself during its startup (schematool does this)
				if (context.isActive())
					contextLaunchOrder.add(context);
				context.addApplicationListener(this);
			}
			resultingContexts.add(context);
			contextAliasMap.put(contextSpecList, resultingContexts);
			return resultingContexts;
		}
		finally {
			contextsResolving.remove(originalContextSpecList);
		}
	}
	
	private void waitForAllContexts()
	{
		try {
			synchronized (contextLaunchOrder) {
				onWaitingForShutdown();
				while (contextLaunchOrder.size() > 0)
					contextLaunchOrder.wait();
			}
		}
		catch (InterruptedException e) {
			log.error("interrupted waiting for context shutdown");
			Thread.currentThread().interrupt();
			return;
		}
	}
	
	@ManagedOperation
	public String shutdown()
	{
		AbstractApplicationContext context = null;
		int n = 0;

		do {
			context = null;
			synchronized (contextLaunchOrder) {
				shuttingDown = true;
				if (contextLaunchOrder.size() > 0)
					context = contextLaunchOrder.remove(contextLaunchOrder.size() - 1);
			}
			
			if (context != null) {
				context.close();
				n++;
			}
		} while (context != null);
		
		synchronized (contextLaunchOrder) {
			shuttingDown = false;
			contextLaunchOrder.notifyAll();
		}
		return "shutdown " + n + " contexts";
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event)
	{
		final AbstractApplicationContext context = (AbstractApplicationContext) event.getApplicationContext();
		boolean waitAndSignal = false;
		
		synchronized (contextLaunchOrder) {
			contextLaunchOrder.remove(context);
			// if that was the last context and it self-terminated
			if (contextLaunchOrder.size() == 0 && !shuttingDown)
				waitAndSignal = true;
		}
		
		if (waitAndSignal) {
			// since the ContextClosedEvent actually comes at the start of the close process,
			// start a background thread to monitor the state of the context and signal
			// the thread waiting in onRun() when it is finished closing.
			new Thread() {
				@Override
				public void run()
				{
					while (context.isActive()) {
						try {
							Thread.sleep(100L);
						}
						catch (InterruptedException e) {
							log.error("interrupted while waiting for last context to finish shutdown");
							Thread.currentThread().interrupt();
						}
					}
					
					synchronized (contextLaunchOrder) {
						contextLaunchOrder.notifyAll();
					}
				}
			}.start();
		}
	}
	
	protected void onWaitingForShutdown() {} // testability
	
	/**
	 * Polymorphic interface to represent command line actions that are executed in order
	 * as present on the command line.
	 * 
	 * @author bwheeler
	 */
	private static interface IAction
	{
		void execute();
	}
	
	/**
	 * Command line action to load properties
	 * 
	 * @author bwheeler
	 */
	private class PropertyLoadAction implements IAction
	{
		private String propertyLocation;

		public PropertyLoadAction(String propertyLocation)
		{
			this.propertyLocation = propertyLocation;
		}

		@Override
		public void execute()
		{
			loadPropertySpec(propertyLocation);
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + "[" + propertyLocation + "]";
		}
	}

	/**
	 * Command line action to load contexts
	 * 
	 * @author bwheeler
	 */
	private class ContextLoadAction implements IAction
	{
		private String contextLocation;

		public ContextLoadAction(String contextLocation)
		{
			this.contextLocation = contextLocation;
		}

		@Override
		public void execute()
		{
			findOrLaunchContext(contextLocation);
		}
		
		@Override
		public String toString()
		{
			return getClass().getSimpleName() + "[" + contextLocation + "]";
		}
	}
}
