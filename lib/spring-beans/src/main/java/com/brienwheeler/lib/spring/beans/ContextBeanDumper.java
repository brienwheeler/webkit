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

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.brienwheeler.lib.util.ValidationUtils;

/**
 * If this bean exists in a Spring context and DEBUG logging is enabled for it, it will
 * log an alphabetically sorted list of Spring bean names contained in the context.  
 *  
 * @author Brien Wheeler
 */
public class ContextBeanDumper implements BeanFactoryPostProcessor
{
    private static final Log log = LogFactory.getLog(ContextBeanDumper.class);

    /**
     * If DEBUG logging is not enabled, do nothing.  Otherwise log an alphabetically sorted list of Spring
     * bean names contained in the context.
     * 
     * @param beanFactory the beanFactory that is creating all the Spring context beans.
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
    	ValidationUtils.assertNotNull(beanFactory, "beanFactory cannot be null");
        if (log.isDebugEnabled())
        {
            StringBuffer buffer = new StringBuffer();
            String[] beanNames = beanFactory.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            buffer.append("Beans present in context:");
            for (String beanName : beanNames)
            {
                buffer.append("\n        ");
                buffer.append(beanName);
            }
            log.debug(buffer.toString());
        }
    }
}
