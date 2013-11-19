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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.SettingsFactory;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.connection.ConnectionProviderFactory;
import org.hibernate.ejb.connection.InjectedDataSourceConnectionProvider;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

import com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer;
import com.brienwheeler.lib.spring.beans.SmartClassPathXmlApplicationContext;

/**
 * A useful utility for dumping and/or executing database schema initialization or updates.
 * 
 * <p>By default, this tool reads the Spring beans file
 * <tt>classpath:com/brienwheeler/lib/db/appEntityManagerFactory.xml</tt>
 * looking for a bean named <tt>com.brienwheeler.lib.db.appEntityManagerFactory</tt>.
 * These values can be changed using the <tt>emfContextLocation</tt> and <tt>emfContextBeanName</tt>
 * properties.
 * 
 * <p>By default this tool only prints a set of SQL it believes is needed to update the
 * schema and performs no modifications agaisnt the database.  You may use the <tt>mode</tt>
 * and <tt>exec</tt> properties to change this behavior.  Setting Mode.CLEAN will instead generate
 * SQL that would initialize an empty database.
 * 
 * Setting exec true will cause the SQL to be executed against the database referred to by the
 * emfContextBean.  When mode == CLEAN and exec == true, database tables are dropped before being
 * re-instantiated.
 * 
 * @author Brien Wheeler
 */
public class SchemaToolBean implements InitializingBean, ApplicationListener<ContextRefreshedEvent>
{
    private static final Log log = LogFactory.getLog(SchemaToolBean.class);

    public static enum Mode 
    {
        UPDATE,
        CLEAN
    }
    
    private Mode mode = Mode.UPDATE;
    private boolean exec = false;
    private boolean closeContextOnDone = true;
    private String emfContextLocation = null;
    private String emfContextBeanName = null;
    private String emfPersistenceLocationsPropName = null;
    private String emfPersistenceLocationsPropValue = null;

    @Required
	public void setMode(Mode mode) {
		this.mode = mode;
	}

    @Required
	public void setExec(boolean exec) {
		this.exec = exec;
	}

    @Required
    public void setCloseContextOnDone(boolean closeContextOnDone) {
		this.closeContextOnDone = closeContextOnDone;
	}

	@Required
	public void setEmfContextLocation(String emfContextLocation) {
		this.emfContextLocation = emfContextLocation;
	}

    @Required
	public void setEmfContextBeanName(String emfContextBeanName) {
		this.emfContextBeanName = emfContextBeanName;
	}

    @Required
	public void setEmfPersistenceLocationsPropName(String emfPersistenceLocationsPropName)
	{
		this.emfPersistenceLocationsPropName = emfPersistenceLocationsPropName;
	}

    @Required
	public void setEmfPersistenceLocationsPropValue(String emfPersistenceLocationsPropValue)
	{
		this.emfPersistenceLocationsPropValue = emfPersistenceLocationsPropValue;
	}

	public void afterPropertiesSet() throws Exception
    {
		try {
	    	// set based on default values in schematool properties file if not already set
	    	PropertyPlaceholderConfigurer.setProperty(emfPersistenceLocationsPropName, emfPersistenceLocationsPropValue);
	    	
	        Configuration configuration = determineHibernateConfiguration();
	
	        Settings settings = null;
	        if (exec || mode == Mode.UPDATE) 
	        {
	            ClassPathXmlApplicationContext newContext = new SmartClassPathXmlApplicationContext(emfContextLocation);
	            try {
	            	// get a reference to the factory bean, don't have it create a new EntityManager
	                LocalContainerEntityManagerFactoryBean factoryBean = newContext.getBean("&" + emfContextBeanName, LocalContainerEntityManagerFactoryBean.class);
	                SettingsFactory settingsFactory = new InjectedDataSourceSettingsFactory(factoryBean.getDataSource());
	                settings = settingsFactory.buildSettings(new Properties());
	            }
	            finally {
	            	newContext.close();
	            }
	        }
	        
	        if (mode == Mode.UPDATE) 
	        {
	            SchemaUpdate update = new SchemaUpdate(configuration, settings);
	            update.execute(true, exec);
	        }
	        else
	        {
	            SchemaExport export = exec ? new SchemaExport(configuration, settings) :
	                    new SchemaExport(configuration);
	            export.create(true, exec);
	        }
		}
		catch (Exception e) {
			log.error("Error running SchemaTool", e);
			throw e;
		}
    }
	
