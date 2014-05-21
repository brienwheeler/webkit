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
package com.brienwheeler.lib.svc.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.brienwheeler.lib.monitor.work.IWorkMonitorProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.brienwheeler.lib.monitor.intervene.IInterventionListener;
import com.brienwheeler.lib.monitor.telemetry.ITelemetryPublishService;
import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;
import com.brienwheeler.lib.monitor.work.IWorkPublishService;
import com.brienwheeler.lib.monitor.work.WorkMonitor;
import com.brienwheeler.lib.svc.IStartableService;
import com.brienwheeler.lib.svc.IStoppableService;
import com.brienwheeler.lib.svc.ServiceOperationException;
import com.brienwheeler.lib.svc.ServiceState;
import com.brienwheeler.lib.svc.ServiceStateException;
import com.brienwheeler.lib.util.ArrayUtils;
import com.brienwheeler.lib.util.ObjectUtils;
import com.brienwheeler.lib.util.ValidationUtils;

public abstract class StartableServiceBase implements IStartableService, IWorkMonitorProvider
{
	protected final Log log = LogFactory.getLog(getClass());
	
	protected final AtomicReference<ServiceState> state = new AtomicReference<ServiceState>(ServiceState.STOPPED);
	protected final ArrayList<IStartableService> subServices = new ArrayList<IStartableService>();
	protected final Set<Thread> serviceThreads = new HashSet<Thread>();
	protected final String logId = ObjectUtils.getUniqueId(this);
	protected final WorkMonitor workMonitor = new WorkMonitor(getClass().getName());
	protected final CopyOnWriteArraySet<IInterventionListener> interventionListeners =
			new CopyOnWriteArraySet<IInterventionListener>();
	protected final CopyOnWriteArraySet<ITelemetryPublishService> telemetryPublishers =
			new CopyOnWriteArraySet<ITelemetryPublishService>();
	protected final CopyOnWriteArraySet<IWorkPublishService> workPublishers =
			new CopyOnWriteArraySet<IWorkPublishService>();

	private final AtomicInteger refCount = new AtomicInteger(0);
	private boolean autoStartSubServices = true;

	@Override
	public void start()
	{
		synchronized (state)
		{
			boolean done = false;
			while (!done)
			{
				switch (state.get())
				{
					case STOPPED :
						changeState(ServiceState.STARTING);
						done = true;
						break;
						
					case STARTING :
						if (waitForStateChangeEx() != ServiceState.RUNNING)
							throw new ServiceOperationException("start operation in other thread failed");
						break;
							
					case STOPPING :
						waitForStateChangeEx();
						break;
						
					case STOP_FAILED :
						throw new ServiceOperationException("previous stop operation failed");
						
					case RUNNING :
						incrementRefCount();
						return;
				}
			}
		}
		
		try
		{
			registerWithWorkPublishers();
			
			if (autoStartSubServices)
				autoStartSubServices();
			
			onStart();
			
			synchronized (state)
			{
				changeState(ServiceState.RUNNING);
				incrementRefCount();
			}
		}
		catch (InterruptedException e)
		{
			cleanPartialStart();
			Thread.currentThread().interrupt();
			throw new ServiceOperationException("start operation thread interrupted", e);
		}
		catch (RuntimeException e)
		{
			cleanPartialStart();
			throw e;
		}
		catch (Error e)
		{
			cleanPartialStart();
			throw e;
		}
	}

	public ServiceState getState()
	{
		return state.get();
	}
	
	public void setAutoStartSubServices(boolean autoStartSubServices)
	{
		this.autoStartSubServices = autoStartSubServices;
	}

	public synchronized void setInterventionListeners(Collection<IInterventionListener> interventionListeners)
	{
		this.interventionListeners.retainAll(interventionListeners);
		this.interventionListeners.addAll(interventionListeners);
	}

	public synchronized void setTelemetryPublishers(Collection<ITelemetryPublishService> telemetryPublishers)
	{
		this.telemetryPublishers.retainAll(telemetryPublishers);
		this.telemetryPublishers.addAll(telemetryPublishers);
	}

	public synchronized void setWorkPublishers(Collection<IWorkPublishService> workPublishers)
	{
		IWorkPublishService oldWorkPublishers[] = this.workPublishers.toArray(new IWorkPublishService[this.workPublishers.size()]);
		this.workPublishers.retainAll(workPublishers);
		this.workPublishers.addAll(workPublishers);

		// do incremental register / deregister if RUNNING
		if (getState() == ServiceState.RUNNING) {
			// register new work publishers -- in new set but not old set
			for (IWorkPublishService workPublisher : this.workPublishers)
				if (!ArrayUtils.contains(oldWorkPublishers, workPublisher))
					workPublisher.registerWorkMonitor(workMonitor);
			
			// deregister old work publishers -- in old set but not new set
			for (IWorkPublishService workPublisher : oldWorkPublishers)
				if (!this.workPublishers.contains(workPublisher))
					workPublisher.deregisterWorkMonitor(workMonitor);
		}
	}

    @Override
    public WorkMonitor getWorkMonitor()
    {
        return workMonitor;
    }

    protected void onStart() throws InterruptedException
	{
	}

	protected void changeState(ServiceState newState)
	{
		ValidationUtils.assertNotNull(newState, "newState cannot be null");
		synchronized (state)
		{
			state.set(newState);
			state.notifyAll();
			if (log.isInfoEnabled())
				log.info(logId + ": changing state to " + newState.toString());
		}
	}
	
