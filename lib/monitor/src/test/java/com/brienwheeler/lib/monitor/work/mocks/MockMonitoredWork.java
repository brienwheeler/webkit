package com.brienwheeler.lib.monitor.work.mocks;

import com.brienwheeler.lib.monitor.work.IWorkMonitorProvider;
import com.brienwheeler.lib.monitor.work.MonitoredWork;
import com.brienwheeler.lib.monitor.work.WorkMonitor;

public class MockMonitoredWork implements IWorkMonitorProvider
{
    public static final String WORK_NAME = "testWork";

    private final WorkMonitor workMonitor = new WorkMonitor(getClass().getName());

    @Override
    public WorkMonitor getWorkMonitor()
    {
        return workMonitor;
    }

    @MonitoredWork
    public void testMethodWorkMethodName(long sleep)
    {
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                // unexpected, throw so test fails
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    @MonitoredWork(value=WORK_NAME)
    public void testMethodWorkName(long sleep)
    {
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                // unexpected, throw so test fails
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    @MonitoredWork(value=WORK_NAME)
    public void testMethodWorkNameInterruptedException(long sleep) throws InterruptedException
    {
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                // return without throwing so test fails
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new InterruptedException("test");
    }

    @MonitoredWork(value=WORK_NAME)
    public void testMethodWorkNameRuntimeException(long sleep)
    {
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                // return without throwing so test fails
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new RuntimeException("test");
    }

    @MonitoredWork(value=WORK_NAME)
    public void testMethodWorkNameError(long sleep)
    {
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                // return without throwing so test fails
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new Error("test");
    }

    @MonitoredWork(value=WORK_NAME)
    public void testMethodWorkNameThrowable(long sleep) throws Throwable
    {
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                // return without throwing so test fails
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new Throwable("test");
    }
}
