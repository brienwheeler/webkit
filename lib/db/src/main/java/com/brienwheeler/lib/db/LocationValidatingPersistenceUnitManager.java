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
package com.brienwheeler.lib.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;

import com.brienwheeler.lib.util.ArrayUtils;
import com.brienwheeler.lib.util.ValidationUtils;

/**
 * A PersistenceUnitManager that can be given a list of resources that may or may not be
 * valid.  Each resource will be checked for existence before being passed to the
 * superclass DefaultPersistenceUnitManager.
 * 
 * <p>The list may be provided directly to {@link #setPersistenceXmlLocations(String[])} or
 * as a comma-separated list to {@link #setCommaSeparatedXmlLocations(String)}
 * 
 * @author Brien Wheeler
 */
public class LocationValidatingPersistenceUnitManager extends DefaultPersistenceUnitManager
{
    private static final Log log = LogFactory.getLog(LocationValidatingPersistenceUnitManager.class);

    /**
     * Convenience method for specifying list of potential locations as a comma-separated
     * list.  The provided String is split and each element is trimmed before the array
     * of resulting Strings are passed to {@link #setPersistenceXmlLocations(String[])}.
     * 
     * @param locations the comma-separated list of potential persistence XML file locations.
     */
    public void setCommaSeparatedXmlLocations(String locations)
    {
    	ValidationUtils.assertNotNull(locations, "locations cannot be null");
        setPersistenceXmlLocations(ArrayUtils.trimElements(locations.split(",")));
    }

    /**
     * Accept a list of locations for persistence XML files.  Before passing this list
     * to our superclass DefaultPersistenceUnitManager, check each location on the list
     * for actual existence.
     */
	@Override
	public void setPersistenceXmlLocations(String... persistenceXmlLocations)
	{
    	ValidationUtils.assertNotNull(persistenceXmlLocations, "persistenceXmlLocations cannot be null");
        List<String> goodLocations = new ArrayList<String>();

        ResourceEditor editor = new ResourceEditor();
        for (String location : persistenceXmlLocations)
        {
            if (location == null || location.isEmpty())
                continue;

            editor.setAsText(location);
            Resource resource = (Resource) editor.getValue();
            if (resource != null && resource.exists())
            {
                log.debug("using valid persistence XML location: " + location);
                goodLocations.add(location);
            }
            else
            {
                log.warn("ignoring invalid persistence XML location: " + location);
            }
        }

        String[] valid = goodLocations.toArray(new String[goodLocations.size()]);
        log.info("using validated persistence locations: [" + ArrayUtils.toString(valid) + "]");
        super.setPersistenceXmlLocations(valid);
	}

	@Override
	protected boolean isPersistenceUnitOverrideAllowed()
	{
		return true;
	}
}