	protected void ensureState(ServiceState expected, String message)
	{
		ServiceState current = state.get();
		if (current != expected)
			throw new ServiceStateException(message + " [" + current.toString() + "]");
	}
	
	protected ServiceState waitForStateChange() throws InterruptedException
	{
		synchronized (state)
		{
			ServiceState startState = state.get();
			
			ServiceState newState = state.get();
			while (newState == startState)
			{
				state.wait();
				newState = state.get();
			}
			return newState;
		}
	}

	protected ServiceState waitForStateChangeEx()
	{
		try {
			return waitForStateChange();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ServiceOperationException("service operation thread interrupted", e);
		}
	}
	
	protected int incrementRefCount()
	{
		synchronized (state) {
			int newCount = refCount.incrementAndGet();
			if (log.isDebugEnabled())
				log.debug(logId + ": refCount incremented to " + newCount);
			return newCount;
		}
	}
	
	protected int decrementRefCount()
	{
		synchronized (state) {
			int newCount = refCount.decrementAndGet();
			if (log.isDebugEnabled())
				log.debug(logId + ": refCount decremented to " + newCount);
			if (newCount < 0)
			{
				// over-closing?  Never let the reference count get below zero, it would prevent
				// a restart
				refCount.set(0);
				throw new IllegalStateException("reference count decremented below zero");
			}
			return newCount;
		}
	}

	protected void startSubService(IStartableService subService) throws InterruptedException
	{
		ValidationUtils.assertNotNull(subService, "subService cannot be null");
		subService.start();
		synchronized (subServices) {
			subServices.add(subService);
			if (log.isDebugEnabled())
				log.debug(logId + ": added subservice " + ObjectUtils.getUniqueId(subService));
		}
	}
	
	private void cleanPartialStart()
	{
		changeState(ServiceState.STOPPING);
		try
		{
			deregisterWithWorkPublishers();
			shutdownSubServices();
		}
		finally
		{
			changeState(ServiceState.STOPPED);
		}
	}
	
	protected void shutdownSubServices()
	{
		IStartableService[] copy;
		synchronized (subServices) {
			copy = subServices.toArray(new IStartableService[subServices.size()]);
			subServices.clear();
		}

		Throwable error = null;
		
		// shutdown in reverse order of startup, just in case there are sibling dependencies
		// keep track of first RuntimeException or Error (if any thrown) and re-throw after
		// iterating across all subServices -- a problem shutting one down should not preclude
		// attempts to shut down all others 
		for (int i=copy.length-1; i>=0; i--)
		{
			try
			{
				if (copy[i] instanceof IStoppableService)
					((IStoppableService) copy[i]).stopImmediate();
			}
			catch (RuntimeException e) {
				if (error == null)
					error = e;
			}
			catch (Error e) {
				if (error == null)
					error = e;
			}
		}
		
		// throw error experienced during sub service shutdowns (if any)
		if (error != null && error instanceof RuntimeException)
			throw (RuntimeException) error;
		if (error != null && error instanceof Error)
			throw (Error) error;
	}
	
	protected <T> T executeWithGracefulShutdown(ServiceWork<T> work)
	{
		ValidationUtils.assertNotNull(work, "work cannot be null");
		synchronized (state) {
			ensureState(ServiceState.RUNNING, "refusing work");
			serviceThreads.add(Thread.currentThread());
		}
		
		try {
			return work.doServiceWork();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ServiceStateException("service operation interrupted", e);
		}
		finally {
			synchronized (state) {
				serviceThreads.remove(Thread.currentThread());
				state.notifyAll();
			}
		}
	}

	protected void recordInterventionRequest(String message)
	{
		log.error(message);
		for (IInterventionListener interventionListener : interventionListeners)
			interventionListener.recordInterventionRequest(this, log, message);
	}
	
	protected void publishTelemetry(TelemetryInfo telemetryInfo)
	{
		telemetryInfo.publish();
		for (ITelemetryPublishService telemetryPublisher : telemetryPublishers)
			telemetryPublisher.publish(telemetryInfo);
	}
	
	protected void registerWithWorkPublishers()
	{
		for (IWorkPublishService workPublisher : workPublishers)
			workPublisher.registerWorkMonitor(workMonitor);
	}
	
	protected void deregisterWithWorkPublishers()
	{
		for (IWorkPublishService workPublisher : workPublishers)
			workPublisher.deregisterWorkMonitor(workMonitor);
	}

	private void autoStartSubServices() throws InterruptedException
	{
		autoStartSubServices(getClass());
	}
	
	private void autoStartSubServices(Class<? extends IStartableService> clazz) throws InterruptedException
	{
		// do any superclass services first
		if (IStartableService.class.isAssignableFrom(clazz.getSuperclass()))
			autoStartSubServices(clazz.getSuperclass().asSubclass(IStartableService.class));
		
		for (Field field : clazz.getDeclaredFields())
		{
			if (IStartableService.class.isAssignableFrom(field.getType()))
			{
				boolean setInaccessible = false;
				if (!field.isAccessible())
				{
					field.setAccessible(true);
					setInaccessible = true;
				}
				try {
					Object value = field.get(this);
					if (value != null)
						startSubService((IStartableService) value);
				}
				catch (IllegalAccessException e) {
					throw new RuntimeException("error accessing IStartableService field " + field.getName(), e);
				}
				finally {
					if (setInaccessible)
						field.setAccessible(false);
				}
			}
		}
	}

}
