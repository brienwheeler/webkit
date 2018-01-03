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
package com.brienwheeler.lib.monitor.telemetry.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.brienwheeler.lib.monitor.telemetry.TelemetryInfo;

public class TelemetryInfoJsonSerializer extends TelemetryInfoProcessorBase
{
	public static final String VERSION_NAME = "json";
	
	public static final Log log = LogFactory.getLog(TelemetryInfoJsonSerializer.class);
	
	@Override
	protected void onProcess(TelemetryInfo telemetryInfo)
	{
		// already done?
		if (telemetryInfo.getProcessedVersion(VERSION_NAME) != null)
			return;

		try {
			JSONObject object = new JSONObject();
			// first do well-known attributes
			object.put(TelemetryInfo.ATTR_NAME, telemetryInfo.get(TelemetryInfo.ATTR_NAME));
			object.put(TelemetryInfo.ATTR_CREATED_AT, telemetryInfo.get(TelemetryInfo.ATTR_CREATED_AT));
			
			for (String attrName : telemetryInfo.getAttributeNames()) {
				// skip well-known attributes, already done
				if (attrName.equals(TelemetryInfo.ATTR_NAME) ||
						attrName.equals(TelemetryInfo.ATTR_CREATED_AT))
					continue;
				
				// BDFIL
				Object value = telemetryInfo.get(attrName);
				if (value instanceof Boolean)
					object.put(attrName, ((Boolean) value).booleanValue());
				else if (value instanceof Double)
					object.put(attrName, ((Double) value).doubleValue());
				else if (value instanceof Float)
					object.put(attrName, ((Float) value).floatValue());
				else if (value instanceof Integer)
					object.put(attrName, ((Integer) value).intValue());
				else if (value instanceof Long)
					object.put(attrName, ((Long) value).longValue());
				else
					object.put(attrName, telemetryInfo.get(attrName).toString());
			}
			
			telemetryInfo.setProcessedVersion(VERSION_NAME, object.toString());
		}
		catch (JSONException e) {
			log.error(e);
		}
	}
}
