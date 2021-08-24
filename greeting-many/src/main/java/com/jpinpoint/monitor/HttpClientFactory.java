package com.jpinpoint.monitor;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
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
    private static final int CONNECTION_REQUEST_TIMEOUT_MILLISEC = 5100;
    private static final int SO_TIMEOUT_MILLISEC = 5000;
    private static final String HOST_URL = "localhost:8080";
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

//        httpComponentsClientHttpRequestFactory.setReadTimeout(SO_TIMEOUT_MILLISEC);
//        httpComponentsClientHttpRequestFactory.setConnectTimeout(CONNECTION_TIMEOUT_MILLISEC);
//        httpComponentsClientHttpRequestFactory.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLISEC);

        httpComponentsClientHttpRequestFactory.setHttpClient(createHttpClient());
        return httpComponentsClientHttpRequestFactory;
    }

    private void logPoolInfo(String label) {
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

    private static void registerMBean(PoolingHttpClientConnectionManager connMgr) { //NO-PMD - suppressed HttpClientBuilderWithoutPoolSize - reason: false positive - not building #159
        PoolingHttpClientConnectionManagerStatsMXBean poolStats = new PoolingHttpClientConnectionManagerStats(connMgr);
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName = new ObjectName("com.jpinpoint.monitor:name=PoolingHttpClientConnectionManager");
            if (!mbeanServer.isRegistered(objectName)) {
                mbeanServer.registerMBean(poolStats, objectName);
                log.debug("registered mbean {}");
            }
        } catch (JMException ex) {
            log.error("register mbean error", ex);
        }
    }

    private HttpClient createHttpClient() {
        RequestConfig requestConfig = createRequestConfig();
        connectionMgr = createConnectionManager();

        registerMBean(connectionMgr);

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
                .setSocketTimeout(SO_TIMEOUT_MILLISEC)
                .build();
    }

    private PoolingHttpClientConnectionManager createConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        connectionManager.setDefaultMaxPerRoute(DEF_MAX_CONN_PER_ROUTE); //MAX_CONN_TOTAL); // or MAX_CONN_PER_ROUTE?
        connectionManager.setMaxTotal(MAX_CONN_TOTAL);

        HttpHost host = new HttpHost(HOST_URL);
        HttpRoute route = new HttpRoute(host);

        connectionManager.setSocketConfig(route.getTargetHost(), SocketConfig.custom()
                .setSoTimeout(SO_TIMEOUT_MILLISEC)
                .setSoKeepAlive(true)
                .setTcpNoDelay(false)
                .build());

        connectionManager.setMaxPerRoute(route, MAX_CONN_PER_ROUTE);

        return connectionManager;
    }


}

