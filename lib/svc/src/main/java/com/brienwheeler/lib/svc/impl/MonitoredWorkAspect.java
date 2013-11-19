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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import com.brienwheeler.lib.svc.MonitoredWork;
import com.brienwheeler.lib.util.ValidationUtils;

@Aspect
public class MonitoredWorkAspect
{
	@Pointcut("execution(@com.brienwheeler.lib.svc.MonitoredWork * *(..))")
	public void monitoredWorkPointcut() {}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Around("monitoredWorkPointcut()")
	public Object aroundMonitoredWork(final ProceedingJoinPoint joinPoint)
	{
		ValidationUtils.assertTrue(joinPoint.getTarget() instanceof StartableServiceBase,
				"@MonitoredWork target must be subclass of StartableServiceBase");
		ValidationUtils.assertTrue(joinPoint.getSignature() instanceof MethodSignature,
				"@MonitoredWork signature must be a method");
		
		ServiceWork work = new ServiceWork() {
			@Override
			public Object doServiceWork() throws InterruptedException
			{
				try {
					return joinPoint.proceed();
				}
				catch (InterruptedException e) {
					throw e;
				}
				catch (RuntimeException e) {
					throw e;
				}
				catch (Error e) {
					throw e;
				}
				catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		final StartableServiceBase service = (StartableServiceBase) joinPoint.getTarget();
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		MonitoredWork annotation = signature.getMethod().getAnnotation(MonitoredWork.class); 
		
		final String workName = annotation != null && !annotation.value().isEmpty() ? annotation.value() : signature.getName();
		
		if ((annotation == null) || !annotation.gracefulShutdown())
			return service.executeMonitoredWork(workName, work);
		
		// default annotation behavior is to also support graceful shutdown
		final ServiceWork innerWork = work;
		work = new ServiceWork() {
			@Override
			public Object doServiceWork() throws InterruptedException {
				return service.executeMonitoredWork(workName, innerWork);
			}
		};
		return service.executeWithGracefulShutdown(work);
	}
}
