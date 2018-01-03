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
package com.brienwheeler.lib.spring.beans;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.support.EncodedResource;

/**
 * A subclass of XmlBeanDefinitionReader that does not re-read the same XML file twice.
 * Useful for preventing bean redefinition (and potential replication of anonymous beans)
 * when importing a complex tree of bean definition imports that might have duplicate
 * subtrees.  Note this does not subvert the standard cyclic import detection. 
 *  
 * @author bwheeler
 */
public class SmartXmlBeanDefinitionReader extends XmlBeanDefinitionReader {

	protected final Set<String> loadedPaths = new ConcurrentSkipListSet<String>();
	
	public SmartXmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
		super(registry);
	}

	@Override
	public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
		String canonicalPath = null;
		try {
			canonicalPath = encodedResource.getResource().getURL().getPath();
		}
		catch (IOException e) {
			// on FileNotFoundException, fall through to normal behavior -- this will probably result in a
			// FNFE from somewhere else that will make more sense.
			if (!(e instanceof FileNotFoundException))
				throw new BeanDefinitionStoreException("Error resolving canonical path for context resource", e);
		}

		if (canonicalPath != null && loadedPaths.contains(canonicalPath)) {
			if (logger.isDebugEnabled())
				logger.debug("skipping already loaded path " + canonicalPath);
			return 0;
		}
		else {
			int count = super.loadBeanDefinitions(encodedResource);
			if (canonicalPath != null)
				loadedPaths.add(canonicalPath);
			return count;
		}
				
	}
}
