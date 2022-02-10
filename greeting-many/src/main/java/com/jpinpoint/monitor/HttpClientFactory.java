package com.jpinpoint.monitor;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.Configurable;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;


@Component
@Slf4j
public class HttpClientFactory {
    private static final int MAX_CONN_TOTAL = 6;
    private static final int MAX_CONN_PER_ROUTE = 4;
    private static final int DEF_MAX_CONN_PER_ROUTE = 3;
    private static final int CONNECTION_TIMEOUT_MILLISEC = 250;
    private static final int CONNECTION_REQUEST_TIMEOUT_MILLISEC = 350;
    private static final int SO_TIMEOUT_MILLISEC_1 = 4000;
    private static final int SO_TIMEOUT_MILLISEC_2 = 50;
    private static final String HOST_URL = "localhost:8081";
    private volatile PoolingHttpClientConnectionManager connectionMgr;

    public RestTemplate createRestTemplate() {
        ClientHttpRequestFactory factory = getClientHttpRequestFactory();

        return new RestTemplate(factory);
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(); /*HttpClientBuilder.create()
                .setMaxConnTotal(MAX_CONN_TOTAL)
                .setMaxConnPerRoute(MAX_CONN_PER_ROUTE)
                .disableConnectionState()
                .build());*/

        httpComponentsClientHttpRequestFactory.setReadTimeout(SO_TIMEOUT_MILLISEC_1);
        httpComponentsClientHttpRequestFactory.setConnectTimeout(CONNECTION_TIMEOUT_MILLISEC);
        httpComponentsClientHttpRequestFactory.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLISEC);
        HttpClient httpClient = createHttpClient();
        registerRequestConfigMBean(httpClient);
        httpComponentsClientHttpRequestFactory.setHttpClient(httpClient);
        return httpComponentsClientHttpRequestFactory;
    }

    public void logPoolInfo(String label) {
        if(log.isInfoEnabled()) {
            StringBuilder logBuilder = new StringBuilder();
            int count = 0;
            for (HttpRoute route : connectionMgr.getRoutes()) {
                logBuilder.append(" ").append(++count).append(": ").append(route).append(" = ")
                        .append(connectionMgr.getStats(route));
            }
            log.debug("{}: connectionMgr.getTotalStats = {}, routes: [{}]", label, connectionMgr.getTotalStats(), logBuilder);
        }
    }

    private static void registerPoolStatsMBean(PoolingHttpClientConnectionManager connMgr) { //fixed NO - PMD - suppressed HttpClientBuilderWithoutPoolSize - reason: false positive - not building #159
        PoolingHttpClientConnectionManagerStatsMXBean poolStats = new PoolingHttpClientConnectionManagerStats(connMgr);
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName = new ObjectName("com.jpinpoint.monitor:name=PoolingHttpClientConnectionManager");
            if (!mbeanServer.isRegistered(objectName)) {
                mbeanServer.registerMBean(poolStats, objectName);
                log.debug("registered mbean {}", objectName.getCanonicalName());
            }
        } catch (JMException ex) {
            log.error("register mbean error", ex);
        }
    }

    private static void registerRequestConfigMBean(HttpClient httpClient) {
        if (httpClient instanceof Configurable) {
            RequestConfig reqConfig = ((Configurable) httpClient).getConfig();
            log.debug("request config: {}", reqConfig);

            HttpClientRequestConfigMXBean reqConfigMBean = new HttpClientRequestConfig(reqConfig);
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            try {
                ObjectName objectName = new ObjectName("com.jpinpoint.monitor:name=HttpClientRequestConfig");
                if (!mbeanServer.isRegistered(objectName)) {
                    mbeanServer.registerMBean(reqConfigMBean, objectName);
                    log.debug("registered mbean {}", objectName.getCanonicalName());
                }
            } catch (JMException ex) {
                log.error("register mbean error", ex);
            }
        }
        else {
            log.warn("No request config found, httpClient does not implement RequestConfig: {}", httpClient);
        }
    }

    private HttpClient createHttpClient() {
        RequestConfig requestConfig = createRequestConfig();
        connectionMgr = createConnectionManager();

        registerPoolStatsMBean(connectionMgr);

        logPoolInfo("start");
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionMgr)
                //.addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor())
                //.setKeepAliveStrategy(keepConnectionAliveStrategy)
                .disableConnectionState()
                .build();
    }

    private RequestConfig createRequestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLISEC)
                .setConnectTimeout(CONNECTION_TIMEOUT_MILLISEC)
                .setSocketTimeout(SO_TIMEOUT_MILLISEC_1) // overrides setting in SocketConfig
                .build();
    }

    private PoolingHttpClientConnectionManager createConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        connectionManager.setDefaultMaxPerRoute(DEF_MAX_CONN_PER_ROUTE);
        connectionManager.setMaxTotal(MAX_CONN_TOTAL);

        //HttpHost host = new HttpHost(HOST_URL); // HOST_URL = "localhost:8081"; // wrong! bug
        HttpHost host = new HttpHost("localhost", 8081, "http");
        HttpRoute route = new HttpRoute(host);

        SocketConfig sc = SocketConfig.custom()
                .setSoTimeout(SO_TIMEOUT_MILLISEC_2) // timeout gets overridden by requestConfig
                .setSoKeepAlive(true)
                .setTcpNoDelay(false)
                .build();
        connectionManager.setSocketConfig(route.getTargetHost(), sc);

        connectionManager.setMaxPerRoute(route, MAX_CONN_PER_ROUTE);

        log.debug("For host {} name {}, targetHost {} name {} and route {} : created socketConfig:{} gotten socketConfig:{}", host, host.getHostName(), route.getTargetHost(), route.getTargetHost().getHostName(), route, sc, connectionManager.getSocketConfig(host));

        return connectionManager;
    }


}

