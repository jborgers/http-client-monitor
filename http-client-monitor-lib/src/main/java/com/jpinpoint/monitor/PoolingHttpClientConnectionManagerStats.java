package com.jpinpoint.monitor;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
class PoolingHttpClientConnectionManagerStats implements PoolingHttpClientConnectionManagerStatsMXBean {

    private final PoolingHttpClientConnectionManager connectionMgr;
    private static final PoolStats NO_POOL_STATS = new PoolStats(0,0,0,0);

    public PoolingHttpClientConnectionManagerStats(PoolingHttpClientConnectionManager connMgr) {
        connectionMgr = connMgr;
    }

    @Override
    public String getName() {
        return "PoolingHttpClientConnectionManagerStats";
    }

    @Override
    public Map<String, PoolUsage> getUsageForRoutes() {
        Map<String, PoolUsage> map = new HashMap<>();
        for (HttpRoute route : connectionMgr.getRoutes()) {
            PoolStats stats = connectionMgr.getStats(route);
            PoolUsage pu = new PoolUsage(stats.getLeased(), stats.getPending(), stats.getAvailable(), stats.getMax());
            map.put(route.toString(), pu);
        }
        return map;
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
    public String getRoute1TargetHost() {
        HttpRoute route1 = connectionMgr.getRoutes().iterator().next();
        return Objects.toString(route1.getTargetHost());
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

    @Override
    public Map<String, SocketConfig> getSocketConfigForRoutes() {
        Map<String, SocketConfig> map = new HashMap<>();
        for (HttpRoute route : connectionMgr.getRoutes()) {
            HttpHost host = route.getTargetHost();
            map.put(host.toString(), connectionMgr.getSocketConfig(host));
        }
        return map;
    }

    @Override
    public SocketConfig getDefaultSocketConfig() {
        return connectionMgr.getDefaultSocketConfig();
    }

    private PoolStats getRoute1Stats() {
        Set<HttpRoute> routes = connectionMgr.getRoutes();
        return (!routes.isEmpty()) ? connectionMgr.getStats(routes.iterator().next()) : NO_POOL_STATS;
    }
}
