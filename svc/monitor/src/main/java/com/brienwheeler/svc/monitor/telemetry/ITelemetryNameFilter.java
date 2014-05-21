package com.brienwheeler.svc.monitor.telemetry;

public interface ITelemetryNameFilter
{
    boolean process(String name);
}
