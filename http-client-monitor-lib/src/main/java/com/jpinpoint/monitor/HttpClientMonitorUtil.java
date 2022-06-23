package com.jpinpoint.monitor;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.Configurable;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HttpClientMonitorUtil {

    private static final Map<String, PoolingHttpClientConnectionManager> serviceNameToConnectionManager = new ConcurrentHashMap<>();

    private HttpClientMonitorUtil() {
    }


    /**
     * Log connection pool information, that is, the pool statistics per route,
     * for the specified service with specified label.
     * @param serviceName name of the service to connect to
     * @param label the label to add in the log line before the statistics
     */
    public static void logPoolInfo(@NonNull String serviceName, @NonNull String label) {
        StringBuilder logBuilder = new StringBuilder();
        int count = 0;
        PoolingHttpClientConnectionManager connectionManager = serviceNameToConnectionManager.get(serviceName);
        log.debug("service={} - connectionManager={}", serviceName, connectionManager);
        if (connectionManager!= null) {
            for (HttpRoute route : connectionManager.getRoutes()) {
                logBuilder.append(" ").append(++count).append(": ").append(route).append(" = ")
                        .append(connectionManager.getStats(route));
            }
            log.debug("service={} - {}: connectionMgr.getTotalStats = {}, routes: [{}]", serviceName, label, connectionManager.getTotalStats(), logBuilder);
        }
        else {
            log.error("No Connection Manager found for service={}", serviceName);
        }
    }

    /**
     * Register an MBean for monitoring the RequestConfig of the specified HttpClient.
     * Name of the MBean: "com.jpinpoint.monitor:name=" + serviceName + "-HttpClientRequestConfig"
     * @param serviceName name of the service to connect to.
     * @param httpClient the httpClient to monitor the RequestConfig settings of.
     */
    public static void registerRequestConfigMBean(String serviceName, HttpClient httpClient) {
        if (httpClient instanceof Configurable) {
            RequestConfig reqConfig = ((Configurable) httpClient).getConfig();
            log.debug("service={} request config={}", serviceName, reqConfig);

            HttpClientRequestConfigMXBean reqConfigMBean = new HttpClientRequestConfig(reqConfig);
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            try {
                ObjectName objectName = new ObjectName("com.jpinpoint.monitor:name=" + serviceName + "-HttpClientRequestConfig");
                if (!mbeanServer.isRegistered(objectName)) {
                    mbeanServer.registerMBean(reqConfigMBean, objectName);
                    log.debug("registered for service={} mbean={}", serviceName, objectName.getCanonicalName());
                }
            } catch (JMException ex) {
                log.error("register mbean error", ex);
            }
        }
        else {
            log.error("No request config found for service={}, httpClient does not implement RequestConfig: {}", serviceName, httpClient);
        }
    }

    /**
     * Register an MBean for monitoring the connection pool of the specified connection manager.
     * Name of the MBean: com.jpinpoint.monitor:name=" + serviceName + "-" + "PoolingHttpClientConnectionManager"
     * @param serviceName name of the service to connect to.
     * @param connMgr the PoolingHttpClientConnectionManager to monitor the pool statistics of.
     */
    public static void registerPoolStatsMBean(String serviceName, PoolingHttpClientConnectionManager connMgr) {
        storeConnectionManagerForService(serviceName, connMgr);
        PoolingHttpClientConnectionManagerStatsMXBean poolStats = new PoolingHttpClientConnectionManagerStats(connMgr);
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName = new ObjectName("com.jpinpoint.monitor:name=" + serviceName + "-" + "PoolingHttpClientConnectionManager");
            if (!mbeanServer.isRegistered(objectName)) {
                mbeanServer.registerMBean(poolStats, objectName);
                log.debug("registered for service={} mbean={}", serviceName, objectName.getCanonicalName());
            }
        } catch (JMException ex) {
            log.error("register mbean error", ex);
        }
    }

    private static void storeConnectionManagerForService(String serviceName, PoolingHttpClientConnectionManager connectionManager ) {
        serviceNameToConnectionManager.put(serviceName, connectionManager);
    }

}