    @Override
	public void onApplicationEvent(ContextRefreshedEvent event)
    {
    	if (closeContextOnDone)
    		((AbstractApplicationContext) event.getApplicationContext()).close();
	}

	private Configuration determineHibernateConfiguration() throws MappingException, ClassNotFoundException, IOException 
    {
        // Read in the bean definitions but don't go creating all the singletons,
        // because that causes creation of data sources and connections to database, etc.
        NonInitializingClassPathXmlApplicationContext context = new NonInitializingClassPathXmlApplicationContext(
                new String[] { emfContextLocation });
        try {
	        context.loadBeanDefinitions();
	        
	        // Get well-known EntityManagerFactory bean definition by name
	        BeanDefinition emfBeanDef = context.getBeanDefinition(emfContextBeanName);
	        if (emfBeanDef == null)
	        {
	            throw new RuntimeException("no bean defined: " + emfContextBeanName);
	        }
	
	        // Get the name of the persistence unit for this EntityManagerFactory
	        PropertyValue puNameProperty = emfBeanDef.getPropertyValues().getPropertyValue("persistenceUnitName");
	        if (puNameProperty == null || !(puNameProperty.getValue() instanceof TypedStringValue))
	        {
	            throw new RuntimeException("no property 'persistenceUnitName' defined on bean: " + emfContextBeanName);
	        }
	        String puName = ((TypedStringValue) puNameProperty.getValue()).getValue();
	
	        // Get the name of the persistence unit for this EntityManagerFactory
	        PropertyValue pumProperty = emfBeanDef.getPropertyValues().getPropertyValue("persistenceUnitManager");
	        PersistenceUnitManager pum = null;
	        if (pumProperty != null)
	        {
	            pum = createConfiguredPum(context, pumProperty);
	        }
	        else
	        {
	            pum = simulateDefaultPum(context, emfBeanDef);
	        }
	
	        // create the Hibernate configuration
	        PersistenceUnitInfo pui = pum.obtainPersistenceUnitInfo(puName);
	        Configuration configuration = new Configuration();
	        configuration.setProperties(pui.getProperties());
	        for (String className : pui.getManagedClassNames())
	        {
	            configuration.addAnnotatedClass(Class.forName(className));
	        }
	
	        return configuration;
        }
        finally {
        	context.close();
        }
    }
    
    private PersistenceUnitManager createConfiguredPum(NonInitializingClassPathXmlApplicationContext context,
            PropertyValue pumProperty)
    {
        if (pumProperty.getValue() instanceof BeanDefinitionHolder)
        {
            String beanName = ((BeanDefinitionHolder) pumProperty.getValue()).getBeanName();
            BeanDefinition beanDefinition = ((BeanDefinitionHolder) pumProperty.getValue()).getBeanDefinition();
            return (PersistenceUnitManager) context.createBean(beanName, beanDefinition);
        }
        else
        {
            throw new RuntimeException("property 'persistenceUnitManager' is not a BeanDefinition on bean: " + emfContextBeanName);
        }
    }
    
    @SuppressWarnings("unchecked")
    private PersistenceUnitManager simulateDefaultPum(NonInitializingClassPathXmlApplicationContext context,
            BeanDefinition emfBeanDef) 
    {
        // Simulate Spring's use of DefaultPersistenceUnitManager
        DefaultPersistenceUnitManager defpum = new DefaultPersistenceUnitManager();

        // Set the location of the persistence XML -- when using the DPUM,
        // you can only set one persistence XML location on the EntityManagerFactory
        PropertyValue locationProperty = emfBeanDef.getPropertyValues().getPropertyValue("persistenceXmlLocation");
        if (locationProperty == null || !(locationProperty.getValue() instanceof TypedStringValue))
        {
            throw new RuntimeException("no property 'persistenceXmlLocation' defined on bean: " + emfContextBeanName);
        }

        // Since PersistenceUnitPostProcessors may do things like set properties
        // onto the persistence unit, we need to instantiate them here so that
        // they get called when preparePersistenceUnitInfos() executes
        PropertyValue puiPostProcProperty = emfBeanDef.getPropertyValues().getPropertyValue("persistenceUnitPostProcessors");
        if (puiPostProcProperty != null && puiPostProcProperty.getValue() instanceof ManagedList)
        {
            List<PersistenceUnitPostProcessor> postProcessors = new ArrayList<PersistenceUnitPostProcessor>();
            for (BeanDefinitionHolder postProcBeanDef : (ManagedList<BeanDefinitionHolder>) puiPostProcProperty.getValue())
            {
                String beanName = postProcBeanDef.getBeanName();
                BeanDefinition beanDefinition = postProcBeanDef.getBeanDefinition();
                PersistenceUnitPostProcessor postProcessor = (PersistenceUnitPostProcessor) context.createBean(beanName, beanDefinition);
                postProcessors.add(postProcessor);
            }
            defpum.setPersistenceUnitPostProcessors(postProcessors.toArray(new PersistenceUnitPostProcessor[postProcessors.size()]));
        }

        defpum.setPersistenceXmlLocation(((TypedStringValue) locationProperty.getValue()).getValue());
        defpum.preparePersistenceUnitInfos();

        return defpum;
    }
    
