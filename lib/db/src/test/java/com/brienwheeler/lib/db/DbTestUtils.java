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

import java.util.concurrent.Callable;

import javax.persistence.EntityManagerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class DbTestUtils
{
	private DbTestUtils() {}
	
	public static <T> T doInHibernateSession(ApplicationContext applicationContext, Callable<T> work)
	{
		EntityManagerFactory entityManagerFactory = applicationContext.getBean("com.brienwheeler.lib.db.appEntityManagerFactory",
				EntityManagerFactory.class);
		
		EntityManagerHolder entityManagerHolder = (EntityManagerHolder)
				TransactionSynchronizationManager.getResource(entityManagerFactory);

		boolean created = entityManagerHolder == null;
		if (created) {
			entityManagerHolder = new EntityManagerHolder(entityManagerFactory.createEntityManager());
			TransactionSynchronizationManager.bindResource(entityManagerFactory, entityManagerHolder);
		}
		
		try {
			return work.call();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (created)
				TransactionSynchronizationManager.unbindResource(entityManagerFactory);
		}
	}
}
