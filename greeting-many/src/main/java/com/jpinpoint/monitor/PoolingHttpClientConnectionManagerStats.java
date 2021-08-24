package com.jpinpoint.monitor;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class PoolingHttpClientConnectionManagerStats implements PoolingHttpClientConnectionManagerStatsMXBean {

    private final PoolingHttpClientConnectionManager connectionMgr;
    private final AtomicInteger counter = new AtomicInteger(0);
    private static final PoolStats NO_POOL_STATS = new PoolStats(0,0,0,0);

    public PoolingHttpClientConnectionManagerStats(PoolingHttpClientConnectionManager connMgr) {
        connectionMgr = connMgr;
    }

    @Override
    public String getName() {
        return "PoolingHttpClientConnectionManagerStats";
    }

    @Override
    public PoolUsage getTotalUsage() {
        PoolStats stats = connectionMgr.getTotalStats();
        log.debug("PoolUsage.getTotalUsage - connectionMgr.getTotalStats = {}", stats);
        return new PoolUsage(stats.getLeased(), stats.getPending(), stats.getAvailable(), stats.getMax());
    }

    @Override
    public Map<String, PoolUsage> getUsageForRoutes() {
        Map<String, PoolUsage> map = new HashMap();
        for (HttpRoute route : connectionMgr.getRoutes()) {
            PoolStats stats = connectionMgr.getStats(route);
            PoolUsage pu = new PoolUsage(stats.getLeased(), stats.getPending(), stats.getAvailable(), stats.getMax());
            map.put(route.toString(), pu);
        }
        return map;
    }

    @Override
    public PoolUsage getRoute1Usage() {
        Set<HttpRoute> routes = connectionMgr.getRoutes();
        PoolUsage pu = null;
        if (!routes.isEmpty()) {
            PoolStats stats = connectionMgr.getStats(routes.iterator().next());
            pu = new PoolUsage(stats.getLeased(), stats.getPending(), stats.getAvailable(), stats.getMax());
        }
        return pu;
    }

    @Override
    public int getTotalLeased() {
        return connectionMgr.getTotalStats().getLeased();
    }

    @Override
    public int getTotalPending() {
        return connectionMgr.getTotalStats().getPending();
    }

    @Override
    public int getTotalAvailable() {
        return connectionMgr.getTotalStats().getAvailable();
    }

    @Override
    public int getTotalMax() {
        return connectionMgr.getTotalStats().getMax();
    }

    @Override
    public int getRoute1Leased() {
        return getRoute1Stats().getLeased();
    }

    @Override
    public int getRoute1Pending() {
        return getRoute1Stats().getPending();
    }

    @Override
    public int getRoute1Available() {
        return getRoute1Stats().getAvailable();
    }

    @Override
    public int getRoute1Max() {
        return getRoute1Stats().getMax();
    }

    private PoolStats getRoute1Stats() {
        Set<HttpRoute> routes = connectionMgr.getRoutes();
        return (!routes.isEmpty()) ? connectionMgr.getStats(routes.iterator().next()) : NO_POOL_STATS;
    }

}
