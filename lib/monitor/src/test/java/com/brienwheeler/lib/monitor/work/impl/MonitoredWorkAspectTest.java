package com.brienwheeler.lib.monitor.work.impl;

import com.brienwheeler.lib.monitor.work.WorkRecord;
import com.brienwheeler.lib.monitor.work.WorkRecordCollection;
import com.brienwheeler.lib.monitor.work.mocks.MockMonitoredWork;
import org.junit.Assert;
import org.junit.Test;

public class MonitoredWorkAspectTest
{
    @Test
    public void testWorkMethodName()
    {
        MockMonitoredWork worker = new MockMonitoredWork();
        worker.testMethodWorkMethodName(0);

        WorkRecordCollection workRecordCollection = worker.getWorkMonitor().rollRecords();
        Assert.assertEquals(1, workRecordCollection.size());
        WorkRecord workRecord = workRecordCollection.getWorkRecord("testMethodWorkMethodName");
        Assert.assertNotNull(workRecord);
        Assert.assertEquals(0, workRecord.getWorkErrorCount());
        Assert.assertEquals(1, workRecord.getWorkOkCount());
    }

    @Test
    public void testWorkName()
    {
        MockMonitoredWork worker = new MockMonitoredWork();
        worker.testMethodWorkName(0);

        WorkRecordCollection workRecordCollection = worker.getWorkMonitor().rollRecords();
        Assert.assertEquals(1, workRecordCollection.size());
        WorkRecord workRecord = workRecordCollection.getWorkRecord(MockMonitoredWork.WORK_NAME);
        Assert.assertNotNull(workRecord);
        Assert.assertEquals(0, workRecord.getWorkErrorCount());
        Assert.assertEquals(1, workRecord.getWorkOkCount());
    }

    @Test
    public void testWorkNameInterruptedException()
    {
        MockMonitoredWork worker = new MockMonitoredWork();
        try {
            worker.testMethodWorkNameInterruptedException(0);
            Assert.fail();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Assert.assertTrue(Thread.interrupted());
        WorkRecordCollection workRecordCollection = worker.getWorkMonitor().rollRecords();
        Assert.assertEquals(1, workRecordCollection.size());
        WorkRecord workRecord = workRecordCollection.getWorkRecord(MockMonitoredWork.WORK_NAME);
        Assert.assertNotNull(workRecord);
        Assert.assertEquals(1, workRecord.getWorkErrorCount());
        Assert.assertEquals(0, workRecord.getWorkOkCount());
    }

    @Test
    public void testWorkNameRuntimeException()
    {
        MockMonitoredWork worker = new MockMonitoredWork();
        try {
            worker.testMethodWorkNameRuntimeException(0);
            Assert.fail();
        }
        catch (RuntimeException e) {
            // expected
        }

        WorkRecordCollection workRecordCollection = worker.getWorkMonitor().rollRecords();
        Assert.assertEquals(1, workRecordCollection.size());
        WorkRecord workRecord = workRecordCollection.getWorkRecord(MockMonitoredWork.WORK_NAME);
        Assert.assertNotNull(workRecord);
        Assert.assertEquals(1, workRecord.getWorkErrorCount());
        Assert.assertEquals(0, workRecord.getWorkOkCount());
    }

    @Test
    public void testWorkNameError()
    {
        MockMonitoredWork worker = new MockMonitoredWork();
        try {
            worker.testMethodWorkNameError(0);
            Assert.fail();
        }
        catch (Error e) {
            // expected
        }

        WorkRecordCollection workRecordCollection = worker.getWorkMonitor().rollRecords();
        Assert.assertEquals(1, workRecordCollection.size());
        WorkRecord workRecord = workRecordCollection.getWorkRecord(MockMonitoredWork.WORK_NAME);
        Assert.assertNotNull(workRecord);
        Assert.assertEquals(1, workRecord.getWorkErrorCount());
        Assert.assertEquals(0, workRecord.getWorkOkCount());
    }

    @Test
    public void testWorkNameThrowable()
    {
        MockMonitoredWork worker = new MockMonitoredWork();
        try {
            worker.testMethodWorkNameThrowable(0);
            Assert.fail();
        }
        catch (RuntimeException e) {
            // expected
            Assert.assertEquals(Throwable.class, e.getCause().getClass());
        }
        catch (Throwable e) {
            Assert.fail(); // aspect turns this into RuntimeException
        }

        WorkRecordCollection workRecordCollection = worker.getWorkMonitor().rollRecords();
        Assert.assertEquals(1, workRecordCollection.size());
        WorkRecord workRecord = workRecordCollection.getWorkRecord(MockMonitoredWork.WORK_NAME);
        Assert.assertNotNull(workRecord);
        Assert.assertEquals(1, workRecord.getWorkErrorCount());
        Assert.assertEquals(0, workRecord.getWorkOkCount());
    }
}