    /**
     * A context that provides a loadBeanDefinitions method that performs only
     * the portions of the traditional refresh() required to define all the beans
     * and substitute any placeholders. 
     */
    private static class NonInitializingClassPathXmlApplicationContext extends
            ClassPathXmlApplicationContext
    {
        private ConfigurableListableBeanFactory beanFactory;

        public NonInitializingClassPathXmlApplicationContext(
                String[] configLocations) throws BeansException
        {
            super(configLocations, false, null);
        }

        public void loadBeanDefinitions() throws IOException
        {
            /*
             * This method mirrors the refresh() method up to and including
             * invoking the bean factory post processors, so that a complete set
             * of bean definitions with resolved placeholders exists.
             */

            // Prepare this context for refreshing.
            prepareRefresh();

            // Tell the subclass to refresh the internal bean factory.
            beanFactory = obtainFreshBeanFactory();

            // Prepare the bean factory for use in this context.
            prepareBeanFactory(beanFactory);

            // Allows post-processing of the bean factory in context subclasses.
            postProcessBeanFactory(beanFactory);

            // Invoke factory processors registered as beans in the context.
            invokeBeanFactoryPostProcessors(beanFactory);

			// Register bean processors that intercept bean creation.
			//registerBeanPostProcessors(beanFactory);

			// Initialize message source for this context.
			//initMessageSource();

			// Initialize event multicaster for this context, otherwise close() throws IllegalStateException
			initApplicationEventMulticaster();
			
			// Initialize other special beans in specific context subclasses.
			//onRefresh();

			// Check for listener beans and register them.
			//registerListeners();

			// Instantiate all remaining (non-lazy-init) singletons.
			//finishBeanFactoryInitialization(beanFactory);

			// Last step: publish corresponding event, otherwise close() throws IllegalStateException
			finishRefresh();
        }

        public BeanDefinition getBeanDefinition(String beanName)
        {
            return beanFactory.getBeanDefinition(beanName);
        }

        @Override
        protected DefaultListableBeanFactory createBeanFactory()
        {
            return new BeanDefinitionCreatingFactory(getInternalParentBeanFactory());
        }
        
        public Object createBean(String beanName, BeanDefinition beanDefinition)
        {
            return ((BeanDefinitionCreatingFactory) beanFactory).createBean(beanName, beanDefinition);
        }
    }
    
    /**
     * A BeanFactory that exposes a method that creates from the BeanDefinition 
     */
    private static class BeanDefinitionCreatingFactory extends DefaultListableBeanFactory
    {
        public BeanDefinitionCreatingFactory(BeanFactory parentBeanFactory)
        {
            super(parentBeanFactory);
        }
        
        public Object createBean(String beanName, BeanDefinition beanDefinition)
        {
            RootBeanDefinition rootBeanDefinition = getMergedBeanDefinition(beanName, beanDefinition);
            return createBean(beanName, rootBeanDefinition, null);
        }
    }
    
    /**
     * A helper class to create a Hibernate Settings object that uses an
     * InjectedDataSourceConnectionProvider.
     */
    private static class InjectedDataSourceSettingsFactory extends SettingsFactory
    {
        private static final long serialVersionUID = 1L;
        
        private DataSource dataSource;

        public InjectedDataSourceSettingsFactory(DataSource dataSource)
        {
            super();
            this.dataSource = dataSource;
        }

        @Override
        protected ConnectionProvider createConnectionProvider(Properties properties)
        {
            properties.setProperty(Environment.CONNECTION_PROVIDER,
                    InjectedDataSourceConnectionProvider.class.getCanonicalName());
            Map<String,Object> injectionData = new HashMap<String,Object>();
            injectionData.put("dataSource", dataSource);
            return ConnectionProviderFactory.newConnectionProvider(properties, injectionData);
        }
    }
}
