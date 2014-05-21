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
package com.brienwheeler.svc.monitor.telemetry.impl;

import com.brienwheeler.svc.monitor.telemetry.ITelemetryNameFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TelemetryNameFilter implements ITelemetryNameFilter
{
	public static final String FILTER_SEPARATOR = ",";
	public static final String FIELD_SEPARATOR = ":";

	protected final Log log = LogFactory.getLog(getClass());
	
	private static enum Action {
		INCLUDE,
		EXCLUDE,
	}

	private final CopyOnWriteArrayList<FilterRecord> filterRecords = 
			new CopyOnWriteArrayList<FilterRecord>();
	
	@Required
	public void setFilterRecords(String filterString)
	{
		if ((filterString == null) || filterString.trim().isEmpty()) {
			this.filterRecords.clear();
			return;
		}
		
		ArrayList<FilterRecord> filterRecords = new ArrayList<FilterRecord>();
		
		String[] recordStrings = filterString.split(FILTER_SEPARATOR);
		for (String recordString : recordStrings) {
			recordString = recordString.trim();
			String recordFields[] = recordString.split(FIELD_SEPARATOR);
			if (recordFields.length != 2) {
				log.error("invalid filter record string (skipping) :" + recordString);
				continue;
			}
			
			Action filterAction;
			try {
				filterAction = Action.valueOf(recordFields[0].trim());
			}
			catch (IllegalArgumentException e) {
				log.error("invalid filter record action (skipping) :" + recordFields[0].trim());
				continue;
			}

			try {
				filterRecords.add(new FilterRecord(filterAction, recordFields[1].trim()));
			}
			catch (PatternSyntaxException e)
			{
				log.error("invalid filter record pattern (skipping) :" + recordFields[1].trim());
				continue;
			}
		}
		
		this.filterRecords.clear();
		this.filterRecords.addAll(filterRecords);
	}

    @Override
    public boolean process(String name)
    {
        boolean process = checkFilters(name);
        if (log.isDebugEnabled()) {
            log.debug((process ? "in" : "ex") + "cluding " + name);
        }
        return process;
    }

    private boolean checkFilters(String name)
    {
        for (FilterRecord filterRecord : filterRecords) {
            Action action = filterRecord.filterMatches(name);
            if (action != null) {
                return action == Action.INCLUDE;
            }
        }
        return true; // default is to pass the info along
    }

	private class FilterRecord
	{
		private final Action action;
		private final Pattern pattern;
		
		FilterRecord(Action action, String regex)
		{
			this.action = action;
			this.pattern = Pattern.compile(regex);
		}
		
		Action filterMatches(String name)
		{
			if (pattern.matcher(name).matches())
				return action;
			return null;
		}
	}
}
