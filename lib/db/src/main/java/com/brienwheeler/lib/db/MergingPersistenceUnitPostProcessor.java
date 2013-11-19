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
package com.brienwheeler.lib.db;

import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

import com.brienwheeler.lib.util.ValidationUtils;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A PersistenceUnitPostProcessor that merges together classes defined for the persistence
 * unit that are scattered across multiple persistence unit XML files.  Also supports
 * setting properties onto the persistence unit, so that these properties can be defined
 * in Spring's XML files and be subject to placeholder substitution.
 * 
 * @author Brien Wheeler
 */
public class MergingPersistenceUnitPostProcessor implements
        PersistenceUnitPostProcessor
{
    private final Map<String, Set<String>> puiClasses = new HashMap<String, Set<String>>();
    private Properties properties;

    /**
     * Post-process the persistence unit information.  If we have seen this persistence
     * unit name before, merge any newly-defined classes in.  Also, if any properties
     * are set on this post-processor, set them onto the target PersistenceUnitInfo
     */
    @Override
    public synchronized void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui)
    {
    	ValidationUtils.assertNotNull(pui, "persistenceUnitInfo cannot be null");
        Set<String> classes = puiClasses.get(pui.getPersistenceUnitName());
        if (classes == null)
        {
            classes = new LinkedHashSet<String>();
            puiClasses.put(pui.getPersistenceUnitName(), classes);
        }
        pui.getManagedClassNames().addAll(classes);
        classes.addAll(pui.getManagedClassNames());

        if (properties != null)
            pui.setProperties(properties);
    }

    /**
     * Define a set of Properties that should be applied to every PersistenceUnitInfo
     * processed.
     * @param properties the Properties to set on each PersistenceUnitInfo
     */
    public void setProperties(Properties properties)
    {
    	ValidationUtils.assertNotNull(properties, "properties cannot be null");
        this.properties = properties;
    }
}
