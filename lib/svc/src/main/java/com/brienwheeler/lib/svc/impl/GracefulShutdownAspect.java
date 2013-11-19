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

import com.brienwheeler.lib.util.ValidationUtils;

@Aspect
public class GracefulShutdownAspect
{
	@Pointcut("execution(@com.brienwheeler.lib.svc.GracefulShutdown * *(..))")
	public void gracefulShutdownPointcut() {}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Around("gracefulShutdownPointcut()")
	public Object aroundGracefulShutdown(final ProceedingJoinPoint joinPoint)
	{
		ValidationUtils.assertTrue(joinPoint.getTarget() instanceof StartableServiceBase,
				"@GracefulShutdown target must be subclass of StartableServiceBase");
		
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
		
		StartableServiceBase service = (StartableServiceBase) joinPoint.getTarget();
		return service.executeWithGracefulShutdown(work);
	}
}
