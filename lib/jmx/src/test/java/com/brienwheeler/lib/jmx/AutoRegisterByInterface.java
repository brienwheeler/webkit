package com.brienwheeler.lib.jmx;

public class AutoRegisterByInterface extends AutoRegisterMBeanBase implements AutoRegisterByInterfaceMBean
{
	public String testOperation()
	{
		return "success";
	}
}
