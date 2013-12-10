package com.brienwheeler.lib.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;

public class ListenerSet<ListenerInterfaceType>
{
	private final CopyOnWriteArrayList<ListenerInterfaceType> listeners = new CopyOnWriteArrayList<ListenerInterfaceType>();
	private final ConcurrentHashMap<CacheKey, Method> methodCache = new ConcurrentHashMap<ListenerSet.CacheKey, Method>();
	private final Log log;
	
	private static final Method NEGATIVE_CACHE_ENTRY;
	static
	{
		try {
			NEGATIVE_CACHE_ENTRY = ListenerSet.class.getMethod("hashCode");
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e); // can't happen, make compiler happy  :)
		}
	}
			
	public ListenerSet(Log log)
	{
		this.log = log;
	}
	
	public void setListeners(Collection<ListenerInterfaceType> listeners)
	{
		this.listeners.retainAll(listeners);
		this.listeners.addAllAbsent(listeners);
	}
	
	public void addListener(ListenerInterfaceType listener)
	{
		listeners.add(listener);
	}
	
	public void removeListener(ListenerInterfaceType listener)
	{
		listeners.remove(listener);
	}

	public void callListenerMethod(String methodName, Object... arguments)
	{
		Iterator<ListenerInterfaceType> listeners_it = this.listeners.iterator();

		if (!listeners_it.hasNext())
			return;
		
		ListenerInterfaceType listener = listeners_it.next();
		Method method = findMethod(listener.getClass(), methodName, arguments);
		do {
			try {
				method.invoke(listener, arguments);
			}
			catch (InvocationTargetException e) {
				log.error("error invoking listener method " + methodName, e);
			}
			catch (IllegalAccessException e) {
				log.error("error invoking listener method " + methodName, e);
			}
			listener = listeners_it.hasNext() ? listeners_it.next() : null;
		} while (listener != null);
	}
	
	private Method findMethod(Class<?> listenerClass, String methodName, Object... arguments)
	{
		CacheKey cacheKey = new CacheKey(methodName, arguments);
		Method cachedMethod = methodCache.get(cacheKey);
		if (cachedMethod == NEGATIVE_CACHE_ENTRY)
			throw new IllegalArgumentException("no matching method found for " + methodName);
		if (cachedMethod != null)
			return cachedMethod;
		
		Method found = null;
		Method[] methods = listenerClass.getMethods();
		for (int i=0; found==null && i<methods.length; i++) {
			Method candidate = methods[i];
			
			if (!candidate.getName().equals(methodName))
				continue;

			Class<?>[] parameterTypes = candidate.getParameterTypes();
			if (parameterTypes.length != arguments.length)
				continue;
			
			boolean matched = true;
			for (int j=0; matched && j<parameterTypes.length; j++) {
				if (parameterTypes[j] == Boolean.TYPE && arguments[j].getClass() == Boolean.class)
					continue;
				if (parameterTypes[j] == Double.TYPE && arguments[j].getClass() == Double.class)
					continue;
				if (parameterTypes[j] == Float.TYPE && arguments[j].getClass() == Float.class)
					continue;
				if (parameterTypes[j] == Integer.TYPE && arguments[j].getClass() == Integer.class)
					continue;
				if (parameterTypes[j] == Long.TYPE && arguments[j].getClass() == Long.class)
					continue;
				if (!parameterTypes[j].isAssignableFrom(arguments[j].getClass()))
					matched = false;
			}
			if (!matched)
				continue;
			
			found = candidate;
		}
		
		if (found == null)
			methodCache.put(cacheKey, NEGATIVE_CACHE_ENTRY);
		else
			methodCache.put(cacheKey, found);
		
		if (found == null)
			throw new IllegalArgumentException("no matching method found for " + methodName);
		return found;
	}
	
	private static class CacheKey
	{
		private final String methodName;
		private final Class<?>[] argumentClasses;
		
		CacheKey(String methodName, Object[] arguments) {
			ValidationUtils.assertNotNull(methodName, "methodName cannot be null");
			ValidationUtils.assertNotNull(arguments, "arguments cannot be null");
			
			this.methodName = methodName;
			this.argumentClasses = new Class<?>[arguments.length];
			for (int i=0; i<arguments.length; i++) {
				argumentClasses[i] = arguments[i].getClass();
			}
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			CacheKey key2 = (CacheKey) obj;
			if (!methodName.equals(key2.methodName))
				return false;
			return Arrays.equals(argumentClasses, key2.argumentClasses);
		}

		@Override
		public int hashCode()
		{
			int hashCode = methodName.hashCode();
			for (int i=0; i<argumentClasses.length; i++)
				hashCode = hashCode * 31 + argumentClasses[i].getCanonicalName().hashCode();
			return hashCode;
		}
	}
}
