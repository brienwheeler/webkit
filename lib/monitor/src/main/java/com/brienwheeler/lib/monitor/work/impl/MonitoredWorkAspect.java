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
package com.brienwheeler.lib.monitor.work.impl;

import com.brienwheeler.lib.monitor.work.IWorkMonitorProvider;
import com.brienwheeler.lib.monitor.work.MonitoredWork;
import com.brienwheeler.lib.monitor.work.WorkMonitor;
import com.brienwheeler.lib.util.ValidationUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class MonitoredWorkAspect
{
	@Pointcut("execution(@com.brienwheeler.lib.monitor.work.MonitoredWork * *(..))")
	public void monitoredWorkPointcut() {}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Around("monitoredWorkPointcut()")
	public Object aroundMonitoredWork(final ProceedingJoinPoint joinPoint) throws InterruptedException
	{
		ValidationUtils.assertTrue(joinPoint.getTarget() instanceof IWorkMonitorProvider,
				"@MonitoredWork target must be subclass of IWorkMonitorProvider");
		ValidationUtils.assertTrue(joinPoint.getSignature() instanceof MethodSignature,
				"@MonitoredWork signature must be a method");
		
        final WorkMonitor workMonitor = ((IWorkMonitorProvider) joinPoint.getTarget()).getWorkMonitor();
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		MonitoredWork annotation = signature.getMethod().getAnnotation(MonitoredWork.class);
		
		final String workName = annotation != null && !annotation.value().isEmpty() ? annotation.value() : signature.getName();

        long start = System.currentTimeMillis();
        try {
            Object ret = joinPoint.proceed();
            workMonitor.recordWorkOk(workName, System.currentTimeMillis() - start);
            return ret;
        }
        catch (InterruptedException e) {
            workMonitor.recordWorkError(workName, System.currentTimeMillis() - start);
            Thread.currentThread().interrupt();
            throw e;
        }
        catch (RuntimeException e) {
            workMonitor.recordWorkError(workName, System.currentTimeMillis() - start);
            throw e;
        }
        catch (Error e) {
            workMonitor.recordWorkError(workName, System.currentTimeMillis() - start);
            throw e;
        }
        catch (Throwable e) {
            workMonitor.recordWorkError(workName, System.currentTimeMillis() - start);
            throw new RuntimeException(e);
        }
    }
}
