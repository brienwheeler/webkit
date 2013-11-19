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
package com.brienwheeler.lib.monitor.telemetry;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import com.brienwheeler.lib.util.ValidationUtils;

public class TelemetryInfo
{
	public static final String ATTR_NAME = "name";
	public static final String ATTR_CREATED_AT = "createdAt";
	
	protected static enum State { UNPUBLISHED, PUBLISHED };
	
	protected final ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();
	protected final ConcurrentHashMap<String, Object> processedVersions = new ConcurrentHashMap<String, Object>();
	protected final Log log;
	protected State state = State.UNPUBLISHED;
	protected long lastTimestamp;
	
	public TelemetryInfo(String name)
	{
		this(name, null);
	}
	
	public TelemetryInfo(String name, Log log)
	{
		ValidationUtils.assertNotNull(name,  "name cannot be null");
		
		attributes.put(ATTR_NAME, name);
		this.log = log;
		this.lastTimestamp = System.currentTimeMillis();
		attributes.put(ATTR_CREATED_AT, lastTimestamp);
	}
	
	public TelemetryInfo(String name, Log log, long createdAt)
	{
		this(name, log);
		attributes.put(ATTR_CREATED_AT, createdAt);
	}
	
	public String getName()
	{
		return (String) attributes.get(ATTR_NAME);
	}
	
	public long getCreatedAt()
	{
		return ((Long) attributes.get(ATTR_CREATED_AT)).longValue();
	}
	
	public Log getLog()
	{
		return log;
	}
	
	public Collection<String> getAttributeNames()
	{
		return attributes.keySet();
	}
	
	public void set(String attrName, Object attrValue)
	{
		validateModify(attrName);
		ValidationUtils.assertNotNull(attrValue,  "attrValue cannot be null");
		
		attributes.put(attrName, attrValue);
	}
	
	public Object get(String attrName)
	{
		ValidationUtils.assertNotNull(attrName,  "attrName cannot be null");
		
		return attributes.get(attrName);
	}
	
	public void clear(String attrName)
	{
		validateModify(attrName);
		
		attributes.remove(attrName);
	}

	public synchronized void markDelta(String attrName)
	{
		validateModify(attrName);
		
		long newTimestamp = System.currentTimeMillis();
		attributes.put(attrName, newTimestamp - lastTimestamp);
		lastTimestamp = newTimestamp;
	}
	
	public synchronized void publish()
	{
		state = State.PUBLISHED;
	}
	
	public synchronized void checkPublished()
	{
		ValidationUtils.assertTrue(state == State.PUBLISHED, getClass().getSimpleName() + " not yet published");
	}
	
	public Object getProcessedVersion(String versionName)
	{
		ValidationUtils.assertNotNull(versionName,  "versionName cannot be null");
		return processedVersions.get(versionName);
	}

	public void setProcessedVersion(String versionName, Object versionValue)
	{
		ValidationUtils.assertNotNull(versionName,  "versionName cannot be null");
		ValidationUtils.assertNotNull(versionValue,  "versionValue cannot be null");
		processedVersions.put(versionName, versionValue);
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer(1024);
		buf.append(getClass().getSimpleName()).append(":[");
		buf.append(ATTR_NAME).append("=").append(getName());
		buf.append(",").append(ATTR_CREATED_AT).append("=").append(get(ATTR_CREATED_AT));
		for (String attrName : attributes.keySet()) {
			if (attrName.equals(ATTR_NAME) || attrName.equals(ATTR_CREATED_AT))
				continue;
			buf.append(",").append(attrName).append("=").append(get(attrName));
		}
		buf.append("]");
		return buf.toString();
	}

	private void validateModify(String attrName)
	{
		synchronized (this) {
			ValidationUtils.assertTrue(state != State.PUBLISHED, "can't modify a published " + getClass().getSimpleName());
		}
		ValidationUtils.assertNotNull(attrName,  "attrName cannot be null");
		ValidationUtils.assertFalse(attrName.equals(ATTR_NAME), "can't modify attr '" + ATTR_NAME + "'");
		ValidationUtils.assertFalse(attrName.equals(ATTR_CREATED_AT), "can't modify attr '" + ATTR_CREATED_AT + "'");
	}
}
