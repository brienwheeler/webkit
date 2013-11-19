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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * This drop-in replacement for the Spring PropertyPlaceholderConfigurer
 * facilitates property inheritance from parent contexts to children contexts by
 * setting resolved property values as System properties (if not already set)
 * and always using mode SYSTEM_PROPERTIES_MODE_OVERRIDE.
 * <p>
 * It also facilitates the use of many granular property files in a context by
 * merging all property file contents within a single context together and
 * processing them all at once, so inter-file dependencies are easily handled.
 * 
 * @author Brien Wheeler
 */
public class PropertyPlaceholderConfigurer extends
        org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
{
    private static final Log log = LogFactory.getLog(PropertyPlaceholderConfigurer.class);

    private static final Map<ConfigurableListableBeanFactory, ContextData> contextDataMap = new HashMap<ConfigurableListableBeanFactory, ContextData>();
    private static final String OBFUSCATED_LOG_VALUE = "********";

    private PropertyPlaceholderOrder placeholderOrder;
    private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;
    private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

    public PropertyPlaceholderConfigurer()
    {
        super.setSystemPropertiesMode(SYSTEM_PROPERTIES_MODE_OVERRIDE);
    }

    @Override
    public void setPlaceholderPrefix(String placeholderPrefix)
    {
        super.setPlaceholderPrefix(placeholderPrefix);
        this.placeholderPrefix = placeholderPrefix;
    }

    @Override
    public void setPlaceholderSuffix(String placeholderSuffix)
    {
        super.setPlaceholderSuffix(placeholderSuffix);
        this.placeholderSuffix = placeholderSuffix;
    }

	@Override
	public int getOrder()
	{
		return placeholderOrder != null ? placeholderOrder.getOrder() : super.getOrder();
	}
    
    public void setPlaceholderOrder(PropertyPlaceholderOrder placeholderOrder)
    {
		this.placeholderOrder = placeholderOrder;
	}

    @Override
    protected void processProperties(
            ConfigurableListableBeanFactory beanFactoryToProcess,
            Properties properties) throws BeansException
    {
        ContextData contextData;
        synchronized (contextDataMap)
        {
            contextData = contextDataMap.get(beanFactoryToProcess);
            if (contextData == null)
            {
                contextData = new ContextData();
                contextDataMap.put(beanFactoryToProcess, contextData);
            }
        }
        
        int ppcCount = beanFactoryToProcess.getBeanNamesForType(PropertyPlaceholderConfigurer.class, true, false).length;
        synchronized (contextData)
        {
        	/*
        	 * Spring calls all placeholder configurers according to their Order value.
        	 * We want to aggregate all placeholder definitions at the same Order value and
        	 * resolve them at the sane time, to ease inter-file dependency management.
        	 */
        	
        	/*
        	 * First we check to see if we have previously aggregated properties for a
        	 * different order value.  If so, resolve the previously aggregated properties
        	 * and clear out the aggregation before proceeding. 
        	 */
        	Integer previousOrder = contextData.isOrderChanging(getOrder());
        	if (previousOrder != null)
        	{
                log.info("Processing merged context properties of order " + previousOrder);
                processProperties(contextData.getProperties(), placeholderPrefix, placeholderSuffix);
                contextData.clearProperties();
        	}
        	
        	/*
        	 * Now we just merge these definitions into the current Order level aggregation.
        	 * We'll process them when we hit another placeholder configurer with a different
        	 * Order value, or when we hit the last placeholder configurer in the context.
        	 */
            CollectionUtils.mergePropertiesIntoMap(properties, contextData.getProperties());
            
            /*
             * Finally, if this is the last placeholder configurer in the context we need
             * to resolve the aggregated properties and call Spring to process these into
             * bean defintions, et al.
             */
            if (contextData.incrementPpcCount() == ppcCount)
            {
                log.info("Processing merged context properties of order " + getOrder());
                processProperties(contextData.getProperties(), placeholderPrefix, placeholderSuffix);
                contextData.clearProperties();
                log.info("Resolving context placeholders");
                super.processProperties(beanFactoryToProcess, contextData.getProperties());
            }
        }
    }

	/**
     * Resolves any placeholders in the supplied properties map, preferring the use of
     * previously set System properties over the current property map contents for
     * placeholder substitution.
     * <p>
     * Also sets any resolved properties as System properties, if no System
     * property by that name already exists.
     * 
     * @param properties the merged context properties
     */
    public static void processProperties(final Properties properties)
    {
    	processProperties(properties, DEFAULT_PLACEHOLDER_PREFIX, DEFAULT_PLACEHOLDER_SUFFIX);
    }

	/**
     * Resolves any placeholders in the supplied properties map, preferring the use of
     * previously set System properties over the current property map contents for
     * placeholder substitution.
     * <p>
     * Also sets any resolved properties as System properties, if no System
     * property by that name already exists.
     * 
     * @param properties the merged context properties
     */
    public static void processProperties(final Properties properties,
    		String placeholderPrefix, String placeholderSuffix)
    {
        PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper(
                placeholderPrefix, placeholderSuffix);
        PropertyPlaceholderHelper.PlaceholderResolver resolver = new PropertyPlaceholderHelper.PlaceholderResolver()
        {
            @Override
            public String resolvePlaceholder(String placeholderName)
            {
                // SYSTEM_PROPERTIES_MODE_OVERRIDE means we look at previously set
                // system properties in preference to properties defined in our file
                String value = System.getProperty(placeholderName);
                if (value == null)
                    value = properties.getProperty(placeholderName);
                return value;
            }
        };

        for (Object key : properties.keySet())
        {
            String propertyName = (String) key;
            // get the value from the map
            String propertyValue = properties.getProperty(propertyName);
            // resolve it against system properties then other properties in the
            // passed-in Properties
            String resolvedValue = helper.replacePlaceholders(propertyValue,
                    resolver);

            // set back into passed-in Properties if changed
            if (!ObjectUtils.nullSafeEquals(propertyValue, resolvedValue))
            {
                properties.setProperty(propertyName, resolvedValue);
            }

            // set into System properties if not already set
            setProperty(propertyName, resolvedValue);
        }
    }

    public static void setProperty(String propertyName, String propertyValue)
    {
        // set into System properties if not already set
        if (System.getProperty(propertyName) == null)
        {
            if (log.isInfoEnabled()) {
            	String logValue = propertyValue;
            	if (propertyName.toLowerCase().contains("password"))
            		logValue = OBFUSCATED_LOG_VALUE;
            	if (propertyName.toLowerCase().contains("secret"))
            		logValue = OBFUSCATED_LOG_VALUE;
                log.info("Setting system property: " + propertyName + "=" + logValue);
            }
            System.setProperty(propertyName, propertyValue);
        }
        else
        {
        	if (log.isDebugEnabled())
        		log.debug("Skipping system property: " + propertyName + " (already set)");
        }
    }
    
    /**
     * Programatically process a single property file location, using System
     * properties in preference to included definitions for placeholder
     * resolution, and setting any resolved properties as System properties, if
     * no property by that name already exists.
     * 
     * @param locationName String-encoded resource location for the properties file to process
     */
    public static void processLocation(String locationName)
    {
        processLocation(locationName, null, null);
    }

    /**
     * Programatically process a single property file location, using System
     * properties in preference to included definitions for placeholder
     * resolution, and setting any resolved properties as System properties, if
     * no property by that name already exists.
     * 
     * @param locationName String-encoded resource location for the properties file to process
     * @param placeholderPrefix the placeholder prefix String
     * @param placeholderSuffix the placeholder suffix String
     */
    public static void processLocation(String locationName,
            String placeholderPrefix, String placeholderSuffix)
    {
        ResourceEditor resourceEditor = new ResourceEditor();
        resourceEditor.setAsText(locationName);

        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setLocation((Resource) resourceEditor.getValue());
        if (placeholderPrefix != null)
            configurer.setPlaceholderPrefix(placeholderPrefix);
        if (placeholderSuffix != null)
            configurer.setPlaceholderSuffix(placeholderSuffix);

        GenericApplicationContext context = new GenericApplicationContext();
        context.getBeanFactory().registerSingleton("propertyPlaceholderConfigurer", configurer);
        context.refresh();
        context.close();
    }

    /**
     * Helper class to track aggregated property data and current Order level of the 
     * placeholder processing.
     *  
     * @author Brien Wheeler
     */
    private static class ContextData
    {
        private int ppcCount;
        private Integer currentOrder;
        private final Properties properties = new Properties();

        Properties getProperties()
        {
            return properties;
        }

        void clearProperties()
        {
        	properties.clear();
        }
        
        synchronized int incrementPpcCount()
        {
            return ++ppcCount;
        }
        
        Integer isOrderChanging(int nextOrder)
        {
        	Integer previousOrder = null;
        	if ((currentOrder == null) || (nextOrder != currentOrder.intValue()))
        	{
    			previousOrder = currentOrder;
    			currentOrder = nextOrder;
        	}
        	return previousOrder;
        }
    }
}
