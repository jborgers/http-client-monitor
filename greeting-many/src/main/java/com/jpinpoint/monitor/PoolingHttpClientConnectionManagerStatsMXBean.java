package com.jpinpoint.monitor;

import java.util.Map;

public interface PoolingHttpClientConnectionManagerStatsMXBean {
        String getName();
        PoolUsage getTotalUsage();
        Map<String, PoolUsage> getUsageForRoutes();
        PoolUsage getRoute1Usage();

        int getTotalLeased();
        int getTotalPending();
        int getTotalAvailable();
        int getTotalMax();

        int getRoute1Leased();
        int getRoute1Pending();
        int getRoute1Available();
        int getRoute1Max();
}

/*
        leased = l;
        pending = p;
        available = a;
        max = m;
    }
    public long getLeased() { return leased; }
    public long getPending() { return pending; }
    public long getAvailable() { return available; }
    public long getMax() { return max; }
}*/
