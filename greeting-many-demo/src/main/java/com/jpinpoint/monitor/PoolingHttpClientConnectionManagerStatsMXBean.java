package com.jpinpoint.monitor;

import org.apache.http.config.SocketConfig;

import java.util.Map;

public interface PoolingHttpClientConnectionManagerStatsMXBean {
        String getName();
        Map<String, PoolUsage> getUsageForRoutes();

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

        SocketConfig getDefaultSocketConfig();
}

