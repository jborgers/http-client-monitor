package com.jpinpoint.monitor;

import org.apache.http.config.SocketConfig;

import java.util.Map;

public interface PoolingHttpClientConnectionManagerStatsMXBean {
        String getName();
        //PoolUsage getTotalUsage();
        Map<String, PoolUsage> getUsageForRoutes();
        //PoolUsage getRoute1Usage();

        int getTotalLeased();
        int getTotalPending();
        int getTotalAvailable();
        int getTotalMax();

        String getRoute1TargetHost();
        int getRoute1Leased();
        int getRoute1Pending();
        int getRoute1Available();
        int getRoute1Max();

        Map<String, SocketConfig> getSocketConfigForRoutes();

        //SocketConfig getRoute1SocketConfig();

        SocketConfig getDefaultSocketConfig();
}

