package com.brienwheeler.lib.jmx;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class AutoRegisterByAnnotation extends AutoRegisterMBeanBase
{
	@ManagedOperation
	public String testOperation()
	{
		return "success";
	}
}
